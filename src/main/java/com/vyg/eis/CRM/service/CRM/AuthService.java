package com.vyg.eis.CRM.service.CRM;

import com.vyg.eis.CRM.common.exception.ResourceNotFoundException;
import com.vyg.eis.CRM.domain.CRM.UserEntity;
import com.vyg.eis.CRM.dto.LoginRequest;
import com.vyg.eis.CRM.dto.LoginResponse;
import com.vyg.eis.CRM.repository.CRM.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResourceNotFoundException("Account is deactivated. Contact your administrator.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid email or password");
        }

        userService.updateLastLogin(user.getId());

        return LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
