package com.stepaniuk.testhorizon.info;

import com.stepaniuk.testhorizon.payload.info.UserInfo;
import com.stepaniuk.testhorizon.shared.UserInfoService;
import com.stepaniuk.testhorizon.testspecific.ServiceLevelUnitTest;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByIdException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ServiceLevelUnitTest
@ContextConfiguration(classes = {UserInfoService.class})
class UserInfoServiceTest {

    @Autowired
    private UserInfoService userInfoService;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getUserInfoWhenUserExistsReturnsUserInfo() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setFirstName("John");
        user.setLastName("Doe");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserInfo result = userInfoService.getUserInfo(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(user.getFirstName(), result.getFirstName());
        assertEquals(user.getLastName(), result.getLastName());
    }

    @Test
    void getUserInfoWhenUserDoesNotExistThrowsException() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when && then
        assertThrows(NoSuchUserByIdException.class, () -> userInfoService.getUserInfo(userId));
    }
}
