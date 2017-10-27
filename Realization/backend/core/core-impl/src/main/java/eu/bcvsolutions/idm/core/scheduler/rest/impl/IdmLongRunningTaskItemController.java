package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Default controller for Processed Task Item
 *
 * @author Marek Klement
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/long-running-task-item")
@Api(
		value = IdmLongRunningTaskItemController.TAG,
		description = "Operations with processed task items",
		tags = { IdmLongRunningTaskItemController.TAG })
public class IdmLongRunningTaskItemController extends AbstractReadWriteDtoController<IdmProcessedTaskItemDto, IdmProcessedTaskItemFilter> {


	protected static final String TAG = "Long running task items";

	@Autowired
	private final IdmProcessedTaskItemService itemService;

	@Autowired
	private final IdmScheduledTaskService scheduledTaskService;

	@Autowired
	public IdmLongRunningTaskItemController(IdmProcessedTaskItemService itemService, IdmScheduledTaskService scheduledTaskService) {
		super(itemService);
		//
		Assert.notNull(itemService);
		Assert.notNull(itemService);
		Assert.notNull(scheduledTaskService);
		//
		this.scheduledTaskService = scheduledTaskService;
		this.itemService = itemService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(
			value = "Processed task items",
			nickname = "getProcessedTaskItems",
			response = IdmProcessedTaskItemDto.class,
			tags={ IdmLongRunningTaskItemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
			})
	public ResponseEntity<?> get(
			@ApiParam(value = "Processed task's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(value = "Search processed task's items (/search/quick alias)", nickname = "searchProcessedTaskItems", tags={ IdmLongRunningTaskItemController.TAG }, authorizations = {
			@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
					@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
			@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
					@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
	})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	/**
	 * All endpoints will support find quick method.
	 *
	 * @param parameters
	 * @param pageable
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value= "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_READ + "')")
	@ApiOperation(value = "Search processed task's items", nickname = "searchQuickProcessedTaskItems", tags={ IdmLongRunningTaskItemController.TAG }, authorizations = {
			@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
					@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") }),
			@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
					@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_READ, description = "") })
	})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_DELETE + "')")
	@ApiOperation(
			value = "Delete record",
			nickname = "deleteRecord",
			tags = { IdmLongRunningTaskItemController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_DELETE, description = "") })
			})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Records's uuid identifier", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@ResponseBody
	@RequestMapping(value = "/{backendId}/queue-item", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCHEDULER_CREATE + "')")
	@ApiOperation(
			value = "Create record",
			nickname = "createRecord",
			tags = { IdmLongRunningTaskItemController.TAG },
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.SCHEDULER_CREATE, description = "") })
			})
	public ResponseEntity<?> addToQueue(
			@ApiParam(value = "Records's uuid identifier", required = true)
			@PathVariable @NotNull String backendId, @Valid @RequestBody UUID scheduledTask) {
		IdmScheduledTaskDto task = scheduledTaskService.get(scheduledTask);
		IdmProcessedTaskItemDto itemDto = itemService.get(backendId);
		itemService.createQueueItem(itemDto,new OperationResult(OperationState.EXECUTED),task);
		//
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@Override
	protected IdmProcessedTaskItemFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmProcessedTaskItemFilter filter = super.toFilter(parameters);
		return filter;
	}
}
