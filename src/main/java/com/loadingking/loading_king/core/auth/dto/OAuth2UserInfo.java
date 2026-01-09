package com.loadingking.loading_king.core.auth.dto;

import java.util.Map;

public interface OAuth2UserInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getName();
    Map<String, Object> getAttributes();
}
