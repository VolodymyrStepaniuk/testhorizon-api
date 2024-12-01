package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.user.exceptions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PageMapper pageMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NoSuchUserByIdException(id));
    }

    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));
    }

    public PagedModel<UserResponse> getAllUsers(Pageable pageable) {
        var users = userRepository.findAll(pageable);

        return pageMapper.toResponse(
            users.map(
                userMapper::toResponse
            ), URI.create("/users")
        );
    }

    public UserResponse updateUser(Long id, UserUpdateRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        if (userRequest.getFirstName() != null)
            user.setFirstName(userRequest.getFirstName());

        if (userRequest.getLastName() != null)
            user.setLastName(userRequest.getLastName());

        if (userRequest.getEmail() != null)
            user.setEmail(userRequest.getEmail());

        if (userRequest.getPassword() != null)
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        return userMapper.toResponse(userRepository.save(user));
    }

    public void deleteUserById(Long id){
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        userRepository.delete(user);
    }
}
