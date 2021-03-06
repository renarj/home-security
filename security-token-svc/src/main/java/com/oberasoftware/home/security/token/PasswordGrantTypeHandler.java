package com.oberasoftware.home.security.token;

import org.springframework.stereotype.Component;

/**
 * @author Renze de Vries
 */
@Component
public class PasswordGrantTypeHandler extends BaseGrantTypeHandler {
    private static final String PASSWORD_GRANT = "password";

    @Override
    public String getSupportedGrantType() {
        return PASSWORD_GRANT;
    }
}
