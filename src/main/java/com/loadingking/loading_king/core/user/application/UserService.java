package com.loadingking.loading_king.core.user.application;


import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.dto.UserRequest;
import com.loadingking.loading_king.core.user.dto.UserResponse;
import com.loadingking.loading_king.core.user.userRepository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 내 정보 조회
    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new UserResponse(user);
    }

    // 내 정보 수정 (차량번호)
    @Transactional
    public UserResponse updateMyInfo(Long userId, UserRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.updateInfo(request.getCarNumber());

        return new UserResponse(user);
    }


}
