package com.loadingking.loading_king.core.auth.api;

import com.loadingking.loading_king.core.auth.application.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String home(@RequestParam(value = "token", required = false) String token) {
      if(token != null) {
          return "<h1>Welcome! Your token is: " + token + "</h1>";
      }
      return "<h1>홈 화면</h1><p>로그인 후 이용해주세요.</p>";
    }
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        String newAccessToken = authService.reissue(refreshToken);
        return ResponseEntity.ok().header("Authorization", "Bearer " + newAccessToken).build();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        // "Bearer " 제거 로직 필요
        String accessToken = bearerToken.substring(7);
        authService.logout(accessToken);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}
import com.loadingking.loading_king.core.user.domain.Role;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.repository.UserRepository;
import com.loadingking.loading_king.infra.security.jwt.JwtTokenProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // [1] 개발자용 프리패스 로그인 (테스트용)
    // 요청: POST /api/auth/dev-login
    @PostMapping("/dev-login")
    public ResponseEntity<?> devLogin() {
        // 1. 테스트용 기사님 계정 조회 (없으면 생성)
        User testUser = userRepository.findByEmail("driver@test.com")
                .orElseGet(() -> userRepository.save(User.createSocialUser("driver@test.com", Role.USER)));

        // 2. 정식 토큰 발급
        String token = jwtTokenProvider.createAccessToken(testUser.getId(), testUser.getRole().name());

        // 3. 응답
        return ResponseEntity.ok(Map.of(
                "token", token,
                "message", "테스트 계정(driver@test.com)으로 로그인되었습니다."
        ));
    }

    // [2] 토큰 재발급
    // 요청: POST /api/auth/reissue
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestHeader("Refresh-Token") String refreshToken) {
        String newAccessToken = authService.reissue(refreshToken);
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + newAccessToken)
                .body(Map.of("message", "토큰이 재발급되었습니다."));
    }

    // [3] 로그아웃
    // 요청: POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7);
            authService.logout(accessToken);
        }
        return ResponseEntity.ok(Map.of("message", "로그아웃 되었습니다."));
    }

    // [4] 홈 화면 (테스트용)
    // 요청: GET /api/auth/ (또는 그냥 / 도 가능하게 하려면 별도 설정 필요)
    @GetMapping("/")
    public String home(@RequestParam(value = "token", required = false) String token) {
        if(token != null) {
            return "<h1>Welcome! Your token is: " + token + "</h1>";
        }
        return "<h1>Smart Loader API Server</h1><p>서버가 정상 작동 중입니다.</p>";
    }
}
