package org.infoufps.infrastructure.account.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;
import org.infoufps.domain.account.Account;
import org.infoufps.domain.account.AccountSearchCriteria;
import org.infoufps.domain.account.AccountStatus;
import org.infoufps.domain.account.IAccountRepository;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.Role;
import org.infoufps.domain.shared.exceptions.EmailAlreadyExistsException;
import org.infoufps.domain.shared.pagination.PageRequest;
import org.infoufps.domain.shared.pagination.PageResult;
import org.infoufps.domain.shared.text.TextNormalizer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@ApplicationScoped
public class AccountRepositoryJpa implements IAccountRepository {

    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    @Inject
    private EntityManager entityManager;

    @Override
    @Transactional
    public Account save(Account account) {
        AccountJpa existing = entityManager.find(AccountJpa.class, account.getAccountId().value());
        if (existing != null) {
            existing.setName(account.getName());
            existing.setEmail(account.getEmail());
            existing.setPasswordHash(account.getPasswordHash());
            existing.setRole(account.getRole());
            existing.setStatus(account.getStatus());
            existing.setCreatedAt(account.getCreatedAt());
            flushTranslatingEmailConflict();
            return account;
        }
        AccountJpa accountJpa = toEntity(account);
        entityManager.persist(accountJpa);
        flushTranslatingEmailConflict();
        return account;
    }

    /**
     * Fuerza el flush para detectar de inmediato la violación del índice único
     * de email y traducirla a un conflicto de dominio (HTTP 409).
     */
    private void flushTranslatingEmailConflict() {
        try {
            entityManager.flush();
        } catch (PersistenceException exception) {
            if (isUniqueConstraintViolation(exception)) {
                throw new EmailAlreadyExistsException();
            }
            throw exception;
        }
    }

    private boolean isUniqueConstraintViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException sqlException
                    && UNIQUE_VIOLATION_SQL_STATE.equals(sqlException.getSQLState())) {
                return true;
            }
            if (current instanceof ConstraintViolationException constraintViolation
                    && UNIQUE_VIOLATION_SQL_STATE.equals(constraintViolation.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        AccountJpa accountJpa = entityManager
                .createQuery("SELECT a FROM AccountJpa a WHERE LOWER(a.email) = LOWER(:email)", AccountJpa.class)
                .setParameter("email", email)
                .getResultList().stream().findFirst().orElse(null);

        if (accountJpa == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(accountJpa));
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = entityManager
                .createQuery("SELECT COUNT(a) FROM AccountJpa a WHERE LOWER(a.email) = LOWER(:email)", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return count > 0;
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        AccountJpa accountJpa = entityManager
                .createQuery("SELECT a FROM AccountJpa a WHERE a.accountId = :id", AccountJpa.class)
                .setParameter("id", accountId.value())
                .getResultList().stream().findFirst().orElse(null);

        if (accountJpa == null) {
            return Optional.empty();
        }

        return Optional.of(toDomain(accountJpa));
    }

    @Override
    public List<Account> findByIds(Collection<AccountId> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return List.of();
        }
        List<String> ids = accountIds.stream().map(AccountId::value).toList();
        return entityManager
                .createQuery("SELECT a FROM AccountJpa a WHERE a.accountId IN :ids", AccountJpa.class)
                .setParameter("ids", ids)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PageResult<Account> search(AccountSearchCriteria criteria, PageRequest pageRequest) {
        AccountSearchCriteria effective = criteria != null
                ? criteria
                : new AccountSearchCriteria(null, null, null);
        PageRequest page = pageRequest != null ? pageRequest : PageRequest.first();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<AccountJpa> countRoot = countQuery.from(AccountJpa.class);
        countQuery.select(cb.count(countRoot))
                .where(buildPredicates(cb, countRoot, effective).toArray(new Predicate[0]));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        if (total == null || total == 0) {
            return PageResult.empty(page.page(), page.size());
        }

        CriteriaQuery<AccountJpa> selectQuery = cb.createQuery(AccountJpa.class);
        Root<AccountJpa> root = selectQuery.from(AccountJpa.class);
        selectQuery.select(root)
                .where(buildPredicates(cb, root, effective).toArray(new Predicate[0]))
                .orderBy(cb.asc(cb.lower(root.get("name"))), cb.asc(root.get("accountId")));

        TypedQuery<AccountJpa> query = entityManager.createQuery(selectQuery);
        query.setFirstResult(page.getOffset());
        query.setMaxResults(page.size());

        List<Account> items = query.getResultList().stream().map(this::toDomain).toList();
        return PageResult.of(items, page.page(), page.size(), total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<AccountJpa> root,
                                            AccountSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();

        if (criteria.role() != null) {
            predicates.add(cb.equal(root.get("role"), criteria.role()));
        }

        if (criteria.status() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.status()));
        }

        if (criteria.query() != null) {
            String pattern = "%" + TextNormalizer.escapeLikePattern(
                    criteria.query().toLowerCase(Locale.ROOT)) + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), pattern, '\\'),
                    cb.like(cb.lower(root.get("email")), pattern, '\\')
            ));
        }

        return predicates;
    }

    @Override
    public long countActiveAdminsExcluding(AccountId accountId) {
        Long count = entityManager
                .createQuery("SELECT COUNT(a) FROM AccountJpa a " +
                        "WHERE a.role = :admin AND a.status = :active AND a.accountId <> :id", Long.class)
                .setParameter("admin", Role.ADMIN)
                .setParameter("active", AccountStatus.ACTIVE)
                .setParameter("id", accountId.value())
                .getSingleResult();
        return count != null ? count : 0L;
    }

    @Override
    @Transactional
    public void deleteById(AccountId id){
        entityManager.createQuery(
                        "DELETE FROM AccountJpa cj WHERE cj.accountId = :id")
                .setParameter("id", id.value())
                .executeUpdate();
    }

    // ── Mapeo: Domain → JPA ──────────────────────────────────────────

    private AccountJpa toEntity(Account account) {
        AccountJpa entity = new AccountJpa();
        entity.setAccountId(account.getAccountId().value());
        entity.setName(account.getName());
        entity.setEmail(account.getEmail());
        entity.setPasswordHash(account.getPasswordHash());
        entity.setRole(account.getRole());
        entity.setStatus(account.getStatus());
        entity.setEmailVerified(account.isEmailVerified());
        entity.setCreatedAt(account.getCreatedAt());
        return entity;
    }

    // ── Mapeo: JPA → Domain ──────────────────────────────────────────

    private Account toDomain(AccountJpa accountJpa) {
        return Account.of(
                AccountId.of(accountJpa.getAccountId()),
                accountJpa.getName(),
                accountJpa.getEmail(),
                accountJpa.getPasswordHash(),
                accountJpa.getRole(),
                accountJpa.getStatus(),
                accountJpa.isEmailVerified(),
                accountJpa.getCreatedAt()
        );
    }

}
