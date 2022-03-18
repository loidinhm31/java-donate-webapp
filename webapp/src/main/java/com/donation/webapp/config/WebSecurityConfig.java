package com.donation.webapp.config;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.security.CustomOAuth2UserService;
import com.donation.common.security.service.UserService;
import com.donation.webapp.handle.BasicAuthenticationHandler;
import com.donation.webapp.handle.OAuth2LoginSuccessHandler;
import com.donation.webapp.handle.UrlAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private UserService userService;

	@Autowired
	private BasicAuthenticationHandler basicAuthenticationHandler;

	@Autowired private CustomOAuth2UserService oAuth2UserService;

	@Autowired private OAuth2LoginSuccessHandler oauth2LoginHandler;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(authenticationProvider());
	}
	
	@Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/resources/**");
    }
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		
		http.authorizeRequests()
			.antMatchers("/admin/**").hasAuthority(RoleEnum.ADMIN.name())
			.antMatchers("/manage/**").hasAuthority(RoleEnum.ADMIN.name())
			.antMatchers("/user-detail/**").authenticated()
			.antMatchers("/**").permitAll()
			.and()
			.formLogin()
				.loginPage("/login")
				.usernameParameter("username")
				.successHandler(basicAuthenticationHandler)
				.permitAll()
			.and()
			.oauth2Login()
				.loginPage("/login")
				.userInfoEndpoint()
				.userService(oAuth2UserService)
				.and()
				.successHandler(oauth2LoginHandler)
			.and()
			.logout()
				.logoutUrl("/logout")
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
				.clearAuthentication(true)
				.invalidateHttpSession(true)
			.and()
				.rememberMe()
				.key("1234567890_aBcDeFgHiJkLmNoPqRsTuVwXyZ")
				.tokenValiditySeconds(14 * 24 * 60 * 60)
			.and()
				.exceptionHandling().accessDeniedPage("/access-denied");
	}
	
	// beans
	// bcrypt bean definition
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// authenticationProvider bean definition
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
		auth.setUserDetailsService(userService); 	// set the custom user details
		auth.setPasswordEncoder(passwordEncoder()); // set the password encoder - bcrypt
		return auth;
	}

	// authentication success redirect different page
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler(){
	    return new UrlAuthenticationSuccessHandler();
	}

}
