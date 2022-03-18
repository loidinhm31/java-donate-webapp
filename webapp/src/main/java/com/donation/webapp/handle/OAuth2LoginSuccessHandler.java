package com.donation.webapp.handle;

import com.cloudinary.utils.StringUtils;
import com.donation.common.core.enums.AuthenticationType;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.security.CustomOAuth2User;
import com.donation.data.handler.service.UserAccountDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {


	@Autowired
	private UserAccountDetailsService userAccountDetailsService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws ServletException, IOException {

		CustomOAuth2User oauth2User = (CustomOAuth2User) authentication.getPrincipal();
		
		String name = oauth2User.getName();
		String email = oauth2User.getEmail();
		String clientName = oauth2User.getClientName();
		
		AuthenticationType authenticationType = getAuthenticationType(clientName);
		
		UserAccountDetail userAccountDetail = userAccountDetailsService.findUserAccountDetailByEmail(email);
		if (Objects.isNull(userAccountDetail)) {
			userAccountDetailsService.createUserByOAuthLogin(name, email, authenticationType);
		} else {
			oauth2User.setFullName(userAccountDetail.getLastName() + StringUtils.EMPTY + userAccountDetail.getFirstName());
			userAccountDetailsService.updateAuthenticationType(userAccountDetail, authenticationType);
		}
		super.onAuthenticationSuccess(request, response, authentication);
	}
	
	private AuthenticationType getAuthenticationType(String clientName) {
		if (clientName.equals("Google")) {
			return AuthenticationType.GOOGLE;
		} else if (clientName.equals("Facebook")) {
			return AuthenticationType.FACEBOOK;
		} else {
			return AuthenticationType.BASIC;
		}
	}

}
