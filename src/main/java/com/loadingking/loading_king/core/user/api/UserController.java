package com.loadingking.loading_king.core.user.api;

import com.loadingking.loading_king.core.user.application.UserService;
import com.loadingking.loading_king.core.user.dto.UserRequest;
import com.loadingking.loading_king.core.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private UserService userService;


    // TODO: 시큐리티(@AuthenticationPrincipal)에서 유저 정보 꺼내서 넘기기
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo() {

        return ResponseEntity.ok().build();
    }

    // TODO: 시큐리티(@AuthenticationPrincipal)에서 유저 정보 꺼내서 넘기기
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(@RequestBody UserRequest request) {

        return ResponseEntity.ok().build();
    }
}
