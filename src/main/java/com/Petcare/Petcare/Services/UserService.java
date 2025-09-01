package com.Petcare.Petcare.Services;

import com.Petcare.Petcare.DTOs.user.UserDTO;
import com.Petcare.Petcare.DTOs.user.UserCreateDTO;
import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO createUser(UserCreateDTO userCreateDTO);

    UserDTO updateUser(Long id, UserCreateDTO userCreateDTO);

    void deleteUser(Long id);

    Optional<UserDTO> getUserByEmail(String email);
}
