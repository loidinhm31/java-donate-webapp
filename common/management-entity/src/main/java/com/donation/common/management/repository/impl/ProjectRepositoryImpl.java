package com.donation.common.management.repository.impl;

import com.donation.common.core.enums.ProjectStatusEnum;
import com.donation.common.core.enums.SortType;
import com.donation.common.management.entity.*;
import com.donation.common.management.repository.BaseRepository;
import com.donation.common.management.repository.ProjectRepositoryCustom;
import com.donation.common.management.repository.projection.ProjectProjection;
import com.donation.management.model.ProjectSearchFilter;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProjectRepositoryImpl extends BaseRepository<Project>
    implements ProjectRepositoryCustom {

    @Override
    public Page<ProjectProjection> searchProjects(ProjectSearchFilter projectSearchFilter) {


        List<String> phoneNumbers = new ArrayList<>();
        List<String> projectCodes = new ArrayList<>();
        List<String> projectStatuses = new ArrayList<>();

        if (Objects.nonNull(projectSearchFilter.getFilters())) {
            phoneNumbers = projectSearchFilter.getFilters().getPhoneNumbers();
            projectCodes = projectSearchFilter.getFilters().getProjectCodes();
            projectStatuses = projectSearchFilter.getFilters().getProjectStatuses();
        }

        Pageable pageable = PageRequest.of(projectSearchFilter.getStartPage(), projectSearchFilter.getPageSize());

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<ProjectProjection> criteriaQuery = criteriaBuilder.createQuery(ProjectProjection.class);

        Root<Project> projectRoot = criteriaQuery.from(Project.class);

        Join<Project, Beneficiary> projectBeneficiaryJoin =
                projectRoot.join(Project_.beneficiary, JoinType.INNER);

        List<Predicate> predicates = buildQueryCondition(
                projectRoot, projectBeneficiaryJoin,
                phoneNumbers, projectCodes, projectStatuses,
                criteriaBuilder);

        List<Order> orders = new ArrayList<>();

        Expression<Object> sortColumn;
        if (Objects.nonNull(projectSearchFilter.getSortBy())) {
            switch (projectSearchFilter.getSortBy()) {
                case PROJECT_NAME:
                    sortColumn = projectRoot.get(Project_.PROJECT_NAME);
                    break;
                case TARGET_MONEY:
                    sortColumn = projectRoot.get(Project_.TARGET_MONEY);
                    break;
                case CREATED_AT:
                    sortColumn = projectRoot.get(Project_.CREATED_AT);
                    break;
                case UPDATED_AT:
                    sortColumn = projectRoot.get(Project_.UPDATED_AT);
                    break;
                default:
                    sortColumn = projectRoot.get(Project_.PROJECT_ID);
            }
        } else {
            sortColumn = projectRoot.get(Project_.PROJECT_ID);
        }

        orders.add(SortType.DESC.equals(projectSearchFilter.getSortType()) ? criteriaBuilder.desc(sortColumn) : criteriaBuilder.asc(sortColumn));


        // query: count total
        criteriaQuery.multiselect(criteriaBuilder.countDistinct(projectRoot.get(Project_.PROJECT_ID)).alias("countTotal"))
                .where(predicates.toArray(new Predicate[0]));
        long countTotal = em.createQuery(criteriaQuery).getSingleResult().getCountTotal();

        // query data
        criteriaQuery.distinct(true)
                .multiselect(
                        projectRoot.get(Project_.PROJECT_ID),
                        projectRoot.get(Project_.PROJECT_NAME),
                        projectRoot.get(Project_.PROJECT_SUMMARY),
                        projectRoot.get(Project_.PROJECT_CONTENT),
                        projectRoot.get(Project_.STATUS),
                        projectRoot.get(Project_.START_TIME),
                        projectRoot.get(Project_.TARGET_TIME),
                        projectRoot.get(Project_.TARGET_MONEY),
                        projectRoot.get(Project_.CURRENT_MONEY),
                        projectRoot.get(Project_.COUNT_EXTEND),
                        projectBeneficiaryJoin.get(Beneficiary_.BENEFICIARY_PHONE_NUMBER),
                        projectBeneficiaryJoin.get(Beneficiary_.BENEFICIARY_NAME),
                        projectBeneficiaryJoin.get(Beneficiary_.BENEFICIARY_TYPE),
                        projectRoot.get(Project_.CREATED_AT),
                        projectRoot.get(Project_.UPDATED_AT)
                )
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(orders);

        // result list
        List<ProjectProjection> results = em.createQuery(criteriaQuery)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        return new PageImpl<>(results, pageable, countTotal);
    }

    private List<Predicate> buildQueryCondition(Root<Project> projectRoot, Join<Project, Beneficiary> projectBeneficiaryJoin,
                                                List<String> phoneNumbers, List<String> projectCodes, List<String> projectStatuses,
                                                CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(phoneNumbers)) {
            List<Predicate> orPhonePredicates = new ArrayList<>();
            for (String phone : phoneNumbers) {
                Predicate comparePhone =
                        criteriaBuilder.like(projectBeneficiaryJoin.get(Beneficiary_.BENEFICIARY_PHONE_NUMBER),
                                "%"  + phone + "%");
                orPhonePredicates.add(comparePhone);
            }

            predicates.add(criteriaBuilder.or(orPhonePredicates.toArray(new Predicate[0])));
        }

        if (CollectionUtils.isNotEmpty(projectCodes)) {
            Predicate projectIdPredicate =
                    criteriaBuilder.in(projectRoot.get(Project_.PROJECT_ID)).value(projectCodes);

            predicates.add(projectIdPredicate);
        }

        if (CollectionUtils.isNotEmpty(projectStatuses)) {
            List<ProjectStatusEnum> statusEnums = projectStatuses.stream()
                    .map(ProjectStatusEnum::getEnum)
                    .collect(Collectors.toList());
            Predicate statusPredicate =
                    criteriaBuilder.in(projectRoot.get(Project_.STATUS)).value(statusEnums);

            predicates.add(statusPredicate);
        }

        return predicates;
    }
}
