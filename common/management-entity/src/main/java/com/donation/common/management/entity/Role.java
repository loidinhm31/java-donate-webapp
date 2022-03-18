package com.donation.common.management.entity;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.management.converter.RoleEnumConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.donation.common.management.SchemaConstant.DONATION_SCHEMA;

@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
@Entity
@Table(name="role", schema = DONATION_SCHEMA)
public class Role extends BaseEntity<String> {
	
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "role_id")
	private UUID roleId;

	@Column(name = "role_code", unique = true, nullable = false)
	@Convert(converter = RoleEnumConverter.class)
	private RoleEnum roleCode;

	@Column(name = "role_name")
	private String roleName;

	@OneToMany(mappedBy = "role", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<UserRole> userRoles = new ArrayList<>();
}
