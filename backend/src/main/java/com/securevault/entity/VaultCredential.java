package com.securevault.entity;

import com.securevault.entity.enums.CredentialCategory;
import jakarta.persistence.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "vault_credentials", indexes = {
    @Index(name = "idx_vault_user_id", columnList = "user_id"),
    @Index(name = "idx_vault_category", columnList = "user_id, category"),
    @Index(name = "idx_vault_search", columnList = "user_id, alias_name, username")
})
public class VaultCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "application_url", length = 500)
    private String applicationUrl;

    @Column(name = "alias_name", length = 150)
    private String aliasName;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(name = "encrypted_password", nullable = false, columnDefinition = "TEXT")
    private String encryptedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CredentialCategory category = CredentialCategory.OTHER;

    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<CredentialShare> shares = new java.util.ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public VaultCredential() {}

    public VaultCredential(Long id, User user, String applicationUrl, String aliasName, String username, String encryptedPassword, CredentialCategory category, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.applicationUrl = applicationUrl;
        this.aliasName = aliasName;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.category = category != null ? category : CredentialCategory.OTHER;
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public String getAliasName() { return aliasName; }
    public void setAliasName(String aliasName) { this.aliasName = aliasName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public CredentialCategory getCategory() { return category; }
    public void setCategory(CredentialCategory category) { this.category = category; }

    public ZonedDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(ZonedDateTime createdAt) { this.createdAt = createdAt; }

    public ZonedDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static VaultCredentialBuilder builder() { return new VaultCredentialBuilder(); }

    public static class VaultCredentialBuilder {
        private Long id;
        private User user;
        private String applicationUrl;
        private String aliasName;
        private String username;
        private String encryptedPassword;
        private CredentialCategory category = CredentialCategory.OTHER;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;

        public VaultCredentialBuilder id(Long id) { this.id = id; return this; }
        public VaultCredentialBuilder user(User user) { this.user = user; return this; }
        public VaultCredentialBuilder applicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; return this; }
        public VaultCredentialBuilder aliasName(String aliasName) { this.aliasName = aliasName; return this; }
        public VaultCredentialBuilder username(String username) { this.username = username; return this; }
        public VaultCredentialBuilder encryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; return this; }
        public VaultCredentialBuilder category(CredentialCategory category) { this.category = category; return this; }
        public VaultCredentialBuilder createdAt(ZonedDateTime createdAt) { this.createdAt = createdAt; return this; }
        public VaultCredentialBuilder updatedAt(ZonedDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public VaultCredential build() {
            return new VaultCredential(id, user, applicationUrl, aliasName, username, encryptedPassword, category, createdAt, updatedAt);
        }
    }
}
