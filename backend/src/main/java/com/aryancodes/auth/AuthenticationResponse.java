package com.aryancodes.auth;

import com.aryancodes.customer.CustomerDTO;

public record AuthenticationResponse (
        String token,
        CustomerDTO customerDTO){
}
