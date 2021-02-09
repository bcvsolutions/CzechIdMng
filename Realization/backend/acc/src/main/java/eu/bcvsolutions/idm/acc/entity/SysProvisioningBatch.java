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

import java.time.ZonedDateTime;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Provisioning requests in the same batch. Any operation has batch.
 * One entity can have more system entities
 * 
 * @author Filip Mestanek
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_provisioning_batch", indexes = {
		@Index(name = "idx_sys_p_b_next", columnList = "next_attempt"),
		@Index(name = "idx_sys_p_b_sys_entity", columnList = "system_entity_id")
		})
public class SysProvisioningBatch extends AbstractEntity {

	private static final long serialVersionUID = -546573793473482877L;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_entity_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystemEntity systemEntity;
	
	@Column(name = "next_attempt")
	private ZonedDateTime nextAttempt;

	public ZonedDateTime getNextAttempt() {
		return nextAttempt;
	}

	public void setNextAttempt(ZonedDateTime nextAttempt) {
		this.nextAttempt = nextAttempt;
	}

	public SysSystemEntity getSystemEntity() {
		return systemEntity;
	}

	public void setSystemEntity(SysSystemEntity systemEntity) {
		this.systemEntity = systemEntity;
	}
}