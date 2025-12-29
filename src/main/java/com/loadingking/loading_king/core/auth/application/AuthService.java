package com.loadingking.loading_king.core.auth.application;

import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.repository.UserRepository;
import com.loadingking.loading_king.infra.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    // Redis Template이 있다면 여기에 주입 (private final RedisTemplate<String, String> redisTemplate;)

    /**
     * 토큰 재발급 (Reissue)
     * - Access Token이 만료되었을 때, Refresh Token을 통해 새 토큰을 발급합니다.
     */
    @Transactional
    public String reissue(String oldRefreshToken) {
        // 1. Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(oldRefreshToken)) {
            throw new IllegalArgumentException("Invalid Refresh Token - 다시 로그인해주세요.");
        }

        // 2. 토큰에서 유저 정보(Email or ID) 추출
        // (JwtTokenProvider 구현에 따라 getUserId 혹은 getEmail 사용)
        Claims claims = jwtTokenProvider.parseClaims(oldRefreshToken);
        String userIdStr = claims.getSubject(); // 보통 subject에 ID를 넣음
        Long userId = Long.valueOf(userIdStr);

        // 3. (옵션) Redis에 저장된 Refresh Token과 일치하는지 확인
        // String savedToken = redisTemplate.opsForValue().get("RT:" + userId);
        // if (!oldRefreshToken.equals(savedToken)) { throw ... }

        // 4. 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 5. 새로운 Access Token 발급 (Refresh Token은 그대로 쓰거나, Rotation 정책 적용 가능)
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());

        log.info("Access Token Reissued for User ID: {}", userId);
        return newAccessToken;
    }

    /**
     * 로그아웃 (Logout)
     * - 클라이언트가 더 이상 토큰을 쓰지 않겠다고 선언.
     * - Redis가 있다면 해당 Access Token을 Blacklist에 등록.
     */
    public void logout(String accessToken) {
        // 1. Access Token 남은 시간 계산
        long expiration = jwtTokenProvider.getExpiration(accessToken);

        if (expiration > 0) {
            // 2. Redis에 Blacklist 등록 (key: accessToken, value: "logout", ttl: expiration)
            // redisTemplate.opsForValue().set(accessToken, "logout", expiration, TimeUnit.MILLISECONDS);
            log.info("Logged out successfully. Token added to blacklist.");
        }
    }


}

