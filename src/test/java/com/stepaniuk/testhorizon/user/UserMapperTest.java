package com.stepaniuk.testhorizon.user;

import com.stepaniuk.testhorizon.payload.user.UserResponse;
import com.stepaniuk.testhorizon.security.authinfo.AuthInfo;
import com.stepaniuk.testhorizon.testspecific.MapperLevelUnitTest;
import com.stepaniuk.testhorizon.types.user.AuthorityName;
import com.stepaniuk.testhorizon.user.authority.Authority;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@MapperLevelUnitTest
@ContextConfiguration(classes = {UserMapperImpl.class})
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void shouldMapUserToUserResponse() {
        // given
        Instant timeOfCreation = Instant.now().plus(Duration.ofHours(10));
        Instant timeOfModification = Instant.now().plus(Duration.ofHours(20));

        User userToMap = new User(null, "John", "Doe", "johndoe@gmail.com", 19,"Password+123",
                true, true, true, true,
                Set.of(new Authority(1L,AuthorityName.ADMIN)), timeOfCreation, timeOfModification);

        var authInfo = new AuthInfo(1L, List.of());
        // when
        UserResponse userResponse = userMapper.toResponse(userToMap, authInfo);

        // then
        assertNotNull(userResponse);
        assertNull(userResponse.getId());
        assertEquals(userToMap.getEmail(), userResponse.getEmail());
        assertEquals(userToMap.getTotalRating(), userResponse.getTotalRating());
        assertEquals(userToMap.getFirstName(), userResponse.getFirstName());
        assertEquals(userToMap.getLastName(), userResponse.getLastName());
        assertEquals(Set.of(AuthorityName.ADMIN), userResponse.getAuthorities());
        assertEquals(userToMap.getCreatedAt(), userResponse.getCreatedAt());
        assertEquals(userToMap.getUpdatedAt(), userResponse.getUpdatedAt());
        assertTrue(userResponse.hasLinks());
        assertTrue(userResponse.getLinks().hasLink("self"));
    }
}
