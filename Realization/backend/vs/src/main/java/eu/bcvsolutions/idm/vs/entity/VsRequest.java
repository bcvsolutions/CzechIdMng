package eu.bcvsolutions.idm.vs.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import javax.validation.constraints.NotEmpty;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.vs.domain.VsOperationType;
import eu.bcvsolutions.idm.vs.domain.VsRequestState;

/**
 * Single request on virtual system.
 * 
 * @author Svanda
 *
 */
@Entity
@Table(name = "vs_request", indexes = {
		@Index(name = "idx_vs_request_uid", columnList = "uid"),
		@Index(name = "idx_vs_request_system", columnList = "system_id"),
		@Index(name = "idx_vs_request_role_request_id", columnList = "role_request_id"),
		@Index(name = "idx_vs_request_external_id", columnList = "external_id")})
public class VsRequest extends AbstractEntity implements ExternalIdentifiable {

	private static final long serialVersionUID = 1L;

	/**
	 * UID - Unique identification of account
	 */
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "uid", length = DefaultFieldLengths.NAME, nullable = false)
	private String uid;

	/**
	 * Account is for CzechIdM system
	 */
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystem system;

	/**
	 * Account is for specific connector version
	 */
	@Audited
	@NotEmpty
	@Column(name = "connector_key", nullable = false)
	private String connectorKey;

	@Audited
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "operation_type", nullable = false)
	private VsOperationType operationType;

	// @ManyToOne
	// @JoinColumn(name = "duplicate_to_request_id", referencedColumnName =
	// "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	// @SuppressWarnings("deprecation") // jpa FK constraint does not work in
	// // hibernate 4
	// (name = "none")

	// Limitation: We can use only one mapping on same entity type. When we
	// using two relations on same entity (duplicant and previous for example),
	// then we have exception with unsaved entity in second relation!
	@Audited
	@Column(name = "duplicate_to_request_id")
	private UUID duplicateToRequest;

	@Audited
	@ManyToOne
	@JoinColumn(name = "previous_request_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private VsRequest previousRequest;

	@Audited
	@Column(name = "connector_conf", length = Integer.MAX_VALUE)
	private IcConnectorConfiguration configuration;

	@Audited
	@Column(name = "connector_object", length = Integer.MAX_VALUE)
	private IcConnectorObject connectorObject;

	@Audited
	@NotNull
	@Column(name = "state")
	@Enumerated(EnumType.STRING)
	private VsRequestState state = VsRequestState.CONCEPT;

	@Audited
	@NotNull
	@Column(name = "execute_immediately")
	private boolean executeImmediately = false;

	@Audited
	@Column(name = "reason")
	private String reason;
	// ID of request, without DB relation on the request -> Request can be null or
	// doesn't have to exist!
	@Column(name = "role_request_id")
	private UUID roleRequestId;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;

	public VsOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(VsOperationType operationType) {
		this.operationType = operationType;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public IcConnectorConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(IcConnectorConfiguration configuration) {
		this.configuration = configuration;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public IcConnectorObject getConnectorObject() {
		return connectorObject;
	}

	public void setConnectorObject(IcConnectorObject connectorObject) {
		this.connectorObject = connectorObject;
	}

	public VsRequestState getState() {
		return state;
	}

	public void setState(VsRequestState state) {
		this.state = state;
	}

	public boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public void setExecuteImmediately(boolean executeImmediately) {
		this.executeImmediately = executeImmediately;
	}

	public UUID getDuplicateToRequest() {
		return duplicateToRequest;
	}

	public void setDuplicateToRequest(UUID duplicateToRequest) {
		this.duplicateToRequest = duplicateToRequest;
	}

	public VsRequest getPreviousRequest() {
		return previousRequest;
	}

	public void setPreviousRequest(VsRequest previousRequest) {
		this.previousRequest = previousRequest;
	}

	public String getConnectorKey() {
		return connectorKey;
	}

	public void setConnectorKey(String connectorKey) {
		this.connectorKey = connectorKey;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public UUID getRoleRequestId() {
		return roleRequestId;
	}

	public void setRoleRequestId(UUID roleRequestId) {
		this.roleRequestId = roleRequestId;
	}
	
	/**
	 * @since 9.7.9
	 */
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	/**
	 * @since 9.7.9
	 */
	@Override
	public String getExternalId() {
		return externalId;
	}
	
}