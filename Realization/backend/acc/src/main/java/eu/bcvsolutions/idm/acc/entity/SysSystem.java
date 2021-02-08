package eu.bcvsolutions.idm.acc.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInstanceImpl;

/**
 * Target system setting - is used for accont management and provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_system", indexes = {
		@Index(name = "ux_system_name", columnList = "name", unique = true),
		@Index(name = "idx_idm_password_pol_gen", columnList = "password_pol_val_id"),
		@Index(name = "idx_idm_password_pol_val", columnList = "password_pol_gen_id"),
		@Index(name = "idx_idm_sys_remote_ser_id", columnList = "remote_server_id")})
public class SysSystem extends AbstractEntity implements Codeable, FormableEntity, Disableable {

	private static final long serialVersionUID = -8276147852371288351L;
	
	@Audited
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@Column(name = "name", length = DefaultFieldLengths.NAME, nullable = false)
	private String name;
	
	@Audited
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@Audited
	@NotNull
	@Column(name = "readonly", nullable = false)
	private boolean readonly;
	
	@Audited
	@NotNull
	@Column(name = "disabled_provisioning", nullable = false)
	private boolean disabledProvisioning; // @since 9.6.0 - provisioning is disabled on system - just account uid and ACM is executed. Provisioning operation is not created.
	
	@Audited
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled; // just write operation is disabled on the system, ACM and wish is constructed, provisioning operation is available in queue.
	
	@Audited
	@NotNull
	@Column(name = "queue", nullable = false)
	private boolean queue;
	
	@Audited
	@NotNull
	@Column(name = "virtual", nullable = false)
	private boolean virtual;
	
	@Version
	@JsonIgnore
	private Long version; // Optimistic lock - will be used with ETag
	
	@JsonIgnore
	@OneToMany(mappedBy = "system")
	private List<SysRoleSystem> roleSystems; // only for hibernate mappnig - we dont want lazy lists
	
	@Audited
	@Embedded
	private SysConnectorKey connectorKey;
	
	@Audited
	@Column(name = "remote", nullable = false)
	private boolean remote;
	
	@Audited
	@Embedded
	private SysConnectorServer connectorServer;
	
	@Audited
	@Embedded
	private SysBlockedOperation blockedOperation;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "password_pol_val_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmPasswordPolicy passwordPolicyValidate;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "password_pol_gen_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmPasswordPolicy passwordPolicyGenerate;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "remote_server_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysRemoteServer remoteServer;

	public String getName() {
		return name;
	}
	
	@Override
	@JsonIgnore
	public String getCode() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public IdmPasswordPolicy getPasswordPolicyValidate() {
		return passwordPolicyValidate;
	}

	public void setPasswordPolicyValidate(IdmPasswordPolicy passwordPolicyValidate) {
		this.passwordPolicyValidate = passwordPolicyValidate;
	}

	public IdmPasswordPolicy getPasswordPolicyGenerate() {
		return passwordPolicyGenerate;
	}

	public void setPasswordPolicyGenerate(IdmPasswordPolicy passwordPolicyGenerate) {
		this.passwordPolicyGenerate = passwordPolicyGenerate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
	
	public boolean isVirtual() {
		return virtual;
	}
	
	/**
	 * Configured connector
	 * 
	 * @return
	 */
	public SysConnectorKey getConnectorKey() {
		return connectorKey;
	}
	
	public void setConnectorKey(SysConnectorKey connectorKey) {
		this.connectorKey = connectorKey;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isQueue() {
		return queue;
	}

	public void setQueue(boolean queue) {
		this.queue = queue;
	}

	public boolean isRemote() {
		return remote;
	}

	public SysConnectorServer getConnectorServer() {
		return connectorServer;
	}

	public void setRemote(boolean remote) {
		this.remote = remote;
	}

	public void setConnectorServer(SysConnectorServer connectorServer) {
		this.connectorServer = connectorServer;
	}
	
	public SysBlockedOperation getBlockedOperation() {
		return blockedOperation;
	}

	public void setBlockedOperation(SysBlockedOperation blockedOperation) {
		this.blockedOperation = blockedOperation;
	}

	@JsonIgnore
	public IcConnectorInstance getConnectorInstance() {
		return new IcConnectorInstanceImpl(this.getConnectorServer(), this.getConnectorKey(), this.isRemote());
	}
	
	/**
	 * Provisioning is disabled on system - just account uid and ACM is executed. Provisioning operation is not created.
	 * 
	 * @param disabledProvisioning
	 * @since 9.6.0 
	 */
	public void setDisabledProvisioning(boolean disabledProvisioning) {
		this.disabledProvisioning = disabledProvisioning;
	}
	
	/**
	 * Provisioning is disabled on system - just account uid and ACM is executed. Provisioning operation is not created.
	 * 
	 * @return
	 * @since 9.6.0 
	 */
	public boolean isDisabledProvisioning() {
		return disabledProvisioning;
	}
	
	/**
	 * System uses remote server.
	 * 
	 * @return remote server
	 * @since 10.8.0
	 */
	public SysRemoteServer getRemoteServer() {
		return remoteServer;
	}
	
	/**
	 * System uses remote server.
	 * 
	 * @param remoteServer remote server
	 * @since 10.8.0
	 */
	public void setRemoteServer(SysRemoteServer remoteServer) {
		this.remoteServer = remoteServer;
	}
}
