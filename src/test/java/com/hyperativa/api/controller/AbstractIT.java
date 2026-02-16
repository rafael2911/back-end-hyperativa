package com.hyperativa.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hyperativa.api.entity.UserEntity;
import com.hyperativa.api.repository.CardBatchEntityRepository;
import com.hyperativa.api.repository.CardEntityRepository;
import com.hyperativa.api.repository.UserEntityRepository;
import com.hyperativa.api.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserEntityRepository userEntityRepository;

    @Autowired
    protected CardBatchEntityRepository cardBatchEntityRepository;

    @Autowired
    protected CardEntityRepository cardEntityRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected JwtTokenProvider jwtTokenProvider;

    protected String authToken;

    protected UserEntity testUser;

    @BeforeEach
    void setUp() {
        cardBatchEntityRepository.deleteAll();
        cardEntityRepository.deleteAll();
        userEntityRepository.deleteAll();

        testUser = userEntityRepository.save(UserEntity.builder()
                .username("carduser")
                .email("carduser@example.com")
                .password(passwordEncoder.encode("password123"))
                .build());

        // Generate JWT token for tests
        Authentication auth = new UsernamePasswordAuthenticationToken("carduser", null);
        authToken = jwtTokenProvider.generateToken(auth);
    }

}
