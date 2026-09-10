package dev.study.commerce.shared.api;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {
    @GetMapping("/api/v1/csrf")
    public Token token(CsrfToken csrfToken) {
        return new Token(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    public record Token(String headerName, String token) {}
}
