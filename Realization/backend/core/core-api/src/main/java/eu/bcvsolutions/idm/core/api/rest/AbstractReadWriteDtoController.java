package eu.bcvsolutions.idm.core.api.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * CRUD operations for DTO
 * 
 * @author Svanda
 *
 */
public abstract class AbstractReadWriteDtoController<DTO extends BaseDto, F extends BaseFilter>
		extends AbstractReadDtoController<DTO, F> {

	public AbstractReadWriteDtoController(ReadWriteDtoService<DTO, ?, F> entityService) {
		super(entityService);
	}

	/**
	 * Post DTO and convert to response
	 * 
	 * @param dto
	 * @return
	 */
	public ResponseEntity<?> post(DTO dto) {
		DTO createdIdentity = postDto(dto);
		if (createdIdentity.getId() == null) {			
			throw new ResultCodeException(CoreResultCode.ACCEPTED);
		}
		return new ResponseEntity<>(toResource(createdIdentity), HttpStatus.CREATED);
	}

	/**
	 * Creates given DTO
	 * 
	 * @param dto
	 * @return
	 */
	public DTO postDto(DTO dto) {
		return getService().save(dto);
	}

	/**
	 * Put DTO by given backendId and convert to response
	 * 
	 * @param backendId
	 * @param dto
	 * @return
	 */
	public ResponseEntity<?> put(@PathVariable @NotNull String backendId, DTO dto) {
		DTO updateDto = getDto(backendId);
		if (updateDto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		DTO updatedDto = putDto(dto);
		return new ResponseEntity<>(toResource(updatedDto), HttpStatus.OK);
	}

	/**
	 * Updates given DTO
	 * 
	 * @param dto
	 * @return
	 */
	public DTO putDto(DTO dto) {
		Assert.notNull(dto, "DTO is required");
		return getService().save(dto);
	}

	/**
	 * Patch is not implemented yet
	 * 
	 * @param backendId
	 * @param nativeRequest
	 * @return
	 * @throws HttpMessageNotReadableException
	 */
	public ResponseEntity<?> patch(@PathVariable @NotNull String backendId, HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		throw new ResultCodeException(CoreResultCode.NOT_IMPLEMENTED, "patch method");
	}

	/**
	 * Deletes DTO by given id
	 * 
	 * @param backendId
	 * @return
	 */
	public ResponseEntity<?> delete(@PathVariable @NotNull String backendId) {
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		deleteDto(dto);
		return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Deletes given DTO
	 * 
	 * @param backendId
	 * @return
	 */
	public void deleteDto(DTO dto) {
		Assert.notNull(dto, "DTO is required");
		getService().delete(dto);
	}

	/**
	 * Returns DTO service configured to current controller
	 */
	@Override
	protected ReadWriteDtoService<DTO, ?, F> getService() {
		return (ReadWriteDtoService<DTO, ?, F>) super.getService();
	}

}
