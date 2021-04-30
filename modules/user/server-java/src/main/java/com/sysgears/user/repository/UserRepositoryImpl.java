package com.sysgears.user.repository;

import com.sysgears.user.dto.input.FilterUserInput;
import com.sysgears.user.dto.input.OrderByUserInput;
import com.sysgears.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements CustomUserRepository {
    private final EntityManager entityManager;

    @Override
    public CompletableFuture<List<User>> findByCriteria(Optional<OrderByUserInput> orderBy, Optional<FilterUserInput> filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);

        Root<User> user = query.from(User.class);
        List<Predicate> predicates = new ArrayList<>();

        filter.ifPresent(filterUserInput -> {
            filterUserInput.getRole().filter(s -> !s.isBlank())
                    .ifPresent(role -> predicates.add(builder.like(user.get("role"), role)));
            filterUserInput.getIsActive()
                    .ifPresent(isActive -> predicates.add(builder.equal(user.get("isActive"), isActive)));
            filterUserInput.getSearchText().filter(s -> !s.isBlank())
                    .ifPresent(searchText -> predicates.add(builder.or(
                            builder.like(user.get("username"), searchText),
                            builder.like(user.get("email"), searchText)
                    )));
        });

        orderBy.ifPresent(orderByUserInput -> {
            String orderColumn = orderByUserInput.getColumn()
                    .filter(s -> !s.isBlank())
                    .orElse("id");
            if (orderByUserInput.getOrder().filter(s -> !s.isBlank()).isPresent() && orderByUserInput.getOrder().get().toLowerCase().equals("desc")) {
                query.orderBy(builder.desc(user.get(orderColumn)));
            } else {
                query.orderBy(builder.asc(user.get(orderColumn)));
            }
        });
        query.where(predicates.toArray(new Predicate[0]));

        return CompletableFuture.supplyAsync(() -> entityManager.createQuery(query).getResultList());
    }

    @Override
    public CompletableFuture<User> findByUsernameOrEmail(String usernameOrEmail) {
        return CompletableFuture.supplyAsync(() -> {
            CriteriaBuilder builder = entityManager.getCriteriaBuilder();

            CriteriaQuery<User> query = builder.createQuery(User.class);

            Root<User> user = query.from(User.class);

            query.where(builder.or(
                    builder.equal(user.get("username"), usernameOrEmail),
                    builder.equal(user.get("email"), usernameOrEmail)
            ));

            return entityManager.createQuery(query).getSingleResult();
        });
    }
}
