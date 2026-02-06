package com.dionialves.AsteraComm.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserResponseDTO> findAll(String search, Pageable pageable) {
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCase(
                    search, search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(UserResponseDTO::new);
    }

    public UserResponseDTO findById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return new UserResponseDTO(user);
    }

    public UserResponseDTO create(UserCreateDTO dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new RuntimeException("Usuário já existe com este username");
        }

        User user = new User(
                dto.name(),
                dto.username(),
                passwordEncoder.encode(dto.password()),
                dto.role());

        User saved = userRepository.save(user);
        return new UserResponseDTO(saved);
    }

    public UserResponseDTO update(String id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (dto.name() != null) {
            user.setName(dto.name());
        }
        if (dto.role() != null) {
            user.setRole(dto.role());
        }
        if (dto.enabled() != null) {
            user.setEnabled(dto.enabled());
        }

        User saved = userRepository.save(user);
        return new UserResponseDTO(saved);
    }

    public UserResponseDTO updatePassword(String id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setPassword(passwordEncoder.encode(newPassword));
        User saved = userRepository.save(user);
        return new UserResponseDTO(saved);
    }

    public void delete(String id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Usuário não encontrado");
        }
        userRepository.deleteById(id);
    }

    public void disable(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public void enable(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
