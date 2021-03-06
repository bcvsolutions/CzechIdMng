package eu.bcvsolutions.idm.core.model.event.processor.token;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.TokenProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.model.event.TokenEvent.TokenEventType;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;

/**
 * Clear authorization policy caches after identity logout.
 * Support authentication tokens only ("cidmst")
 * 
 * @author Radek Tomiška
 * @since 10.4.1
 */
@Component(TokenEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear authorization policy caches after identity logout.")
public class TokenEvictCacheProcessor extends CoreEventProcessor<IdmTokenDto>  implements TokenProcessor {
	
	public static final String PROCESSOR_NAME = "core-token-evict-cache-processor";
	//
	@Autowired private IdmCacheManager cacheManager;

	public TokenEvictCacheProcessor() {
		super(TokenEventType.UPDATE, TokenEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmTokenDto> process(EntityEvent<IdmTokenDto> event) {
		IdmTokenDto token = event.getContent();
		IdmTokenDto previousToken = event.getOriginalSource();
		//
		// evict authorization manager caches for token identity only
		if (JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME.equals(token.getTokenType()) &&
				(event.hasType(TokenEventType.DELETE) || previousToken == null || (!previousToken.isDisabled() && token.isDisabled()))) { // authentication token was disabled
			// identity owner = see condition above => authentication token only
			UUID identityId = token.getOwnerId(); 
			// evict authorization manager caches for token identity only
			cacheManager.evictValue(AuthorizationManager.PERMISSION_CACHE_NAME, identityId);
			// cached identity authorization policies
			cacheManager.evictValue(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME, identityId);
		}
		// evict token cache on every token change
		cacheManager.evictValue(TokenManager.TOKEN_CACHE_NAME, token.getId());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
