package br.com.alura.runnercircleapi.controller;

import br.com.alura.runnercircleapi.dto.LoginRequestDTO;
import br.com.alura.runnercircleapi.dto.LoginResponseDTO;
import br.com.alura.runnercircleapi.dto.RegisterRequestDTO;
import br.com.alura.runnercircleapi.dto.TreinoRequestDTO;
import br.com.alura.runnercircleapi.model.Role;
import br.com.alura.runnercircleapi.model.TipoTreino;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.UserRepository;
import br.com.alura.runnercircleapi.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TreinoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void buscarTreino_quandoNaoExiste_retorna404() throws Exception {
        // Arrange
        User autor = new User("Usuario Teste Controller", "usuariotestecontroller", "controller.test@email.com", "hash-fake");
        User autorSalvo = userRepository.save(autor);
        String token = jwtService.gerarToken(autorSalvo, false);

        // Act & Assert
        mockMvc.perform(get("/treinos/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void criarTreino_comDadosValidos_retorna201() throws Exception {
        // Arrange
        String email = "treino201@teste.com";
        String token = registrarELogar("Usuario Treino", "usuariotreino201", email, "Alura123");
        User autor = userRepository.findByEmail(email).orElseThrow();

        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CORRIDA, 30, 5000, 300, 140, "treino via mockmvc");
        MockMultipartFile dadosPart = new MockMultipartFile(
                "dados", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        // Act & Assert
        mockMvc.perform(multipart("/treinos")
                        .file(dadosPart)
                        .param("userId", String.valueOf(autor.getId()))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void criarTreino_semToken_retorna401() throws Exception {
        // Arrange
        TreinoRequestDTO dto = new TreinoRequestDTO(TipoTreino.CORRIDA, 30, 5000, 300, 140, "treino sem token");
        MockMultipartFile dadosPart = new MockMultipartFile(
                "dados", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(dto));

        // Act & Assert
        mockMvc.perform(multipart("/treinos")
                        .file(dadosPart)
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarUsuarios_comTokenDeUsuarioComum_retorna403() throws Exception {
        // Arrange
        String token = registrarELogar("Usuario Comum", "usuariocomum403", "comum403@teste.com", "Alura123");

        // Act & Assert
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarUsuarios_comTokenDeAdmin_retorna200() throws Exception {
        // Arrange
        String email = "admin200@teste.com";
        String senha = "Alura123";
        registrarELogar("Usuario Admin", "usuarioadmin200", email, senha);

        User usuario = userRepository.findByEmail(email).orElseThrow();
        usuario.setRole(Role.ADMIN);
        userRepository.save(usuario);

        String tokenAdmin = logar(email, senha);

        // Act & Assert
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isOk());
    }

    private String registrarELogar(String nome, String username, String email, String senha) throws Exception {
        RegisterRequestDTO registerDTO = new RegisterRequestDTO(nome, username, email, senha);
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        return logar(email, senha);
    }

    private String logar(String email, String senha) throws Exception {
        LoginRequestDTO loginDTO = new LoginRequestDTO(email, senha, false);
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponseDTO loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(), LoginResponseDTO.class);
        return loginResponse.token();
    }
}
