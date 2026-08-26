package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.exception.AcessoNegadoException;
import br.com.alura.runnercircleapi.model.User;
import br.com.alura.runnercircleapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAutenticadoService {

    @Autowired
    private UserRepository userRepository;

    public User obterUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new AcessoNegadoException("nenhuma pessoa autenticada");
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new AcessoNegadoException("nenhuma pessoa autenticada"));
    }
}
