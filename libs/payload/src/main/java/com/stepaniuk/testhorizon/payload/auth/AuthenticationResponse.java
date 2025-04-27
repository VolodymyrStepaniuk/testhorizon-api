package com.stepaniuk.testhorizon.payload.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
@Builder
public class AuthenticationResponse {
    @JsonProperty("access_token")
    private final String accessToken;
    @JsonProperty("refresh_token")
    private final String refreshToken;
}
