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
           "(LOWER(v.aliasName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.applicationUrl) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(v.username) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<VaultCredential> searchUserCredentials(@Param("user") User user, @Param("query") String query);
}
