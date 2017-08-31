package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.OperationResultType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncActionLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncItemLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncLogDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncActionLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncItemLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

@Component
public class RoleSynchronizationExecutor extends AbstractSynchronizationExecutor<IdmIdentityDto>
		implements SynchronizationEntityExecutor {

	private final IdmRoleService roleService;
	private final AccRoleAccountService roleAccoutnService;
	private final IdmRoleRepository roleRepository;
	public final static String ROLE_TYPE_FIELD = "roleType";

	@Autowired
	public RoleSynchronizationExecutor(IcConnectorFacade connectorFacade, SysSystemService systemService,
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService, SysSyncLogService synchronizationLogService,
			SysSyncActionLogService syncActionLogService, AccAccountService accountService,
			SysSystemEntityService systemEntityService, ConfidentialStorage confidentialStorage,
			FormService formService, IdmRoleService roleService,
			AccRoleAccountService roleAccoutnService, SysSyncItemLogService syncItemLogService,
			IdmIdentityRoleService roleRoleService, EntityEventManager entityEventManager,
			GroovyScriptService groovyScriptService, WorkflowProcessInstanceService workflowProcessInstanceService,
			EntityManager entityManager, IdmRoleRepository roleRepository, SysSystemMappingService systemMappingService,
			SysSyncConfigRepository synchronizationConfigRepository,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSchemaAttributeService schemaAttributeService) {
		super(connectorFacade, systemService, attributeHandlingService, synchronizationConfigService,
				synchronizationLogService, syncActionLogService, accountService, systemEntityService,
				confidentialStorage, formService, syncItemLogService, entityEventManager, groovyScriptService,
				workflowProcessInstanceService, entityManager, systemMappingService, synchronizationConfigRepository,
				schemaObjectClassService, schemaAttributeService);

		Assert.notNull(roleService, "Identity service is mandatory!");
		Assert.notNull(roleAccoutnService, "Identity account service is mandatory!");
		Assert.notNull(roleRoleService, "Identity role service is mandatory!");
		Assert.notNull(roleRepository);

		this.roleService = roleService;
		this.roleAccoutnService = roleAccoutnService;
		this.roleRepository = roleRepository;
	}

	/**
	 * Delete entity linked with given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doDeleteEntity(AccAccount account, SystemEntityType entityType, SysSyncLogDto log,
			SysSyncItemLogDto logItem, List<SysSyncActionLogDto> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmRoleDto dto = null;
		if (entityId != null) {
			dto = roleService.get(entityId);
		}
		if (dto == null) {
			addToItemLog(logItem, "Entity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		// Delete role
		roleService.delete(dto);
	}

	/**
	 * Call provisioning for given account
	 * 
	 * @param account
	 * @param entityType
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUpdateAccount(AccAccount account, SystemEntityType entityType, SysSyncLogDto log,
			SysSyncItemLogDto logItem, List<SysSyncActionLogDto> actionLogs) {
		UUID entityId = getEntityByAccount(account.getId());
		IdmRole entity = null;
		if (entityId != null) {
			entity = roleRepository.findOne(entityId);
		}
		if (entity == null) {
			addToItemLog(logItem, "Entity account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		// Call provisioning for this entity
		callProvisioningForEntity(entity, entityType, logItem);
	}

	/**
	 * Call provisioning for given account
	 * 
	 * @param entity
	 * @param entityType
	 * @param logItem
	 */
	protected void callProvisioningForEntity(AbstractEntity entity, SystemEntityType entityType, SysSyncItemLogDto logItem) {
		IdmRole role = (IdmRole) entity;
		addToItemLog(logItem,
				MessageFormat.format(
						"Call provisioning (process RoleEventType.SAVE) for role ({0}) with username ({1}).",
						role.getId(), role.getName()));
		entityEventManager.process(new RoleEvent(RoleEventType.UPDATE, roleService.get(role.getId()))).getContent();
	}

	/**
	 * Create and persist new entity by data from IC attributes
	 * 
	 * @param entityType
	 * @param mappedAttributes
	 * @param logItem
	 * @param uid
	 * @param icAttributes
	 * @param account
	 */
	protected void doCreateEntity(SystemEntityType entityType, List<SysSystemAttributeMappingDto> mappedAttributes,
			SysSyncItemLogDto logItem, String uid, List<IcAttribute> icAttributes, AccAccount account) {
		// We will create new Role
		addToItemLog(logItem, "Missing entity action is CREATE_ENTITY, we will do create new role.");
		IdmRole role = new IdmRole();
		// Fill Role by mapped attribute
		role = (IdmRole) fillEntity(mappedAttributes, uid, icAttributes, role, true);
		
		// Create new Role
		role = (IdmRole) this.saveEntity(role, true);
		
		// Update extended attribute (entity must be persisted first)
		updateExtendedAttributes(mappedAttributes, uid, icAttributes, role, true);
		// Update confidential attribute (entity must be persisted first)
		updateConfidentialAttributes(mappedAttributes, uid, icAttributes, role, true);

		// Create new Role account relation
		AccRoleAccountDto roleAccount = new AccRoleAccountDto();
		roleAccount.setAccount(account.getId());
		roleAccount.setRole(role.getId());
		roleAccount.setOwnership(true);
		roleAccoutnService.save(roleAccount);

		// Role Created
		addToItemLog(logItem, MessageFormat.format("Role with id {0} was created", role.getId()));
		if (logItem != null) {
			logItem.setDisplayName(this.getDisplayNameForEntity(role));
		}
		
		// Call provisioning for entity
	    this.callProvisioningForEntity(role, entityType, logItem);
	}

	/**
	 * Fill data from IC attributes to entity (EAV and confidential storage too)
	 * 
	 * @param account
	 * @param entityType
	 * @param uid
	 * @param icAttributes
	 * @param mappedAttributes
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUpdateEntity(SynchronizationContext context) {
		
		String uid = context.getUid();
		SysSyncLogDto log = context.getLog(); 
		SysSyncItemLogDto logItem = context.getLogItem();
		List<SysSyncActionLogDto> actionLogs = context.getActionLogs();
		List<SysSystemAttributeMappingDto> mappedAttributes = context.getMappedAttributes();
		AccAccount account = context.getAccount();
		List<IcAttribute> icAttributes = context.getIcObject().getAttributes();
		UUID entityId = getEntityByAccount(account.getId());
		IdmRole role = null;
		if (entityId != null) {
			role = roleRepository.findOne(entityId);
		}
		if (role != null) {
			// Update role
			role = (IdmRole) fillEntity(mappedAttributes, uid, icAttributes, role, false);
			this.saveEntity(role, true);
			// Update extended attribute (entity must be persisted first)
			updateExtendedAttributes(mappedAttributes, uid, icAttributes, role, false);
			// Update confidential attribute (entity must be persisted
			// first)
			updateConfidentialAttributes(mappedAttributes, uid, icAttributes, role, false);

			// Role Updated
			addToItemLog(logItem, MessageFormat.format("Role with id {0} was updated", role.getId()));
			if (logItem != null) {
				logItem.setDisplayName(this.getDisplayNameForEntity(role));
			}
			
			// Call provisioning for entity
		    this.callProvisioningForEntity(role, context.getEntityType(), logItem);

			return;
		} else {
			addToItemLog(logItem, "Role account relation (with ownership = true) was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
	}

	/**
	 * Operation remove RoleAccount relations and linked roles
	 * 
	 * @param account
	 * @param removeRoleRole
	 * @param log
	 * @param logItem
	 * @param actionLogs
	 */
	protected void doUnlink(AccAccount account, boolean removeRoleRole, SysSyncLogDto log, SysSyncItemLogDto logItem,
			List<SysSyncActionLogDto> actionLogs) {

		EntityAccountFilter roleAccountFilter = new RoleAccountFilter();
		roleAccountFilter.setAccountId(account.getId());
		List<AccRoleAccountDto> roleAccounts = roleAccoutnService
				.find((RoleAccountFilter) roleAccountFilter, null).getContent();
		if (roleAccounts.isEmpty()) {
			addToItemLog(logItem, "Role account relation was not found!");
			initSyncActionLog(SynchronizationActionType.UPDATE_ENTITY, OperationResultType.WARNING, logItem, log,
					actionLogs);
			return;
		}
		addToItemLog(logItem, MessageFormat.format("Role-account relations to delete {0}", roleAccounts));

		roleAccounts.stream().forEach(roleAccount -> {
			// We will remove role account, but without delete connected
			// account
			roleAccoutnService.delete(roleAccount, false);
			addToItemLog(logItem,
					MessageFormat.format(
							"Role-account relation deleted (without call delete provisioning) (username: {0}, id: {1})",
							roleAccount.getRole(), roleAccount.getId()));

		});
		return;
	}
	
	@Override
	protected Object getValueByMappedAttribute(AttributeMapping attribute, List<IcAttribute> icAttributes) {
		Object transformedValue =  super.getValueByMappedAttribute(attribute, icAttributes);
		// Transform role type enumeration from string
		if (transformedValue instanceof String && attribute.isEntityAttribute() && ROLE_TYPE_FIELD.equals(attribute.getIdmPropertyName())) {
			transformedValue = RoleType.valueOf((String) transformedValue);
		}
		return transformedValue;
	}
	
	/**
	 * Save entity
	 * @param entity
	 * @param skipProvisioning
	 * @return
	 */
	@Override
	protected AbstractEntity saveEntity(AbstractEntity entity, boolean skipProvisioning) {
		IdmRole role = (IdmRole) entity;
		// Create DTO mock ...
		IdmRoleDto dummyDTO = new IdmRoleDto(role.getId());
		boolean isNew = roleService.isNew(dummyDTO);
		
		// Content will be set in service (we need do transform entity to DTO). 
		// Here we set only dummy dto (null content is not allowed)
		EntityEvent<IdmRoleDto> event = new RoleEvent(isNew ? RoleEventType.CREATE : RoleEventType.UPDATE, dummyDTO, ImmutableMap.of(ProvisioningService.SKIP_PROVISIONING, skipProvisioning));
		
		return roleService.publishRole(role, event);
	}

	@Override
	public boolean supports(SystemEntityType delimiter) {
		return SystemEntityType.ROLE == delimiter;
	}

	@Override
	protected AbstractEntity findEntityById(UUID entityId, SystemEntityType entityType) {
		return roleRepository.findOne(entityId);
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new RoleAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return roleAccoutnService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccRoleAccountDto();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityService() {
		return null; // We don't have DTO service for IdmRole now.
	}

	@Override
	protected List<? extends AbstractEntity> findAllEntity() {
		return Lists.newArrayList(roleRepository.findAll());
	}

	@Override
	protected Class<? extends FormableEntity> getEntityClass() {
		return IdmRole.class;
	}

	@Override
	protected BaseEntityRepository getRepository() {
		return this.roleRepository;
	}

	@Override
	protected CorrelationFilter getEntityFilter() {
		return new IdmRoleFilter();
	}

	@Override
	protected AbstractEntity findEntityByAttribute(String idmAttributeName, String value) {
		CorrelationFilter filter = getEntityFilter();
		filter.setProperty(idmAttributeName);
		filter.setValue(value);
		
		List<IdmRoleDto> entities = roleService.find((IdmRoleFilter) filter, null).getContent();
		
		if (CollectionUtils.isEmpty(entities)) {
			return null;
		}
		if (entities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_CORRELATION_TO_MANY_RESULTS,
					ImmutableMap.of("correlationAttribute", idmAttributeName, "value", value));
		}
		if (entities.size() == 1) {
			return roleRepository.findOne(entities.get(0).getId());
		}
		return null;
	}
}
