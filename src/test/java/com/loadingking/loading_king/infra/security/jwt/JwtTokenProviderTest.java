package com.loadingking.loading_king.infra.security.jwt;

import com.loadingking.loading_king.core.user.domain.Role;
import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.userRepository.UserRepository;
import com.loadingking.loading_king.infra.security.CustomUserDetail;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String secretKey;

    @Test
    @DisplayName("토큰 생성 및 검증 : 정상 토큰 통과해야 한다.")
    void createdAndValidateToken() {
       //  Given
       User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .role(Role.USER)
                .build();

        CustomUserDetail userDetail = new CustomUserDetail(user, Collections.emptyMap());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetail, "", userDetail.getAuthorities());
        //When
        String token = jwtTokenProvider.createAccessToken(authentication);
        //Then
        System.out.println("Generated Token: " + token);
        assertThat(token).isNotNull();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }



    @Test
    @DisplayName("토큰 검증 : 만료된 토큰은 통과하지 못해야 한다.")
    void validateToken_Fail_Expired() {
        //Given
        Date past = new Date(new Date().getTime() - 1000);
        String expiredToken = Jwts.builder()
                .setSubject("1")
                .setExpiration(past)
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), SignatureAlgorithm.HS256)
                .compact();
        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();

    }
}