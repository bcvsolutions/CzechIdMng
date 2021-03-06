package eu.bcvsolutions.idm.core.api.audit.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for {@link IdmLoggingEventProperty}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmLoggingEventPropertyFilter implements BaseFilter {

	private Long eventId;
	private String mappedKey;
	private String mappedValue;

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getMappedKey() {
		return mappedKey;
	}

	public void setMappedKey(String mappedKey) {
		this.mappedKey = mappedKey;
	}

	public String getMappedValue() {
		return mappedValue;
	}

	public void setMappedValue(String mappedValue) {
		this.mappedValue = mappedValue;
	}

}
