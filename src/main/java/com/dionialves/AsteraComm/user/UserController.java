package com.dionialves.AsteraComm.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.dionialves.AsteraComm.user.dto.ProfileUpdateDTO;
import com.dionialves.AsteraComm.user.dto.ProfilePasswordUpdateDTO;
import com.dionialves.AsteraComm.user.dto.PasswordUpdateDTO;
import com.dionialves.AsteraComm.user.dto.UserCreateDTO;
import com.dionialves.AsteraComm.user.dto.UserResponseDTO;
import com.dionialves.AsteraComm.user.dto.UserUpdateDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<Page<UserResponseDTO>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sort) {

        String[] sortParts = sort.split(",");
        String sortField = sortParts[0];
        Sort.Direction sortDirection = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<UserResponseDTO> users = userService.findAll(search, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id) {
        try {
            UserResponseDTO user = userService.findById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user")
    public ResponseEntity<?> create(@RequestBody UserCreateDTO dto) {
        try {
            UserResponseDTO user = userService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UserUpdateDTO dto) {
        try {
            UserResponseDTO user = userService.update(id, dto);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/user/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateDTO dto) {
        try {
            userService.updatePassword(id, dto.password());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/user/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        try {
            userService.disable(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/user/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        try {
            userService.enable(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(new UserResponseDTO(user));
    }

    @PutMapping("/user/me")
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

    @PatchMapping("/user/me/password")
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
