package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users", produces = "application/json")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id, AuthInfo authInfo) {
        return ResponseEntity.ok(userService.getUserById(id, authInfo));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email, AuthInfo authInfo) {
        return ResponseEntity.ok(userService.getUserByEmail(email, authInfo));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest userRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, AuthInfo authInfo) {
        userService.deleteUserById(id, UUID.randomUUID().toString(), authInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(AuthInfo authInfo)
            throws NoSuchUserByIdException {
        return ResponseEntity.ok(userService.getUserById(authInfo.getUserId(), authInfo));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMe(@RequestBody UserUpdateRequest userRequest, AuthInfo authInfo) {
        return ResponseEntity.ok(userService.updateUser(authInfo.getUserId(), userRequest, UUID.randomUUID().toString(), authInfo));
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(AuthInfo authInfo) {
        userService.deleteUserById(authInfo.getUserId(), UUID.randomUUID().toString(), authInfo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<UserResponse>> getAllUsers(Pageable pageable,
                                                                @Nullable @RequestParam(required = false) List<Long> ids,
                                                                @Nullable @RequestParam(required = false) String email,
                                                                @Nullable @RequestParam(required = false) String fullName,
                                                                AuthInfo authInfo) {
        return ResponseEntity.ok(userService.getAllUsers(pageable, ids, email, fullName, authInfo));
    }

    @GetMapping("/top")
    public ResponseEntity<PagedModel<UserResponse>> getTopUsersByRating(Pageable pageable, AuthInfo authInfo) {
        return ResponseEntity.ok(userService.getTopUsersByRating(pageable, authInfo));
    }

    @PatchMapping("/change-authority/{id}")
    public ResponseEntity<Void> changeUserAuthority(@PathVariable Long id, @RequestParam AuthorityName authority, AuthInfo authInfo) {
        userService.changeUserAuthority(id, authority, authInfo);
        return ResponseEntity.noContent().build();
    }
}
