package eu.bcvsolutions.idm.core.config.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.cache.DistributedIdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.IdMCacheConfiguration;
import eu.bcvsolutions.idm.core.api.config.cache.LocalIdMCacheConfiguration;
import eu.bcvsolutions.idm.core.eav.api.domain.FormDefinitionCache;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultConfigurationService;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultGroovyScriptService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizationManager;
import groovy.lang.Script;

/**
 * Configuration of caches used by core module
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 * @author Radek Tomiška
 */
@Configuration
public class CoreCacheConfiguration {

	/**
	 * Define distributed cache for {@link eu.bcvsolutions.idm.core.api.service.ConfigurationService}
	 *
	 * @return IdMCacheConfiguration for {@link eu.bcvsolutions.idm.core.api.service.ConfigurationService}
	 */
	@Bean
	public IdMCacheConfiguration configurationServiceCacheConfiguration() {
		return DistributedIdMCacheConfiguration.<String, String> builder()
			.withName(DefaultConfigurationService.CACHE_NAME)
				.withKeyType(String.class)
				.withValueType(String.class)
				.build();
	}

	/**
	 * Define local only cache for {@link DefaultGroovyScriptService}. The reason, why we use local only cache
	 * is {@link Script} class, which this service caches, is not {@link java.io.Serializable} so it cannot
	 * be shared in distributed cache.
	 *
	 * @return IdMCacheConfiguration for {@link DefaultGroovyScriptService}
	 */
	@Bean
	public IdMCacheConfiguration groovyScriptCacheConfiguration() {
		return LocalIdMCacheConfiguration.<String, Script>builder()
				.withName(DefaultGroovyScriptService.CACHE_NAME)
				.withKeyType(String.class)
				.withValueType(Script.class)
				.build();
	}
	
	/**
	 * Define distributed cache for {@link AuthorizationManager} - logged identity permissions for entities.
	 *
	 * @return permission cache
	 * @since 10.4.1
	 */
	@Bean
	@SuppressWarnings("rawtypes")
	public IdMCacheConfiguration permissionCacheConfiguration() {
		return DistributedIdMCacheConfiguration.<UUID, HashMap> builder()
			.withName(AuthorizationManager.PERMISSION_CACHE_NAME)
				.withKeyType(UUID.class)
				.withValueType(HashMap.class)
				.withTtl(Duration.ofMinutes(1)) // permissions are based on data structure => cache should be effective short time (one request)
				.build();
	}

	/**
	 * Define distributed cache for {@link AuthorizationManager} - logged identity authorization policies.
	 *
	 * @return autorization policy cache
	 * @since 10.4.1
	 */
	@Bean
	@SuppressWarnings("rawtypes")
	public IdMCacheConfiguration autorizationPolicyCacheConfiguration() {
		return DistributedIdMCacheConfiguration.<UUID, HashMap> builder()
			.withName(AuthorizationManager.AUTHORIZATION_POLICY_CACHE_NAME)
				.withKeyType(UUID.class)
				.withValueType(HashMap.class)
				.withTtl(Duration.ofHours(2)) // Depends on identity is logged out. TODO: clear cache function (keys for logged identity only).
				.build();
	}
	
	/**
	 * Define distributed cache for {@link FormService} - configured form definitions.
	 *
	 * @return form definition cache
	 * @since 10.4.1
	 */
	@Bean
	public IdMCacheConfiguration formDefinitionCacheConfiguration() {
		return DistributedIdMCacheConfiguration.<String, FormDefinitionCache> builder()
			.withName(FormService.FORM_DEFINITION_CACHE_NAME)
				.withKeyType(String.class) // owner type
				.withValueType(FormDefinitionCache.class) // code - form definition
				.build();
	}
}
