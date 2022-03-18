package com.donation.common.management.repository.impl;

import com.donation.common.core.enums.RoleEnum;
import com.donation.common.core.enums.SortType;
import com.donation.common.management.entity.*;
import com.donation.common.management.repository.BaseRepository;
import com.donation.common.management.repository.UserAccountDetailRepositoryCustom;
import com.donation.common.management.repository.projection.UserAccountProjection;
import com.donation.management.model.UserFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.donation.common.core.constant.Constants.USER_NOT_LOGIN;

@Slf4j
@Repository
public class UserAccountDetailRepositoryImpl extends BaseRepository<UserAccountDetail>
    implements UserAccountDetailRepositoryCustom {

    @Override
    public Page<UserAccountProjection> searchUsers(UserFilter userFilter) {

        List<RoleEnum> roleEnums = userFilter.getRoleEnums();
        String searchKey = userFilter.getSearchKey();
        String option = userFilter.getOption();

        Pageable pageable = PageRequest.of(userFilter.getStartPage(), userFilter.getPageSize());

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<UserAccountProjection> criteriaQuery = criteriaBuilder.createQuery(UserAccountProjection.class);

        Root<UserAccountDetail> userAccountDetailRoot = criteriaQuery.from(UserAccountDetail.class);

        Join<UserAccountDetail, UserAccount> userAccountJoin =
                userAccountDetailRoot.join(UserAccountDetail_.userAccount, JoinType.INNER);

        ListJoin<UserAccount, UserRole> userRoleJoin =
                userAccountJoin.join(UserAccount_.userRoles, JoinType.LEFT);

        Join<UserRole, Role> roleJoin =
                userRoleJoin.join(UserRole_.role, JoinType.LEFT);


        List<Predicate> predicates = buildQueryCondition(
                userFilter, roleEnums,
                userAccountDetailRoot, userAccountJoin,
                roleJoin,
                searchKey, option,
                criteriaBuilder);

        List<Order> orders = new ArrayList<>();

        Expression<Object> sortColumn;
        if (Objects.nonNull(userFilter.getSortBy())) {
            switch (userFilter.getSortBy()) {
                case EMAIL:
                    sortColumn = userAccountDetailRoot.get(UserAccountDetail_.EMAIL);
                    break;
                case FIRST_NAME:
                    sortColumn = userAccountDetailRoot.get(UserAccountDetail_.FIRST_NAME);
                    break;
                default:
                    sortColumn = userAccountJoin.get(UserAccount_.USERNAME);
            }
        } else {
            sortColumn = userAccountJoin.get(UserAccount_.USERNAME);
        }

        orders.add(SortType.DESC.equals(userFilter.getSortType()) ? criteriaBuilder.desc(sortColumn) : criteriaBuilder.asc(sortColumn));

        // query: count total
        criteriaQuery.multiselect(criteriaBuilder.countDistinct(userAccountDetailRoot.get(UserAccountDetail_.USER_ID)).alias("countTotal"))
                .where(predicates.toArray(new Predicate[0]));
        long countTotal = em.createQuery(criteriaQuery).getSingleResult().getCountTotal();

        // query: user data
        criteriaQuery
                .distinct(true)
                .multiselect(
                        userAccountDetailRoot.get(UserAccountDetail_.USER_ID),
                        userAccountJoin.get(UserAccount_.USERNAME),
                        userAccountDetailRoot.get(UserAccountDetail_.EMAIL),
                        userAccountDetailRoot.get(UserAccountDetail_.FIRST_NAME),
                        userAccountDetailRoot.get(UserAccountDetail_.LAST_NAME),
                        userAccountDetailRoot.get(UserAccountDetail_.BIRTHDATE),
                        userAccountDetailRoot.get(UserAccountDetail_.PHONE_NUMBER),
                        userAccountDetailRoot.get(UserAccountDetail_.LASTED_LOGIN_TIME),
                        userAccountJoin.get(UserAccount_.IS_ACTIVE),
                        userAccountJoin.get(UserAccount_.IS_VERIFY),
                        userAccountDetailRoot.get(UserAccountDetail_.AUTHENTICATION_TYPE))
                .where(predicates.toArray(new Predicate[0]))
                .orderBy(orders);

        // result list
        List<UserAccountProjection> results = em.createQuery(criteriaQuery)
                .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
        return new PageImpl<>(results, pageable, countTotal);
    }

    private List<Predicate> buildQueryCondition(UserFilter userFilter,
                                                List<RoleEnum> roleEnums,
                                                Root<UserAccountDetail> userAccountDetailRoot,
                                                Join<UserAccountDetail, UserAccount> userAccountJoin,
                                                Join<UserRole, Role> roleJoin,
                                                String searchKey, String option,
                                                CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(roleEnums)) {
            predicates.add(roleJoin.get(Role_.roleCode).in(roleEnums));
        }

        if (Objects.nonNull(userFilter.getIsActive())) {
            predicates.add(criteriaBuilder.equal(userAccountJoin.get(UserAccount_.IS_ACTIVE), userFilter.getIsActive()));
        }

        // Filter by search key on some fields
        if (StringUtils.isNotEmpty(searchKey)) {
            Predicate compareWithEmail =
                    criteriaBuilder.like(criteriaBuilder.lower(userAccountDetailRoot.get(UserAccountDetail_.EMAIL)),
                            "%"  + searchKey.toLowerCase() + "%");

            Predicate compareWithPhone =
                    criteriaBuilder.like(criteriaBuilder.lower(userAccountDetailRoot.get(UserAccountDetail_.PHONE_NUMBER)),
                            "%"  + searchKey.toLowerCase() + "%");

            predicates.add(criteriaBuilder.or(
                    compareWithEmail,
                    compareWithPhone));
        }

        // Filter by lasted login time
        if (StringUtils.isNotEmpty(option)) {
            Predicate compareDateInterval = null;
            if (option.equalsIgnoreCase(USER_NOT_LOGIN)) {
                compareDateInterval =
                        criteriaBuilder.isNull(userAccountDetailRoot.get(UserAccountDetail_.LASTED_LOGIN_TIME));
            } else {
                try {
                    int dateInterval = Integer.parseInt(option);
                    LocalDate currentDate = LocalDate.now();
                    Date dateCompare = convertToDateViaInstant(currentDate.minusDays(dateInterval));
                    compareDateInterval =
                            criteriaBuilder.lessThanOrEqualTo(userAccountDetailRoot.get(UserAccountDetail_.LASTED_LOGIN_TIME),
                                    dateCompare);
                } catch (NumberFormatException e) {
                    log.error("Cannot parse date", e);
                }
            }
            
            if (Objects.nonNull(compareDateInterval)) {
                predicates.add(compareDateInterval);
            }
        }

        if (Objects.nonNull(userFilter.getIsVerify())) {
            predicates.add(criteriaBuilder.equal(userAccountJoin.get(UserAccount_.IS_VERIFY), userFilter.getIsVerify()));
        }

        return predicates;
    }

    public Date convertToDateViaInstant(LocalDate dateToConvert) {
        return java.util.Date.from(dateToConvert.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }
}
