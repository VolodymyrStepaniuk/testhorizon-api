package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.payload.user.UserUpdateRequest;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users", produces = "application/json")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PagedModel<UserResponse>> getAllUsers(Pageable pageable,
                                                                @Nullable @RequestParam(required = false) List<Long> ids) {
        return ResponseEntity.ok(userService.getAllUsers(pageable, ids));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal User user)
            throws NoSuchUserByIdException {
        return ResponseEntity.ok(userService.getUserById(user.getId()));
    }

    @GetMapping("/top")
    public ResponseEntity<PagedModel<UserResponse>> getTopUsersByRating(Pageable pageable) {
        return ResponseEntity.ok(userService.getTopUsersByRating(pageable));
    }
}
