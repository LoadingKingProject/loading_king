package com.loadingking.loading_king.core.auth.application;


import com.loadingking.loading_king.infra.security.CustomUserDetail;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        // [수정 포인트] 입력값이 '숫자(ID)'인지 '이메일'인지 구분해서 처리
        try {
            // 1. 숫자로 변환 시도 (토큰에서 온 ID일 경우)
            Long id = Long.parseLong(input);
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found by ID: " + id));

            return new CustomUserDetail(user);
        } catch (NumberFormatException e) {
            // 2. 숫자가 아니면 이메일로 처리 (로그인 시도일 경우)
            User user = userRepository.findByEmail(input)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found by Email: " + input));
            return new CustomUserDetail(user);
        }
    }
}
