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
