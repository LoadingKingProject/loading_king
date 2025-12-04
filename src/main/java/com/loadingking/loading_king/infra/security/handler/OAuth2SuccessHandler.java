package com.loadingking.loading_king.infra.security.handler;

import com.loadingking.loading_king.infra.security.CustomUserDetail;
import com.loadingking.loading_king.infra.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
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

        log.info("[OAuth2SuccessHandler] Token Generated for User: {}", userDetail.getUsername());

        String targetUrl = UriComponentsBuilder.fromUriString("/")
                .queryParam("token", accessToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);

    }
}
