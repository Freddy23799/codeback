package codeqr.code.repository;

import codeqr.code.model.Session;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
public class SessionRepositoryCustomsImpl implements SessionRepositoryCustoms {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Session> findByUserIdWithSearch(Long userId, String search, LocalDate dateIso, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Session> cq = cb.createQuery(Session.class);
        Root<Session> session = cq.from(Session.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtrer par userId
        if (userId != null) {
            predicates.add(cb.equal(session.get("user").get("id"), userId));
        }

        // Filtrer par texte libre (dans plusieurs champs possibles)
        if (search != null && !search.isEmpty()) {
            String likePattern = "%" + search.toLowerCase() + "%";
            predicates.add(
                cb.or(
                    cb.like(cb.lower(session.get("course").get("name")), likePattern),
                    cb.like(cb.lower(session.get("room").get("name")), likePattern),
                    cb.like(cb.lower(session.get("campus").get("name")), likePattern),
                    cb.like(cb.lower(session.get("level").get("name")), likePattern),
                    cb.like(cb.lower(session.get("specialty").get("name")), likePattern)
                )
            );
        }

        // Filtrer par date
        if (dateIso != null) {
            predicates.add(cb.greaterThanOrEqualTo(session.get("startDate"), dateIso));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(session.get("startDate"))); // exemple : trier par date descendante

        TypedQuery<Session> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // Compter le total pour la pagination
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Session> countRoot = countQuery.from(Session.class);
        countQuery.select(cb.count(countRoot)).where(predicates.toArray(new Predicate[0]));
        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }
}
