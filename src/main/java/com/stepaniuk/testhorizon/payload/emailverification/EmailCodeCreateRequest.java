package com.stepaniuk.testhorizon.payload.emailverification;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EmailCodeCreateRequest {
    @NotNull
    private final String code;
    @NotNull
    private final Instant expiresAt;
}
