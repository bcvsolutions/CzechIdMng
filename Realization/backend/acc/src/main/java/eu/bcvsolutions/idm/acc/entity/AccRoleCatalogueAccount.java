package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import eu.bcvsolutions.idm.acc.domain.EntityAccount;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue;

/**
 * Role catalogue account relation
 * @author svandav
 *
 */
@Entity
@Table(name = "acc_role_catalogue_account", indexes = {
		@Index(name = "idx_acc_cat_account_acc", columnList = "account_id"),
		@Index(name = "idx_acc_cat_account_tree", columnList = "role_catalogue_id") })
public class AccRoleCatalogueAccount extends AbstractEntity implements EntityAccount {

	private static final long serialVersionUID = 1L;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccount account;

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "role_catalogue_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmRoleCatalogue roleCatalogue;

	@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
	@ManyToOne(optional = true)
	@JoinColumn(name = "role_system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysRoleSystem roleSystem;

	@Audited
	@NotNull
	@Column(name = "ownership", nullable = false)
	private boolean ownership = true;

	@Override
	public AccAccount getAccount() {
		return account;
	}

	public void setAccount(AccAccount account) {
		this.account = account;
	}

	@Override
	public boolean isOwnership() {
		return ownership;
	}

	public void setOwnership(boolean ownership) {
		this.ownership = ownership;
	}

	public SysRoleSystem getRoleSystem() {
		return roleSystem;
	}

	public void setRoleSystem(SysRoleSystem roleSystem) {
		this.roleSystem = roleSystem;
	}
	
	public IdmRoleCatalogue getRoleCatalogue() {
		return roleCatalogue;
	}

	public void setRoleCatalogue(IdmRoleCatalogue roleCatalogue) {
		this.roleCatalogue = roleCatalogue;
	}

	@Override
	public AbstractEntity getEntity(){
		return this.roleCatalogue;
	}
}
