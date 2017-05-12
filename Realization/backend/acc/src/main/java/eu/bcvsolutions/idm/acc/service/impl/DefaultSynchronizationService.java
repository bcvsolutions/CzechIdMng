package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationContext;
import eu.bcvsolutions.idm.acc.domain.SynchronizationEventType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.filter.SynchronizationLogFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemEntityFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSyncConfig;
import eu.bcvsolutions.idm.acc.entity.SysSyncItemLog;
import eu.bcvsolutions.idm.acc.entity.SysSyncLog;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationEntityExecutor;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncLogService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractLongRunningTaskExecutor;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;

/**
 * Service for do synchronization and reconciliation
 * @author svandav
 *
 */
@Service
public class DefaultSynchronizationService extends AbstractLongRunningTaskExecutor<SysSyncConfig> implements SynchronizationService {

	private final SysSystemAttributeMappingService attributeHandlingService;
	private final SysSyncConfigService synchronizationConfigService;
	private final SysSyncLogService synchronizationLogService;
	private final SysSystemEntityService systemEntityService;
	private final AccAccountService accountService;
	private final EntityEventManager entityEventManager;
	private final LongRunningTaskManager longRunningTaskManager;
	private final PluginRegistry<SynchronizationEntityExecutor, SystemEntityType> pluginExecutors; 
	private final List<SynchronizationEntityExecutor> executors; 
	//
	private UUID synchronizationConfigId = null;

	@Autowired
	public DefaultSynchronizationService(
			SysSystemAttributeMappingService attributeHandlingService,
			SysSyncConfigService synchronizationConfigService,
			SysSyncLogService synchronizationLogService,
			AccAccountService accountService, SysSystemEntityService systemEntityService,
			EntityEventManager entityEventManager,
			LongRunningTaskManager longRunningTaskManager, List<SynchronizationEntityExecutor>  executors) {
		Assert.notNull(attributeHandlingService);
		Assert.notNull(synchronizationConfigService);
		Assert.notNull(synchronizationLogService);
		Assert.notNull(accountService);
		Assert.notNull(systemEntityService);
		Assert.notNull(entityEventManager);
		Assert.notNull(longRunningTaskManager);
		Assert.notNull(executors);
		//
		this.attributeHandlingService = attributeHandlingService;
		this.synchronizationConfigService = synchronizationConfigService;
		this.synchronizationLogService = synchronizationLogService;
		this.accountService = accountService;
		this.systemEntityService = systemEntityService;
		this.entityEventManager = entityEventManager;
		this.longRunningTaskManager = longRunningTaskManager;
		this.executors = executors;
		
		this.pluginExecutors = OrderAwarePluginRegistry.create(executors);
	}
	
	@Override
	public SysSyncConfig startSynchronizationEvent(SysSyncConfig config) {
		CoreEvent<SysSyncConfig> event = new CoreEvent<SysSyncConfig>(SynchronizationEventType.START, config);
		return (SysSyncConfig) entityEventManager.process(event).getContent(); 
	}
	
	/**
	 * Prepare and execute long running task
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public void startSynchronization(SysSyncConfig config) {
		DefaultSynchronizationService taskExecutor = new DefaultSynchronizationService(attributeHandlingService, synchronizationConfigService, synchronizationLogService, accountService, systemEntityService, entityEventManager, longRunningTaskManager, executors);
		taskExecutor.synchronizationConfigId = config.getId();
		longRunningTaskManager.execute(taskExecutor);
	}
	
	/**
	 * Add transactional only - public method called from long running task manager
	 */
	@Override
	@Transactional(propagation = Propagation.NEVER)
	public SysSyncConfig call() {
		return super.call();
	}
		
	@Override
	public String getDescription() {
		SysSyncConfig config = synchronizationConfigService.get(synchronizationConfigId);
		if (config == null) {
			return "Synchronization long running task";
		}
		return MessageFormat.format("Run synchronization [{0}] - [{1}]", config.getName(), config.getSystemMapping().getName());
	}

