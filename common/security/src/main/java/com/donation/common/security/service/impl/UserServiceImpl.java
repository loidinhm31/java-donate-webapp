package com.donation.common.security.service.impl;

import com.donation.common.management.entity.UserAccount;
import com.donation.common.management.repository.UserAccountRepository;
import com.donation.common.security.CustomUserDetail;
import com.donation.common.security.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
	
	@Autowired
	private UserAccountRepository userAccountRepository;

	@Override
	public UserDetails loadUserByUsername(String username) {

		UserAccount user = userAccountRepository.findByUsername(username);

		if (user == null) {
			throw new UsernameNotFoundException(username);
		}
		return new CustomUserDetail(user);
	}
}
