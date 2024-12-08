package com.stepaniuk.testhorizon.security;

import com.stepaniuk.testhorizon.payload.auth.AuthenticationResponse;
import com.stepaniuk.testhorizon.security.exceptions.InvalidTokenException;
import com.stepaniuk.testhorizon.user.User;
import com.stepaniuk.testhorizon.user.UserRepository;
import com.stepaniuk.testhorizon.user.exceptions.NoSuchUserByEmailException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public String generateAccessToken(User user){
        return jwtProvider.generateAccessToken(user);
    }

    public String generateRefreshToken(User user){
        return jwtProvider.generateRefreshToken(user);
    }

    private boolean isRefreshToken(String token) {
        return jwtProvider.getTokenType(token) == TokenType.REFRESH;
    }

    public AuthenticationResponse refreshToken(String refreshToken){

        if (!refreshToken.startsWith("Bearer ") || !isRefreshToken(refreshToken)) {
            throw new InvalidTokenException(refreshToken);
        }

        final String methodRefreshToken;
        final String username;

        String accessToken = null;
        methodRefreshToken = refreshToken.substring(7);
        username = jwtProvider.extractUsername(methodRefreshToken);

        if (username != null) {
            var user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new NoSuchUserByEmailException(username));
            if (jwtProvider.isTokenValid(methodRefreshToken, user)) {
                accessToken = jwtProvider.generateAccessToken(user);
            }
        }
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(methodRefreshToken)
                .build();
    }
}
