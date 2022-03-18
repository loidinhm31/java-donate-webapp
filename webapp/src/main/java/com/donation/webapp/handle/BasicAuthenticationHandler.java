package com.donation.webapp.handle;

import com.donation.common.core.enums.AuthenticationType;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.security.CustomUserDetail;
import com.donation.data.handler.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class BasicAuthenticationHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserAccountDetailsService userAccountDetailsService;

    @Autowired
    private UrlAuthenticationSuccessHandler urlAuthenticationSuccessHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUserDetail userDetails = (CustomUserDetail) authentication.getPrincipal();
        UserAccountDetail userAccountDetail = userDetails.getUserAccountDetails();

        userAccountDetailsService.updateAuthenticationType(userAccountDetail, AuthenticationType.BASIC);

        urlAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
    }
}
