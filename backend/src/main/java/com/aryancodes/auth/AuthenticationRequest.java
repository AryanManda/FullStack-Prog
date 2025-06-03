package com.aryancodes.auth;

public record AuthenticationRequest(
        String username,
        String password
) {
}
