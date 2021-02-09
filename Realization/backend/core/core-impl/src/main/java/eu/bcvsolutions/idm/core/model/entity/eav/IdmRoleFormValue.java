package eu.bcvsolutions.idm.core.model.entity.eav;

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
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Role extended attributes
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_role_form_value", indexes = {
		@Index(name = "idx_idm_role_form_a", columnList = "owner_id"),
		@Index(name = "idx_idm_role_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_idm_role_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_idm_role_form_uuid", columnList = "uuid_value") })
public class IdmRoleFormValue extends AbstractFormValue<IdmRole> {

	private static final long serialVersionUID = -6873566385389649927L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmRole owner;
	
	public IdmRoleFormValue() {
	}
	
	public IdmRoleFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public IdmRole getOwner() {
		return owner;
	}
	
	public void setOwner(IdmRole owner) {
		this.owner = owner;
	}

}
