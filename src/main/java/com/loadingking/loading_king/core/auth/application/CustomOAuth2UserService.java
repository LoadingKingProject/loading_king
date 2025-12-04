package com.loadingking.loading_king.core.auth.application;


import com.loadingking.loading_king.core.user.domain.Role;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.userRepository.UserRepository;
import com.loadingking.loading_king.infra.security.CustomUserDetail;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest); // 스프링시큐리티에서 기본 제공되는 메서드 호출

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        User user = saveOrUpdate(email, name);

        return new CustomUserDetail(user, attributes);
    }

    public User saveOrUpdate(String email, String name) {
        User user = userRepository.findByEmail(email)
                .map(entity -> entity) //
                .orElse(User.builder()
                        .email(email)
                        .role(Role.USER)
                        .build());

        return userRepository.save(user);
    }





}
