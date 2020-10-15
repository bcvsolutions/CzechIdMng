package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Test schedulable task.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description(TestRegistrableSchedulableTask.DESCRIPTION)
public class TestRegistrableSchedulableTask extends AbstractSchedulableTaskExecutor<String> {
	
	public static final String DESCRIPTION = "test-description";
	public static final String PARAMETER = "parameterOne";
	private String description;
	
	@Override
	public String process() {
		return null;		
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> params = super.getPropertyNames();
		params.add(PARAMETER);
		return params;
	}
	
	@Override
    public String getDescription() {
    	if (description != null) {
    		return description;
    	}
    	return super.getDescription();
    }
    
    public void setDescription(String description) {
		this.description = description;
	}

}
