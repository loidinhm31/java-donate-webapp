package com.donation.common.security;

import java.util.Collection;
import java.util.stream.Collectors;

import com.donation.common.management.entity.Role;
import com.donation.common.management.entity.UserAccount;
import com.donation.common.management.entity.UserAccountDetail;
import com.donation.common.management.entity.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;




public class CustomUserDetail implements UserDetails {
	
	private static final long serialVersionUID = 1L;
	
	private final UserAccount user;

	public CustomUserDetail(UserAccount theUser) {
		this.user = theUser;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<Role> userRoles = user.getUserRoles().stream()
				.map(UserRole::getRole)
				.collect(Collectors.toList());
		return userRoles.stream()
				.map(role -> new SimpleGrantedAuthority(role.getRoleCode().name()))
				.collect(Collectors.toList());
	}

	@Override
	public String getPassword() {

		// For not bypass oauth
		if (StringUtils.isNotEmpty(user.getUserPassword())) {
			return user.getUserPassword();
		}
		return null;
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	@Override
	public boolean isAccountNonExpired() {
		return Boolean.TRUE;
	}

	@Override
	public boolean isAccountNonLocked() {
		return Boolean.TRUE;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return Boolean.TRUE;
	}

	@Override
	public boolean isEnabled() {
		return user.getIsVerify() && user.getIsActive();
	}

	public UserAccountDetail getUserAccountDetails() {
		return this.user.getUserAccountDetail();
	}

	public UserAccount getUser() {
		return this.user;
	}
}
