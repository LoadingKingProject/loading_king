package com.loadingking.loading_king.infra.security.handler;

import com.loadingking.loading_king.infra.security.CustomUserDetail;
import com.loadingking.loading_king.infra.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // CustomUserDetail 가져오기
        CustomUserDetail userDetail = (CustomUserDetail) authentication.getPrincipal();


        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        log.info("[OAuth2SuccessHandler] Token Generated for User: {}", userDetail.getUsername());

        /*String targetUrl = UriComponentsBuilder.fromUriString("/")
                .queryParam("token", accessToken)
                .build().toUriString();*/

        Cookie tokenCookie = new Cookie("accessToken", accessToken);
        tokenCookie.setPath("/"); // 쿠키가 유효한 경로 설정
        tokenCookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
        tokenCookie.setMaxAge(60 * 60); // 쿠키 유효기간 설정
        response.addCookie(tokenCookie);


        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setPath("/");
        refreshCookie.setHttpOnly(true); // 자바스크립트 접근 불가 (XSS 방지)
        refreshCookie.setSecure(false);   // HTTPS에서만 전송 (운영 환경 적용 시 필수)
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshCookie);

        getRedirectStrategy().sendRedirect(request, response, "/");

    }
}
