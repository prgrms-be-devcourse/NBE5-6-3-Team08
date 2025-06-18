package com.grepp.datenow.infra.auth.oauth2.user;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfo {
    
    String getProviderId();
    String getProvider();
    String getName();
    
    static OAuth2UserInfo create(String path, OAuth2User user) {
        if(path.equals("/login/oauth2/code/github"))
            return new GithubOAuth2UserInfo(user.getAttributes());
    
        throw new IllegalArgumentException("지원하지 않는 인증서버 입니다.");
    }

}
