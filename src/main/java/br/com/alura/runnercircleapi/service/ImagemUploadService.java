package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.exception.ImagemInvalidaException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class ImagemUploadService {

    private static final long TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024;
    private static final List<String> TIPOS_PERMITIDOS = List.of("image/jpeg", "image/png", "image/webp");

    @Value("${runnercircle.upload.dir:uploads}")
    private String uploadDir;

    public String salvar(MultipartFile imagem) {
        validar(imagem);

        String nomeArquivo = UUID.randomUUID() + extrairExtensao(imagem.getOriginalFilename());

        try {
            Path diretorio = Path.of(uploadDir);
            Files.createDirectories(diretorio);
            imagem.transferTo(diretorio.resolve(nomeArquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("erro ao salvar a imagem", e);
        }

        return "/uploads/" + nomeArquivo;
    }

    private void validar(MultipartFile imagem) {
        if (imagem.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new ImagemInvalidaException("a imagem deve ter no máximo 5MB");
        }

        String contentType = imagem.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new ImagemInvalidaException("a imagem deve estar no formato jpg, jpeg, png ou webp");
        }
    }

    private String extrairExtensao(String nomeOriginal) {
        if (nomeOriginal == null || !nomeOriginal.contains(".")) {
            return "";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }
}
