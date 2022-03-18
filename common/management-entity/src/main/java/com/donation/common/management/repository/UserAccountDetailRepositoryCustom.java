package com.donation.common.management.repository;

import com.donation.common.management.repository.projection.UserAccountProjection;
import com.donation.management.model.UserFilter;
import org.springframework.data.domain.Page;

public interface UserAccountDetailRepositoryCustom {
    Page<UserAccountProjection> searchUsers(UserFilter userFilter);
}
