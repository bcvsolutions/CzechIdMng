package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;

/**
 * System extended attributes (system connector configuration, etc).
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "sys_system_form_value", indexes = {
		@Index(name = "idx_sys_sys_form_a", columnList = "owner_id"),
		@Index(name = "idx_sys_sys_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_sys_sys_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_sys_sys_form_uuid", columnList = "uuid_value") })
public class SysSystemFormValue extends AbstractFormValue<SysSystem> {

	private static final long serialVersionUID = -6873566385389649927L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystem owner;
	
	public SysSystemFormValue() {
	}
	
	public SysSystemFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public SysSystem getOwner() {
		return owner;
	}
	
	@Override
	public void setOwner(SysSystem owner) {
		this.owner = owner;
	}

}
