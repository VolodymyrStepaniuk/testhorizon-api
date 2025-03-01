package com.stepaniuk.testhorizon.shared;


import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoService {
    private final UserRepository userRepository;

    public UserInfo getUserInfo(Long userId) {
        var user = userRepository.findById(userId).orElseThrow(
                () -> new NoSuchUserByIdException(userId)
        );
        return new UserInfo(userId, user.getFirstName(), user.getLastName());
    }
}
