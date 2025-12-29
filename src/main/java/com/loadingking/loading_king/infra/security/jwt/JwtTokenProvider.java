package com.loadingking.loading_king.infra.security.jwt;

import com.loadingking.loading_king.core.user.repository.UserRepository;
import com.loadingking.loading_king.infra.security.CustomUserDetail;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long accessTokenValidityInMilliseconds;


    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenValidityInMilliseconds,
            UserRepository userRepository) {

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds;

    }

    // 토큰 생성
    public String createAccessToken(Long userId, String role) {

        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))  // 토큰 제목에 User ID(PK) 저장
                .claim("auth", role)        //권한 정보 "auth"라는 키로 저장
                .setIssuedAt(now)                       //발생 시간
                .setExpiration(validity)                //만료 시간
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String createAccessToken(Authentication authentication) {
        CustomUserDetail userPrincipal = (CustomUserDetail) authentication.getPrincipal();
        // 권한들 꺼내서 문자열로 변환 (예: "ROLE_USER,ROLE_ADMIN")
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 핵심 메서드 호출
        return createAccessToken(userPrincipal.getUser().getId(), authorities);
    }

    //메서드 오버로딩 (같은 메서드명 사용)
    public String createRefreshToken(Long userId) {
        // Access Token 생성 로직과 비슷하지만, 유효기간만 길게(예: 7일) 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + (7 * 24 * 60 * 60 * 1000)); // 7일

        return Jwts.builder()
                .setSubject(String.valueOf(userId)) // ID를 Subject로 저장
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    public String createRefreshToken(Authentication authentication) {
        CustomUserDetail userPrincipal = (CustomUserDetail) authentication.getPrincipal();

        return createRefreshToken(userPrincipal.getUser().getId());
    }


    public long getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        long now = new Date().getTime();
        return (expiration.getTime() - now);
    }
    public String getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
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

    public Claims parseClaims(String accessToken) {

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