package com.donation.management.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class ProvideRoleRequest {
    List<UUID> users;

    Boolean active;
}
