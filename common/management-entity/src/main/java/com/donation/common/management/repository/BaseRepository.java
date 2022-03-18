package com.donation.common.management.repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.List;

public abstract class BaseRepository<T> {

    @PersistenceContext
    protected EntityManager em;

    public Long calculateCount(List<Predicate> predicates, Class<T> tClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> tRoot = cq.from(tClass);

        Predicate[] predicateArray = new Predicate[predicates.size()];
        predicates.toArray(predicateArray);

        cq.select(cb.count(tRoot));

        cq.where(predicateArray);

        Long count = em.createQuery(cq).getSingleResult();
        return count;
    }

    public Long countTotalWithKey(Class<T> tClass, String uniqueKey, Predicate[] whereFilter) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<T> tRoot = criteriaQuery.from(tClass);

        criteriaQuery
                .select(criteriaBuilder.countDistinct(tRoot.get(uniqueKey)))
                .where(whereFilter);

        return em.createQuery(criteriaQuery).getSingleResult();
    }


    public Path<T> getPath(Root<T> tRoot, String attributeName) {
        Path<T> path = tRoot;
        for (String part : attributeName.split("\\.")) {
            path = path.get(part);
        }
        return path;
    }
}
