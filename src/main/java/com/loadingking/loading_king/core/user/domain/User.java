package com.loadingking.loading_king.core.user.domain;


import com.loadingking.loading_king.core.user.application.UserService;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;



@Builder
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    //소셜 로그인하기 때문에 필요없는 password는 제거

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "car_number")
    private String carNumber;

    @Column(name = "join_status")
    private String joinStatus;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;


    public User(Long id, String email, Role role, String carNumber, String joinStatus, LocalDateTime createdAt) {
        this.email = email;
        this.role = role;
        this.carNumber = carNumber;
        this.joinStatus = joinStatus;
        this.createdAt = createdAt;
    }

    public void updateInfo(String carNumber) {
        if (carNumber != null) {
            this.carNumber = carNumber;   // 소셜 로그인 , 우리 앱에서 수정할수있는건 차량번호만 가능

        }
    }
}
