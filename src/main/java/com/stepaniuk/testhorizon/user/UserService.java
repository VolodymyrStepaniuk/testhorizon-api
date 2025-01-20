package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByEmailException;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PageMapper pageMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        return userMapper.toResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));
    }

    public PagedModel<UserResponse> getAllUsers(Pageable pageable, List<Long> userIds) {
        Specification<User> specification = Specification.where(null);

        if (userIds != null && !userIds.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .in(root.get("id")).value(userIds)
            );
        }

        var users = userRepository.findAll(specification, pageable);

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

    public void deleteUserById(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        userRepository.delete(user);
    }

    public PagedModel<UserResponse> getTopUsersByRating(Pageable pageable) {
        Specification<User> specification = (root, query, criteriaBuilder) -> {

            query.orderBy(criteriaBuilder.desc(root.get("totalRating")));
            return criteriaBuilder.conjunction();
        };

        var users = userRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                users.map(userMapper::toResponse),
                URI.create("/users")
        );
    }
}
