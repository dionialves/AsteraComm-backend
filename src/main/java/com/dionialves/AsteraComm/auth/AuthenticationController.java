package com.dionialves.AsteraComm.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.dionialves.AsteraComm.infra.security.TokenService;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("resgister")
    public ResponseEntity<LoginResponseDTO> register(@RequestBody RegisterDTO data) {
        if (userRepository.findByUsername(data.username()) != null)
            return ResponseEntity.badRequest().build();

        String encryptedPassword = new BCryptPasswordEncoder().encode(data.password());
        User newUser = new User(data.username(), encryptedPassword, data.role());

        userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("validate")
    public ResponseEntity<Void> validate(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.replace("Bearer ", "");
        String username = tokenService.validateToken(token);

        if (username == null || username.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok().build();
    }
}
