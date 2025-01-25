package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.event.user.UserDeletedEvent;
import com.stepaniuk.testhorizon.event.user.UserUpdatedEvent;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PageMapper pageMapper;
    private final UserMapper userMapper;
    private final UserProducer userProducer;
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

    public UserResponse updateUser(Long id, UserUpdateRequest userRequest, String correlationId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        var userData = new UserUpdatedEvent.Data();

        if (userRequest.getFirstName() != null){
            user.setFirstName(userRequest.getFirstName());
            userData.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null) {
            user.setLastName(userRequest.getLastName());
            userData.setLastName(userRequest.getLastName());
        }
        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
            userData.setEmail(userRequest.getEmail());
        }
        if (userRequest.getPassword() != null)
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        var savedUser = userRepository.save(user);

        userProducer.send(
                 new UserUpdatedEvent(
                         Instant.now(), UUID.randomUUID().toString(), correlationId,
                         savedUser.getId(), userData
                 )
        );

        return userMapper.toResponse(savedUser);
    }

    public void deleteUserById(Long id, String correlationId) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        userRepository.delete(user);

        userProducer.send(
                new UserDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
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
