package com.loadingking.loading_king.global.config;

import com.loadingking.loading_king.core.auth.application.CustomOAuth2UserService;
import com.loadingking.loading_king.core.auth.application.CustomUserDetailsService;
import com.loadingking.loading_king.infra.security.handler.OAuth2SuccessHandler;
import com.loadingking.loading_king.infra.security.jwt.JwtAuthenticationFilter;
import com.loadingking.loading_king.infra.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomOAuth2UserService customOAuth2UserService,
                          OAuth2SuccessHandler oAuth2SuccessHandler,
                          JwtTokenProvider jwtTokenProvider,
                          CustomUserDetailsService customUserDetailsService) {
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ 인가(permitAll / authenticated)는 여기서만!
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/", "/login/**", "/drive",
                                "/oauth2/**", "/api/auth/**",
                                "/css/**", "/images/**", "/js/**",
                                "/favicon.ico", "/error"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // ✅ oauth2Login 설정은 여기서만!
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )

                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
