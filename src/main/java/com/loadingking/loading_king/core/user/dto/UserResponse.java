package com.loadingking.loading_king.core.user.dto;


import com.loadingking.loading_king.core.user.domain.User;
import lombok.Getter;

@Getter
public class UserResponse {

    private final Long id;
    private final String email;
    private final String carNumber;
    private final String role;
    private final String joinStatus;

   public UserResponse(User user) {

    this.id = user.getId();
    this.email = user.getEmail();
    this.carNumber = user.getCarNumber();
    this.role = user.getRole().name();
    this.joinStatus = user.getJoinStatus();

   }

}
