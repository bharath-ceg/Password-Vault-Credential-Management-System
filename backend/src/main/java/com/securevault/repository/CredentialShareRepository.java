package com.securevault.repository;

import com.securevault.entity.CredentialShare;
import com.securevault.entity.User;
import com.securevault.entity.VaultCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialShareRepository extends JpaRepository<CredentialShare, Long> {

    List<CredentialShare> findByOwnerOrderByCreatedAtDesc(User owner);

    List<CredentialShare> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<CredentialShare> findByCredential(VaultCredential credential);

    Optional<CredentialShare> findByCredentialAndRecipient(VaultCredential credential, User recipient);

    Optional<CredentialShare> findByIdAndOwner(Long id, User owner);

    Optional<CredentialShare> findByIdAndRecipient(Long id, User recipient);

    @Modifying
    @Transactional
    @Query("DELETE FROM CredentialShare s WHERE s.credential = :credential")
    void deleteByCredential(@Param("credential") VaultCredential credential);
}
