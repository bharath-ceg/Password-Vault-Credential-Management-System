package com.securevault.repository;

import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import com.securevault.entity.enums.CredentialCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaultCredentialRepository extends JpaRepository<VaultCredential, Long> {
    List<VaultCredential> findByUserOrderByCreatedAtDesc(User user);
    List<VaultCredential> findByUserAndCategoryOrderByCreatedAtDesc(User user, CredentialCategory category);
    Optional<VaultCredential> findByIdAndUser(Long id, User user);

    @Query("SELECT v FROM VaultCredential v WHERE v.user = :user AND " +
           "(LOWER(v.aliasName) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR " +
           "LOWER(v.applicationUrl) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')) OR " +
           "LOWER(v.username) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))")
    List<VaultCredential> searchUserCredentials(@Param("user") User user, @Param("query") String query);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE VaultCredential c SET c.updatedAt = :updatedAt, c.createdAt = :createdAt WHERE c.id = :id")
    void updateTimestamps(@Param("id") Long id, @Param("updatedAt") java.time.ZonedDateTime updatedAt, @Param("createdAt") java.time.ZonedDateTime createdAt);
}
