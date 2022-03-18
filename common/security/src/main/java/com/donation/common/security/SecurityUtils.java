package com.donation.common.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Objects;

public class SecurityUtils {
    private SecurityUtils() {
        // This is security util class
    }

    public static String getAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String idToken = StringUtils.EMPTY;
        if (authentication.getPrincipal() instanceof OidcUser) {
            OidcUser principal = ((OidcUser) authentication.getPrincipal());
            idToken = principal.getIdToken().getTokenValue();
        }
        return idToken;
    }

    public static String getUserIdentifier() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdentifier = StringUtils.EMPTY;
        if (Objects.nonNull(authentication)) {
            if (authentication.getPrincipal() instanceof CustomOAuth2User) {
                CustomOAuth2User user = (CustomOAuth2User) authentication.getPrincipal();
                userIdentifier = user.getAttributes().get("email").toString();
            } else if (authentication.getPrincipal() instanceof CustomUserDetail) {
                CustomUserDetail user = (CustomUserDetail) authentication.getPrincipal();
                userIdentifier = user.getUsername();
            }
        }
        return userIdentifier;
    }

}
