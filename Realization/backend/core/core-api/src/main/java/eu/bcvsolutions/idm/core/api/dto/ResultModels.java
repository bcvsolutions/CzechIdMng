package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.exception.ErrorModel;

/**
 * Model wrapper for errors and infos response
 * - simply adds errors or info element to response
 * 
 * @author Radek Tomiška 
 */
@com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY)
public class ResultModels implements Serializable {
	
	public static final String ATTRIBUTE_ERRORS = "_errors";
	public static final String ATTRIBUTE_INFOS = "_infos";
	//
	private static final long serialVersionUID = 130622622063070662L;
	//
	private final List<ErrorModel> errors = Lists.newArrayList();
	private final List<ResultModel> infos = Lists.newArrayList();

	public ResultModels() {
	}

	public ResultModels(ErrorModel error) {
		this(error, null);
	}
	
	public ResultModels(ErrorModel error, ResultModel info) {
		if (error != null) {
			this.errors.add(error);
		}
		if (info != null) {
			this.infos.add(info);
		}
	}
	
	public ResultModels(List<ErrorModel> errors) {
		this.errors.addAll(errors);
	}
	
	public ResultModels(List<ErrorModel> errors, List<ResultModel> infos) {
		if (errors != null) {
			this.errors.addAll(errors);
		}
		if (infos != null) {
			this.infos.addAll(infos);
		}
	}

	public void addError(ErrorModel error) {
		this.errors.add(error);
	}
	
	public void addInfo(ResultModel info) {
		this.infos.add(info);
	}
	
	@com.fasterxml.jackson.annotation.JsonProperty(ATTRIBUTE_ERRORS)
	public List<ResultModel> getErrors() {
		return Collections.unmodifiableList(errors);
	}
	
	@com.fasterxml.jackson.annotation.JsonProperty(ATTRIBUTE_INFOS)
	public List<ResultModel> getInfos() {
		return Collections.unmodifiableList(infos);
	}
	
	@com.fasterxml.jackson.annotation.JsonIgnore
	public ErrorModel getError() {
		if (this.errors.isEmpty()) {
			return null;
		}
		return errors.get(0);
	}
}