package com.donation.common.management.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;


@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "user_account", schema = DONATION_SCHEMA)
public class UserAccount {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "user_id", unique = true, nullable = false)
	private UUID userId;
	
	@Column(name = "username", unique = true, nullable = false)
	private String username;
	
	@Column(name = "password")
	private String userPassword;

	@Column(name = "is_verify", nullable = false)
	private Boolean isVerify;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;
	
	@OneToMany(mappedBy = "userAccount", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<UserRole> userRoles = new ArrayList<>();

	@OneToOne(mappedBy = "userAccount", cascade = CascadeType.ALL)
	@PrimaryKeyJoinColumn
	private UserAccountDetail userAccountDetail;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