	/**
	 * Called from long running task
	 */
	@Override
	public SysSyncConfig process() {
		SysSyncConfig config = synchronizationConfigService.get(synchronizationConfigId);
		//
		if (config == null) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_NOT_FOUND,
					ImmutableMap.of("id", synchronizationConfigId));
		}
		SysSystemMapping mapping = config.getSystemMapping();
		Assert.notNull(mapping);
		SystemEntityType entityType = mapping.getEntityType();
	
		SynchronizationEntityExecutor executor =  getSyncExecutor(entityType);
		executor.setLongRunningTaskExecutor(this);
		return executor.process(synchronizationConfigId);
	}
	
	/**
	 * Synchronization has own cancel mechanism
	 * 
	 * @param running
	 */
	public void updateState(boolean running) {
		boolean result = super.updateState();
		if (running && !result) { // synchronization was canceled from long running task agenda - we need to stop synchronization through event 
			stopSynchronizationEvent(synchronizationConfigService.get(synchronizationConfigId));
		}
	}
	
	@Override
	public SysSyncConfig stopSynchronizationEvent(SysSyncConfig config) {
		CoreEvent<SysSyncConfig> event = new CoreEvent<SysSyncConfig>(SynchronizationEventType.CANCEL, config);
		return (SysSyncConfig) entityEventManager.process(event).getContent(); 
	}
	
	@Override
	public SysSyncConfig stopSynchronization(SysSyncConfig config){
		Assert.notNull(config);
		// Synchronization must be running
		SynchronizationLogFilter logFilter = new SynchronizationLogFilter();
		logFilter.setSynchronizationConfigId(config.getId());
		logFilter.setRunning(Boolean.TRUE);
		List<SysSyncLog> logs  = synchronizationLogService.find(logFilter, null).getContent();
		
		if (logs.isEmpty()) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_IS_NOT_RUNNING,
					ImmutableMap.of("name", config.getName()));
		}
		
		logs.forEach(log -> {
			log.setRunning(false);
		});
		synchronizationLogService.saveAll(logs);
		return config;
	}

	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public boolean doItemSynchronization(SynchronizationContext wrapper) {
		Assert.notNull(wrapper);
		return getSyncExecutor(wrapper.getEntityType()).doItemSynchronization(wrapper);
	}

	@Override
	public SysSyncItemLog resolveMissingEntitySituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);
		Assert.notNull(configId);
		Assert.notNull(actionType);

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
		SysSystem system = mapping.getSystem();

		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMapping> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
				.getContent();
		SysSyncItemLog itemLog = new SysSyncItemLog();
		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);
		
		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid)
		.addSystem(system)
		.addConfig(config)
		.addEntityType(entityType)
		.addLogItem(itemLog)
		.addMappedAttributes(mappedAttributes)
		.addIcObject(icObject);
		
		getSyncExecutor(entityType).resolveMissingEntitySituation(SynchronizationMissingEntityActionType.valueOf(actionType), context);
		return itemLog;

	}

	@Override
	public SysSyncItemLog resolveLinkedSituation(String uid, SystemEntityType entityType,
			List<IcAttribute> icAttributes, UUID accountId, UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(icAttributes);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(accountId);

		SysSyncItemLog itemLog = new SysSyncItemLog();

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
		AccAccount account = accountService.get(accountId);

		SystemAttributeMappingFilter attributeHandlingFilter = new SystemAttributeMappingFilter();
		attributeHandlingFilter.setSystemMappingId(mapping.getId());
		List<SysSystemAttributeMapping> mappedAttributes = attributeHandlingService.find(attributeHandlingFilter, null)
				.getContent();
	
		// Little workaround, we have only IcAttributes ... we create IcObject manually
		IcConnectorObjectImpl icObject = new IcConnectorObjectImpl();
		icObject.setAttributes(icAttributes);
		icObject.setUidValue(uid);
				
		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid)
		.addAccount(account)
		.addConfig(config)
		.addEntityType(entityType)
		.addLogItem(itemLog)
		.addMappedAttributes(mappedAttributes)
		.addIcObject(icObject);

		getSyncExecutor(entityType).resolveLinkedSituation(SynchronizationLinkedActionType.valueOf(actionType), context);
		return itemLog;
	}

	@Override
	public SysSyncItemLog resolveUnlinkedSituation(String uid, SystemEntityType entityType, UUID entityId,
			UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(entityId);

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
	
		SysSystem system = mapping.getSystem();
		SysSystemEntity systemEntity = findSystemEntity(uid, system, entityType);
		SysSyncItemLog itemLog = new SysSyncItemLog();

		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid)
		.addSystem(system)
		.addConfig(config)
		.addEntityType(entityType)
		.addEntityId(entityId)
		.addSystemEntity(systemEntity);
		
		getSyncExecutor(entityType).resolveUnlinkedSituation(SynchronizationUnlinkedActionType.valueOf(actionType), context);
		return itemLog;
	}

	@Override
	public SysSyncItemLog resolveMissingAccountSituation(String uid, SystemEntityType entityType, UUID accountId,
			UUID configId, String actionType) {
		Assert.notNull(uid);
		Assert.notNull(entityType);
		Assert.notNull(configId);
		Assert.notNull(actionType);
		Assert.notNull(accountId);

		SysSyncConfig config = synchronizationConfigService.get(configId);
		SysSystemMapping mapping = config.getSystemMapping();
		AccAccount account = accountService.get(accountId);
		SysSystem system = mapping.getSystem();
		SysSyncItemLog itemLog = new SysSyncItemLog();
		SynchronizationContext context = new SynchronizationContext();
		context.addUid(uid)
		.addSystem(system)
		.addConfig(config)
		.addEntityType(entityType)
		.addAccount(account)
		.addLogItem(itemLog);

		getSyncExecutor(entityType).resolveMissingAccountSituation(ReconciliationMissingAccountActionType.valueOf(actionType), context);
		return itemLog;
	}
	
	private SysSystemEntity findSystemEntity(String uid, SysSystem system, SystemEntityType entityType) {
		SystemEntityFilter systemEntityFilter = new SystemEntityFilter();
		systemEntityFilter.setEntityType(entityType);
		systemEntityFilter.setSystemId(system.getId());
		systemEntityFilter.setUidId(uid);
		List<SysSystemEntity> systemEntities = systemEntityService.find(systemEntityFilter, null).getContent();
		SysSystemEntity systemEntity = null;
		if (systemEntities.size() == 1) {
			systemEntity = systemEntities.get(0);
		} else if (systemEntities.size() > 1) {
			throw new ProvisioningException(AccResultCode.SYNCHRONIZATION_TO_MANY_SYSTEM_ENTITY, uid);
		}
		return systemEntity;
	}

	@Override
	public void setSynchronizationConfigId(UUID synchronizationConfigId) {
		this.synchronizationConfigId = synchronizationConfigId;
	}
	
	/**
	 * Find executor for synchronization given entity type
	 * @param entityType
	 * @return
	 */
	private SynchronizationEntityExecutor getSyncExecutor(SystemEntityType entityType){
		
		SynchronizationEntityExecutor executor =  pluginExecutors.getPluginFor(entityType);
		if (executor == null) {
			throw new UnsupportedOperationException(
					MessageFormat.format("Synchronization executor for SystemEntityType {0} is not supported!", entityType));
		}
		return executor;
	}
}
