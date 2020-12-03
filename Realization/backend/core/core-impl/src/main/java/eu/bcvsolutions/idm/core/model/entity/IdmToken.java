package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javax.validation.constraints.NotEmpty;
import java.time.ZonedDateTime;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Persistent token
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_token", indexes = {
	@Index(name = "idx_idm_token_o_id", columnList = "owner_id"),
	@Index(name = "idx_idm_token_o_type", columnList = "owner_type"),
	@Index(name = "idx_idm_token_exp", columnList = "expiration"),
	@Index(name = "idx_idm_token_token", columnList = "token"),
	@Index(name = "idx_idm_token_type", columnList = "token_type"),
	@Index(name = "idx_idm_token_external_id", columnList = "external_id")
})
public class IdmToken extends AbstractEntity implements Disableable, ExternalIdentifiable {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String ownerType;
	
	@Column(name = "owner_id", length = 16)
	private UUID ownerId;
	
	@Size(max = DefaultFieldLengths.LOG)
	@Column(name = "token", length = DefaultFieldLengths.LOG)
	private String token;

	@Size(max = DefaultFieldLengths.ENUMARATION)
	@Column(name = "token_type", length = DefaultFieldLengths.ENUMARATION)
	private String tokenType;
	
	@Column(name = "module_id")
	private String moduleId;
	
	@Column(name = "properties", length = Integer.MAX_VALUE)
	private ConfigurationMap properties; // full token, cached authorites etc
	
	@NotNull
	@Column(name = "issued_at")
	private ZonedDateTime issuedAt;
	
	@Column(name = "expiration")
	private ZonedDateTime expiration;

	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled; // e.g. logout, authorities removed
	
	@NotNull
	@Column(name = "secret_verified", nullable = false)
	private boolean secretVerified = true;
	
	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}
	
	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public ZonedDateTime getExpiration() {
		return expiration;
	}

	public void setExpiration(ZonedDateTime expiration) {
		this.expiration = expiration;
	}
	
	public void setProperties(ConfigurationMap properties) {
		this.properties = properties;
	}
	
	public ConfigurationMap getProperties() {
		if (properties == null) {
			properties = new ConfigurationMap();
		}
		return properties;
	}
	
	public ZonedDateTime getIssuedAt() {
		return issuedAt;
	}
	
	public void setIssuedAt(ZonedDateTime issuedAt) {
		this.issuedAt = issuedAt;
	}
	
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	
	public String getModuleId() {
		return moduleId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
	
	/**
	 * Token is verified (by two factor) authentication, if needed.
	 * 
	 * @return true => token is verified or verification is not required
	 * @since 10.7.0
	 */
	public boolean isSecretVerified() {
		return secretVerified;
	}
	
	/**
	 * Token is verified (by two factor) authentication, if needed.
	 * 
	 * @param verified  true => token is verified or verification is not required. False => verification is required
	 * @since 10.7.0
	 */
	public void setSecretVerified(boolean secretVerified) {
		this.secretVerified = secretVerified;
	}
}
