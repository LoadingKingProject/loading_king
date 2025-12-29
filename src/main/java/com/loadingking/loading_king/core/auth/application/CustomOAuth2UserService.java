package com.loadingking.loading_king.core.auth.application;


import com.loadingking.loading_king.core.auth.dto.GoogleOAuth2UserInfo;
import com.loadingking.loading_king.core.auth.dto.OAuth2UserInfo;
import com.loadingking.loading_king.core.user.domain.Role;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.repository.UserRepository;
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

        OAuth2User oAuth2User = super.loadUser(userRequest); // 스프링시큐리티에서 제공되는 메서드 호출
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo;
        if("google".equals(registrationId)){
            userInfo = new GoogleOAuth2UserInfo(attributes);
        }else{
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 공급자입니다: " + registrationId);
        }

        User user = saveOrUpdate(userInfo);
        return new CustomUserDetail(user, attributes);
    }

    public User saveOrUpdate(OAuth2UserInfo userInfo) {

            String email = userInfo.getEmail().toLowerCase();

        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.createSocialUser(userInfo.getEmail(), Role.USER )
                ));
    }





}
