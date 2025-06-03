package com.aryancodes;

import com.aryancodes.s3.S3Buckets;
import com.aryancodes.s3.S3Service;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public S3Service s3Service() {
        return Mockito.mock(S3Service.class);
    }

    @Bean
    public S3Buckets s3Buckets() {
        S3Buckets s3Buckets = Mockito.mock(S3Buckets.class);
        Mockito.when(s3Buckets.getCustomer()).thenReturn("test-bucket");
        return s3Buckets;
    }
}
