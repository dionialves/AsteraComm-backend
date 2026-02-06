package com.dionialves.AsteraComm.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.username(), data.password());
        var auth = authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO data) {
        if (userRepository.existsByUsername(data.username())) {
            return ResponseEntity.badRequest().body("Usuário já existe");
        }

        String encryptedPassword = passwordEncoder.encode(data.password());
        User newUser = new User(data.name(), data.username(), encryptedPassword, data.role());

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/validate")
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

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateCurrentUser(@RequestBody ProfileUpdateDTO data) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (data.name() != null && !data.name().isBlank()) {
            dbUser.setName(data.name());
        }

        User saved = userRepository.save(dbUser);
        return ResponseEntity.ok(new UserResponseDTO(saved));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<?> updateCurrentUserPassword(@RequestBody ProfilePasswordUpdateDTO data) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(data.currentPassword(), dbUser.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Senha atual incorreta");
        }

        dbUser.setPassword(passwordEncoder.encode(data.newPassword()));
        userRepository.save(dbUser);

        return ResponseEntity.ok().build();
    }
}
