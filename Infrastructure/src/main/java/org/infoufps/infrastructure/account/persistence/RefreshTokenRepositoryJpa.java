package org.infoufps.infrastructure.account.persistence;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.infoufps.domain.account.AccountId;
import org.infoufps.domain.account.IRefreshTokenRepository;
import org.infoufps.domain.account.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class RefreshTokenRepositoryJpa implements IRefreshTokenRepository {

    @Inject
    private EntityManager entityManager;

    @Override
    @Transactional
    public RefreshToken save(RefreshToken token) {
        RefreshTokenJpa existing = entityManager.find(RefreshTokenJpa.class, token.getTokenId());
        if (existing != null) {
            existing.setRevokedAt(token.getRevokedAt());
            return token;
        }
        entityManager.persist(toEntity(token));
        return token;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            return Optional.empty();
        }
        return entityManager
                .createQuery("SELECT t FROM RefreshTokenJpa t WHERE t.tokenHash = :hash", RefreshTokenJpa.class)
                .setParameter("hash", tokenHash)
                .getResultList()
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public boolean revokeIfActive(String tokenId, LocalDateTime revokedAt) {
        int updated = entityManager
                .createQuery("UPDATE RefreshTokenJpa t SET t.revokedAt = :revokedAt " +
                        "WHERE t.tokenId = :tokenId AND t.revokedAt IS NULL")
                .setParameter("revokedAt", revokedAt)
                .setParameter("tokenId", tokenId)
                .executeUpdate();
        return updated > 0;
    }

    @Override
    @Transactional
    public void revokeAllByAccountId(AccountId accountId, LocalDateTime revokedAt) {
        entityManager
                .createQuery("UPDATE RefreshTokenJpa t SET t.revokedAt = :revokedAt " +
                        "WHERE t.accountId = :accountId AND t.revokedAt IS NULL")
                .setParameter("revokedAt", revokedAt)
                .setParameter("accountId", accountId.value())
                .executeUpdate();
    }

    private RefreshTokenJpa toEntity(RefreshToken token) {
        RefreshTokenJpa entity = new RefreshTokenJpa();
        entity.setTokenId(token.getTokenId());
        entity.setAccountId(token.getAccountId().value());
        entity.setTokenHash(token.getTokenHash());
        entity.setCreatedAt(token.getCreatedAt());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setRevokedAt(token.getRevokedAt());
        return entity;
    }

    private RefreshToken toDomain(RefreshTokenJpa entity) {
        return RefreshToken.of(
                entity.getTokenId(),
                AccountId.of(entity.getAccountId()),
                entity.getTokenHash(),
                entity.getCreatedAt(),
                entity.getExpiresAt(),
                entity.getRevokedAt()
        );
    }
}
