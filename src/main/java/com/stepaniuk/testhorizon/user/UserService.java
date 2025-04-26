package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.event.user.UserAuthorityUpdatedEvent;
import com.stepaniuk.testhorizon.event.user.UserDeletedEvent;
import com.stepaniuk.testhorizon.event.user.UserUpdatedEvent;
import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.shared.exceptions.AccessToManageEntityDeniedException;
import com.stepaniuk.testhorizon.shared.PageMapper;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import com.stepaniuk.testhorizon.user.authority.AuthorityRepository;
import com.stepaniuk.testhorizon.user.email.EmailCodeRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchAuthorityException;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByEmailException;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.stepaniuk.testhorizon.security.SecurityUtils.hasAuthority;
import static com.stepaniuk.testhorizon.security.SecurityUtils.isOwner;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final EmailCodeRepository emailCodeRepository;
    private final PageMapper pageMapper;
    private final UserMapper userMapper;
    private final UserProducer userProducer;
    private final AuthorityRepository authorityRepository;

    public UserResponse getUserById(Long id, AuthInfo authInfo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        return userMapper.toResponse(user, authInfo);
    }

    public UserResponse getUserByEmail(String email, AuthInfo authInfo) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchUserByEmailException(email));

        return userMapper.toResponse(user, authInfo);
    }

    public PagedModel<UserResponse> getAllUsers(Pageable pageable, List<Long> userIds, String email, String fullName, AuthInfo authInfo) {
        Specification<User> specification = Specification.where(null);

        if (userIds != null && !userIds.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .in(root.get("id")).value(userIds)
            );
        }

        if (email != null) {
            specification = specification.and((root, query, criteriaBuilder) -> criteriaBuilder
                    .equal(root.get("email"), email)
            );
        }

        if (fullName != null) {
            specification = specification.and((root, query, criteriaBuilder) -> {
                String fullNamePattern = "%" + fullName + "%";
                return criteriaBuilder.like(
                        criteriaBuilder.concat(criteriaBuilder.concat(root.get("firstName"), " "), root.get("lastName")),
                        fullNamePattern
                );
            });
        }

        var users = userRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                users.map(
                        user -> userMapper.toResponse(user, authInfo)
                ), URI.create("/users")
        );
    }

    public UserResponse updateUser(Long id, UserUpdateRequest userRequest, String correlationId, AuthInfo authInfo) {

        if(hasNoAccessToManageUser(id, authInfo)) {
            throw new AccessToManageEntityDeniedException("User", "/users");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        var userData = new UserUpdatedEvent.Data();

        if (userRequest.getFirstName() != null) {
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

        var savedUser = userRepository.save(user);

        userProducer.send(
                new UserUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        savedUser.getId(), userData
                )
        );

        return userMapper.toResponse(savedUser, authInfo);
    }

    public void deleteUserById(Long id, String correlationId, AuthInfo authInfo) {

        if (hasNoAccessToManageUser(id, authInfo)) {
            throw new AccessToManageEntityDeniedException("User", "/users");
        }

        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        var emailCodes = emailCodeRepository.findAllByUserId(id);

        if (!emailCodes.isEmpty())
            emailCodeRepository.deleteAll(emailCodes);

        userRepository.delete(user);

        userProducer.send(
                new UserDeletedEvent(
                        Instant.now(), UUID.randomUUID().toString(), correlationId,
                        id
                )
        );
    }

    public PagedModel<UserResponse> getTopUsersByRating(Pageable pageable, AuthInfo authInfo) {
        Specification<User> specification = (root, query, criteriaBuilder) -> {

            query.orderBy(criteriaBuilder.desc(root.get("totalRating")));
            return criteriaBuilder.conjunction();
        };

        var users = userRepository.findAll(specification, pageable);

        return pageMapper.toResponse(
                users.map(
                        user -> userMapper.toResponse(user, authInfo)
                ),
                URI.create("/users")
        );
    }

    public void changeUserAuthority(Long id, AuthorityName authority, AuthInfo authInfo) {
        if(!hasAuthority(authInfo, AuthorityName.ADMIN.name()))
            throw new AccessToManageEntityDeniedException("User", "/users");

        var user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchUserByIdException(id));

        var userAuthority = authorityRepository.findByName(authority)
                .orElseThrow(() -> new NoSuchAuthorityException(authority));

        var authorities = new HashSet<>(Set.of(userAuthority));

        user.setAuthorities(authorities);
        userRepository.save(user);

        userProducer.send(
                new UserAuthorityUpdatedEvent(
                        Instant.now(), UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                        user.getId(), authority
                )
        );
    }

    private boolean hasNoAccessToManageUser(Long userId, AuthInfo authInfo) {
        return !(isOwner(authInfo, userId) || hasAuthority(authInfo, AuthorityName.ADMIN.name()));
    }

}
