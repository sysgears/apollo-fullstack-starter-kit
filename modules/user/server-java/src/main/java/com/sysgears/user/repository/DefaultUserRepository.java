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
public class DefaultUserRepository implements UserRepository {
    private final EntityManager entityManager;
    private final JpaUserRepository jpaUserRepository;

    @Override
    public Optional<User> findById(int id) {
        return jpaUserRepository.findById(id);
    }

    @Override
    public CompletableFuture<User> findUserById(int id) {
        return jpaUserRepository.findUserById(id);
    }

    @Override
    public User save(User user) {
        return jpaUserRepository.save(user);
    }

    @Override
    public void delete(User user) {
        jpaUserRepository.delete(user);
    }

    @Override
    public CompletableFuture<List<User>> findByCriteria(Optional<OrderByUserInput> orderBy, Optional<FilterUserInput> filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> query = builder.createQuery(User.class);

        Root<User> user = query.from(User.class);
        List<Predicate> predicates = new ArrayList<>();

        filter.ifPresent(filterUserInput -> {
            filterUserInput.getRole().ifPresent(role -> predicates.add(builder.like(user.get("role"), role)));
            filterUserInput.getIsActive().ifPresent(isActive -> predicates.add(builder.equal(user.get("isActive"), isActive)));
            filterUserInput.getSearchText().ifPresent(searchText -> {
                predicates.add(builder.or((builder.like(user.get("username"), searchText)),
                        builder.like(user.get("email"), searchText)));
            });
        });

        orderBy.ifPresent(orderByUserInput -> {
            String orderColumn = orderByUserInput.getColumn().orElse("id");
            if (orderByUserInput.getOrder().isPresent() && orderByUserInput.getOrder().get().toLowerCase().equals("desc")) {
                query.orderBy(builder.desc(user.get(orderColumn)));
            } else {
                query.orderBy(builder.asc(user.get(orderColumn)));
            }
        });
        query.where(predicates.toArray(new Predicate[0]));

        return CompletableFuture.supplyAsync(() -> entityManager.createQuery(query).getResultList());
    }
}