package com.aryancodes.customer;

public record CustomerUpdateRequest(
        String name,
        String email,
        Integer age
) {
}
