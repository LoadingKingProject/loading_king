package com.loadingking.loading_king.core.user.api;

import com.loadingking.loading_king.core.user.application.UserService;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.dto.UserRequest;
import com.loadingking.loading_king.core.user.dto.UserResponse;
import com.loadingking.loading_king.infra.security.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    // TODO: 시큐리티(@AuthenticationPrincipal)에서 유저 정보 꺼내서 넘기기
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetail userDetail) {

        User user = userDetail.getUser();
        UserResponse response = userService.getMyInfo(user.getId());
        return ResponseEntity.ok(response);
    }

    // TODO: 시큐리티(@AuthenticationPrincipal)에서 유저 정보 꺼내서 넘기기
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @RequestBody UserRequest request) {

        User user = userDetail.getUser();
        UserResponse response = userService.updateMyInfo(user.getId(), request);

        return ResponseEntity.ok(response);
    }
}
