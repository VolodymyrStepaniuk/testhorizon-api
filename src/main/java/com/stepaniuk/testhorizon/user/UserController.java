package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users", produces = "application/json")
public class UserController {

        private final UserService userService;

        @GetMapping
        public ResponseEntity<PagedModel<UserResponse>> getAllUsers(Pageable pageable) {
            return ResponseEntity.ok(userService.getAllUsers(pageable));
        }
}
