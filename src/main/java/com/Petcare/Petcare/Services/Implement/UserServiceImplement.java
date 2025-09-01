package com.Petcare.Petcare.Services.Implement;

import com.Petcare.Petcare.DTOs.user.UserDTO;
import com.Petcare.Petcare.DTOs.user.UserCreateDTO;
import com.Petcare.Petcare.Models.User.User;
import com.Petcare.Petcare.Repositories.UserRepository;
import com.Petcare.Petcare.Services.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImplement implements UserService {

    private final UserRepository userRepository;

    public UserServiceImplement(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ======== CONVERSIONES ========
    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail());
    }

    private User convertToEntity(UserCreateDTO dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // ahora sí se guarda
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
        return user;
    }

    // ======== MÉTODOS ========
    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        User user = convertToEntity(userCreateDTO);
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserCreateDTO userCreateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setFirstName(userCreateDTO.getFirstName());
        user.setLastName(userCreateDTO.getLastName());
        user.setEmail(userCreateDTO.getEmail());
        user.setPassword(userCreateDTO.getPassword());
        user.setRole(userCreateDTO.getRole());
        user.setActive(userCreateDTO.isActive());
        user = userRepository.save(user);
        return convertToDTO(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
}
