package com.loadingking.loading_king.infra.security.jwt;

import com.loadingking.loading_king.core.user.domain.User;
import com.loadingking.loading_king.core.user.userRepository.UserRepository;
import com.loadingking.loading_king.infra.security.CustomUserDetail;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Collections;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMilliseconds;
    private final UserRepository userRepository;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenValidityInMilliseconds,
            UserRepository userRepository) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;
        this.userRepository = userRepository;
    }

    // 토큰 생성
    public String createAccessToken(Authentication authentication) {

        //인증된 사용자 정보(Principal) 가져오기
        CustomUserDetail userPrincipal = (CustomUserDetail) authentication.getPrincipal();
        Long userId = userPrincipal.getUser().getId();

        //권한 정보
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))  // 토큰 제목에 User ID(PK) 저장
                .claim("auth", authorities)        //권한 정보 "auth"라는 키로 저장
                .setIssuedAt(now)                       //발생 시간
                .setExpiration(validity)                //만료 시간
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        // 토큰에 담긴 유저 ID 가져오기
        Long userId = Long.valueOf(claims.getSubject());

        // [수정] DB에서 진짜 유저 정보 조회 (없으면 예외 발생)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // [수정] 조회한 유저 정보로 CustomUserDetail 생성
        CustomUserDetail customUserDetail = new CustomUserDetail(user, Collections.emptyMap());

        // [수정] Authentication 객체에 CustomUserDetail을 담아서 반환
        return new UsernamePasswordAuthenticationToken(customUserDetail, token, customUserDetail.getAuthorities());
    }


    public boolean validateToken(String token) {

        try{
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {

       try{
           return Jwts.parserBuilder()
                   .setSigningKey(key)
                   .build()
                   .parseClaimsJws(accessToken)
                   .getBody();
       } catch (ExpiredJwtException e){
           return e.getClaims();
       }
    }

}