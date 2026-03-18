package com.hyperativa.api.service;

import com.hyperativa.api.dto.AuthRequestDTO;
import com.hyperativa.api.dto.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO register(AuthRequestDTO request);

    AuthResponseDTO login(AuthRequestDTO request);

}
