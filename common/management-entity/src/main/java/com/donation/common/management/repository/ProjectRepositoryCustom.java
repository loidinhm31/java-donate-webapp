package com.donation.common.management.repository;

import com.donation.common.management.repository.projection.ProjectProjection;
import com.donation.management.model.ProjectSearchFilter;
import org.springframework.data.domain.Page;

public interface ProjectRepositoryCustom {
    Page<ProjectProjection> searchProjects(ProjectSearchFilter projectSearchFilter);
}
