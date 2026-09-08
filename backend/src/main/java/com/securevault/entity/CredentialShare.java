package com.securevault.entity;

import com.securevault.entity.enums.SharePermission;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "credential_shares", indexes = {
    @Index(name = "idx_shares_owner", columnList = "owner_id"),
    @Index(name = "idx_shares_recipient", columnList = "recipient_id"),
    @Index(name = "idx_shares_credential", columnList = "credential_id"),
    @Index(name = "idx_shares_expires", columnList = "expires_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uq_credential_recipient", columnNames = {"credential_id", "recipient_id"})
})
public class CredentialShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "credential_id", nullable = false)
    private VaultCredential credential;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_level", nullable = false, length = 30)
    private SharePermission permission = SharePermission.VIEW_ONLY;

    @Column(name = "expires_at")
    private ZonedDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public CredentialShare() {}

    public CredentialShare(Long id, VaultCredential credential, User owner, User recipient, SharePermission permission, ZonedDateTime expiresAt, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.credential = credential;
        this.owner = owner;
        this.recipient = recipient;
        this.permission = permission != null ? permission : SharePermission.VIEW_ONLY;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = ZonedDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && ZonedDateTime.now().isAfter(expiresAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public VaultCredential getCredential() { return credential; }
    public void setCredential(VaultCredential credential) { this.credential = credential; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }

    public SharePermission getPermission() { return permission; }
    public void setPermission(SharePermission permission) { this.permission = permission; }

    public ZonedDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static CredentialShareBuilder builder() { return new CredentialShareBuilder(); }

    public static class CredentialShareBuilder {
        private Long id;
        private VaultCredential credential;
        private User owner;
        private User recipient;
        private SharePermission permission = SharePermission.VIEW_ONLY;
        private ZonedDateTime expiresAt;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public CredentialShareBuilder id(Long id) { this.id = id; return this; }
        public CredentialShareBuilder credential(VaultCredential credential) { this.credential = credential; return this; }
        public CredentialShareBuilder owner(User owner) { this.owner = owner; return this; }
        public CredentialShareBuilder recipient(User recipient) { this.recipient = recipient; return this; }
        public CredentialShareBuilder permission(SharePermission permission) { this.permission = permission; return this; }
        public CredentialShareBuilder expiresAt(ZonedDateTime expiresAt) { this.expiresAt = expiresAt; return this; }
        public CredentialShareBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CredentialShareBuilder updatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public CredentialShare build() {
            return new CredentialShare(id, credential, owner, recipient, permission, expiresAt, createdAt, updatedAt);
        }
    }
}
