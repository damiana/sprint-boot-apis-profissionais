package br.com.alura.runnercircleapi.service;

import br.com.alura.runnercircleapi.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${runnercircle.jwt.secret}")
    private String secret;

    @Value("${runnercircle.jwt.expiracao-padrao}")
    private long expiracaoPadrao;

    @Value("${runnercircle.jwt.expiracao-lembrar-me}")
    private long expiracaoLembrarMe;

    public String gerarToken(User user, boolean lembrarMe) {
        long expiracao = lembrarMe ? expiracaoLembrarMe : expiracaoPadrao;
        Date emissao = new Date();
        Date expiracaoData = new Date(emissao.getTime() + expiracao);

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(emissao)
                .expiration(expiracaoData)
                .signWith(chaveAssinatura())
                .compact();
    }

    public Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(chaveAssinatura())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey chaveAssinatura() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
