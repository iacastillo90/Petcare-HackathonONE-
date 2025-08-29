package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.UserDTO;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO createUser(UserDTO userDTO);

    UserDTO updateUser(Long id, UserDTO userDTO);

    void deleteUser(Long id);

    Optional<UserDTO> getUserByEmail(String email);
}
