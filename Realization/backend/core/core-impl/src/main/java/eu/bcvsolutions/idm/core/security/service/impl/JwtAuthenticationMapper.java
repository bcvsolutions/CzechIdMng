package eu.bcvsolutions.idm.core.security.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.MacSigner;
import org.springframework.security.jwt.crypto.sign.SignerVerifier;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.Hashing;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.security.api.domain.DefaultGrantedAuthority;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.DefaultGrantedAuthorityDto;
import eu.bcvsolutions.idm.core.security.api.dto.IdmJwtAuthenticationDto;
import eu.bcvsolutions.idm.core.security.api.filter.IdmAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.GrantedAuthoritiesFactory;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * Reads authentication from token and provides conversions from / to dto and to token.
 * Persist / loads created tokens.
 *
 * @author Radek Tomiška
 *
 */
@Component
public class JwtAuthenticationMapper {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationMapper.class);
	//
	public static final String AUTHENTICATION_TOKEN_NAME = IdmAuthenticationFilter.AUTHENTICATION_TOKEN_NAME.toUpperCase();
	public static final String PROPERTY_SECRET_TOKEN = "idm.sec.security.jwt.secret.token";
	public static final String DEFAULT_SECRET_TOKEN = "idmSecret";
	public static final String PROPERTY_AUTHORITIES = "authorities";
	public static final String PROPERTY_CURRENT_USERNAME = "currentUsername";
	// @since 10.5.0 - Token owner can be different than logger user (switched user).
	public static final String PROPERTY_CURRENT_IDENTITY_ID = "currentIdentityId";
	public static final String PROPERTY_ORIGINAL_USERNAME = "originalUsername";
	public static final String PROPERTY_ORIGINAL_IDENTITY_ID = "originalIdentityId";
	//
	@Lazy
	@Autowired private ObjectMapper mapper;
	@Autowired private ConfigurationService configurationService;
	@Autowired private TokenManager tokenManager;
	@Autowired private GrantedAuthoritiesFactory grantedAuthoritiesFactory;

	/**
	 * Reads {@link IdmJwtAuthentication} from given token
	 *
	 * @param token
	 * @return
	 * @throws IOException
	 */
	public IdmJwtAuthentication readToken(String token) throws IOException {
		if (StringUtils.isEmpty(token)) {
			return null;
		}
		//
		SignerVerifier verifier = getVerifier();
		String decoded = JwtHelper.decodeAndVerify(token, verifier).getClaims();
		return fromDto(mapper.readValue(decoded, IdmJwtAuthenticationDto.class));
	}

	/**
	 * Return IdM OAuth token verifier.
	 * @return
	 */
	public SignerVerifier getVerifier() {
		return new MacSigner(getSecret().asString());
	}

	/**
	 * Writes authentication to token
	 *
	 * @param authentication
	 * @return
	 * @throws IOException
	 */
	public String writeToken(IdmJwtAuthentication authentication) {
		Assert.notNull(authentication, "Authentication is required to write token");
		//
		return writeToken(toDto(authentication));
	}

	/**
	 * Writes authentication dto to token
	 *
	 * @param dto
	 * @return
	 * @throws IOException
	 */
	public String writeToken(IdmJwtAuthenticationDto dto) {
		try {
			Assert.notNull(dto, "Authentication is required to write token");
			//
			String authenticationJson = mapper.writeValueAsString(dto);
			return JwtHelper.encode(authenticationJson, new MacSigner(getSecret().asString())).getEncoded();
		} catch(IOException ex) {
			throw new CoreException(String.format("Creating JWT token [%s] failed.", dto.getId()), ex);
		}
	}

	/**
	 * Reads secret from configuration
	 *
	 * @return
	 */
	private GuardedString getSecret() {
		return configurationService.getGuardedValue(PROPERTY_SECRET_TOKEN, DEFAULT_SECRET_TOKEN);
	}

	/**
	 * Constructs new actual expiration by the configuration
	 *
	 * @return
	 */
	public ZonedDateTime getNewExpiration() {
		Integer timeoutMillis = configurationService.getIntegerValue(
				LoginService.PROPERTY_EXPIRATION_TIMEOUT,
				LoginService.DEFAULT_EXPIRATION_TIMEOUT);
		//
		return ZonedDateTime.now().plus(timeoutMillis, ChronoField.MILLI_OF_DAY.getBaseUnit());
	}

	/**
	 * Converts dto to authentication.
	 * Authentication authorities are loaded or filled from persisted token.
	 * If token not exists, then is created.
	 * Actual authentication informations are returned
	 *
	 * @param dto
	 * @return
	 */
	public IdmJwtAuthentication fromDto(IdmJwtAuthenticationDto dto) {
		Assert.notNull(dto, "Authentication DTO is required to be transformed to authentication.");
		UUID currentIdentityId = dto.getCurrentIdentityId();
		Assert.notNull(dto.getCurrentIdentityId(), "Current identity identifier is required.");
		//
		IdmIdentityDto identity = new IdmIdentityDto(currentIdentityId, dto.getCurrentUsername());
		// try to load token or create a new one
		IdmTokenDto token = dto.getId() == null ? null : tokenManager.getToken(dto.getId());
		if (token == null) {
			token = new IdmTokenDto();
			// required not overridable properties
			token.setTokenType(AUTHENTICATION_TOKEN_NAME);
			token.setOwnerId(dto.getCurrentIdentityId());
			token.setOwnerType(tokenManager.getOwnerType(IdmIdentityDto.class));
			token.setIssuedAt(dto.getIssuedAt());
			token.setExpiration(dto.getExpiration());
			ConfigurationMap properties = token.getProperties();
			properties.put(PROPERTY_AUTHORITIES, getDtoAuthorities(grantedAuthoritiesFactory.getGrantedAuthoritiesForIdentity(currentIdentityId)));
			properties.put(PROPERTY_CURRENT_USERNAME, identity.getUsername());
			properties.put(PROPERTY_CURRENT_IDENTITY_ID, currentIdentityId);
			properties.put(PROPERTY_ORIGINAL_USERNAME,  dto.getOriginalUsername());
			properties.put(PROPERTY_ORIGINAL_IDENTITY_ID, dto.getOriginalIdentityId());
			//
			token.setId(dto.getId()); // preserve authentication id if given
			if (token.getId() == null) {
				// token id has to be written int token
				token.setId(UUID.randomUUID());
			}
			token.setToken(getTokenHash(token));
			token = tokenManager.saveToken(identity, token);
		}
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				token.getId(),
				identity,
				new IdmIdentityDto(dto.getOriginalIdentityId(), dto.getOriginalUsername()),
				token.getExpiration(),
				token.getIssuedAt(),
				null,
				dto.getFromModule());
		//
		Collection<DefaultGrantedAuthorityDto> authorities = getDtoAuthorities(token);
		List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (DefaultGrantedAuthorityDto a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthority(a.getAuthority()));
			}
		} else {
			grantedAuthorities.addAll(grantedAuthoritiesFactory.getGrantedAuthoritiesForIdentity(currentIdentityId));
		}
		authentication.setAuthorities(grantedAuthorities);
		//
		return authentication;
	}

	/**
	 * Converts dto to authentication.
	 *
	 * @param token
	 * @return
	 */
	public IdmJwtAuthentication fromDto(IdmTokenDto token) {
		Assert.notNull(token, "Token is required.");
		//
		List<GrantedAuthority> grantedAuthorities = getDtoAuthorities(token)
				.stream()
				.map(authority -> new DefaultGrantedAuthority(authority.getAuthority()))
				.collect(Collectors.toList());
		//
		IdmJwtAuthentication authentication = new IdmJwtAuthentication(
				new IdmIdentityDto(
						token.getProperties().getUuid(PROPERTY_CURRENT_IDENTITY_ID),
						token.getProperties().getString(PROPERTY_CURRENT_USERNAME)),
				new IdmIdentityDto(
						token.getProperties().getUuid(PROPERTY_ORIGINAL_IDENTITY_ID),
						token.getProperties().getString(PROPERTY_ORIGINAL_USERNAME)),
				token.getExpiration(),
				token.getIssuedAt(),
				grantedAuthorities,
				token.getModuleId());
		authentication.setId(token.getId());
		//
		return authentication;
	}

	/**
	 * Create token with assigned identity authorities
	 *
	 * @param identity
	 * @param preparedToken
	 * @return preparedToken with filled required
	 */
	public IdmTokenDto createToken(IdmIdentityDto identity, IdmTokenDto preparedToken) {
		Assert.notNull(identity, "Identity is required.");
		UUID identityId = identity.getId();
		Assert.notNull(identityId, "Identity identifier is required.");
		//
		// persist token
		IdmTokenDto token = new IdmTokenDto();
		if (preparedToken != null) {
			// fill optional token properties
			token.setId(preparedToken.getId());
			token.setModuleId(preparedToken.getModuleId());
			token.setExternalId(preparedToken.getExternalId());
			token.getProperties().putAll(preparedToken.getProperties());
			token.setDisabled(preparedToken.isDisabled());
			token.setIssuedAt(preparedToken.getIssuedAt());
			token.setOwnerId(preparedToken.getOwnerId());
			token.setOwnerType(preparedToken.getOwnerType());
			token.setTokenType(preparedToken.getTokenType());
		}
		// required properties
		if (token.getTokenType() == null) {
			token.setTokenType(AUTHENTICATION_TOKEN_NAME);
		}
		if (token.getOwnerId() == null) {
			token.setOwnerId(identityId);
		}
		if (token.getOwnerType() == null) {
			token.setOwnerType(tokenManager.getOwnerType(identity));
		}
		if (token.getIssuedAt() == null) {
			token.setIssuedAt(ZonedDateTime.now());
		}
		token.setExpiration(getNewExpiration());
		ConfigurationMap properties = token.getProperties();
		properties.put(PROPERTY_AUTHORITIES, getDtoAuthorities(grantedAuthoritiesFactory.getGrantedAuthoritiesForIdentity(identity.getId())));
		properties.put(PROPERTY_CURRENT_USERNAME, identity.getUsername());
		properties.put(PROPERTY_CURRENT_IDENTITY_ID, identityId);
		properties.putIfAbsent(PROPERTY_ORIGINAL_USERNAME, identity.getUsername()); // original user can be set in prepared token
		properties.putIfAbsent(PROPERTY_ORIGINAL_IDENTITY_ID, identityId); // original user can be set in prepared token
		//
		if (token.getId() == null) {
			// token id has to be written into token
			token.setId(UUID.randomUUID());
		}
		token.setToken(getTokenHash(token));
		token = tokenManager.saveToken(identity, token);
		//
		return token;
	}

	/**
	 * Prolong authentication expiration - but only if difference from old expiration is greater than one minute.
	 * If persistent token for given authentication is found, then persisted token is updated
	 *
	 * @param tokenId
	 * @return returns actual token
	 */
	public IdmJwtAuthenticationDto prolongExpiration(IdmJwtAuthenticationDto authenticationDto) {
		if (authenticationDto == null || authenticationDto.getId() == null) {
			return authenticationDto;
		}
		//
		ZonedDateTime newExpiration = getNewExpiration();
		ZonedDateTime oldExpiration = authenticationDto.getExpiration();
		if (oldExpiration == null) {
			LOG.trace("Authentication token with id [{}] has unlimited expiration (e.g. system token), expiration will not be changed.", authenticationDto.getId());
			return authenticationDto;
		}
		long seconds = ChronoUnit.SECONDS.between(authenticationDto.getExpiration(), newExpiration);
		if (seconds < 60) {
			LOG.trace("Authentication [{}] expiration will not be prolonged - expiration differs by [{}]s only.", authenticationDto.getId(), seconds);
			return authenticationDto;
		}
		//
		authenticationDto.setExpiration(newExpiration);
		IdmTokenDto token = tokenManager.getToken(authenticationDto.getId());
		if (token == null) {
			LOG.trace("Persisted token for authentication with id [{}] not found, persisted token expiration will not be prolonged.", authenticationDto.getId());
			return authenticationDto;
		}
		if (token.getExpiration() == null) {
			LOG.trace("Persisted token with id [{}] has unlimited expiration (e.g. system token), expiration will not be changed.", token.getId());
			return authenticationDto;
		}
		//
		// expiration and token attribute has to be updated
		token.setExpiration(newExpiration);
		token.setToken(getTokenHash(token));
		token = tokenManager.saveToken(new IdmIdentityDto(token.getOwnerId()), token);
		//
		return toDto(token);
	}

	public void disableToken(UUID tokenId) {
		tokenManager.disableToken(tokenId);
	}

	/**
	 * Converts authentication.
	 *
	 * @param authentication to dto
	 * @see #saveToken(IdmJwtAuthentication)
	 * @return
	 */
	public IdmJwtAuthenticationDto toDto(IdmJwtAuthentication authentication) {
		Assert.notNull(authentication, "Authentication is required.");
		//
		IdmJwtAuthenticationDto authenticationDto = new IdmJwtAuthenticationDto();
		authenticationDto.setId(authentication.getId());
		authenticationDto.setCurrentUsername(authentication.getCurrentUsername());
		authenticationDto.setCurrentIdentityId(getIdentityId(authentication.getCurrentIdentity()));
		authenticationDto.setOriginalUsername(authentication.getOriginalUsername());
		authenticationDto.setOriginalIdentityId(getIdentityId(authentication.getOriginalIdentity()));
		authenticationDto.setExpiration(authentication.getExpiration());
		authenticationDto.setFromModule(authentication.getFromModule());
		authenticationDto.setIssuedAt(authentication.getIssuedAt());
		//
		return authenticationDto;
	}

	/**
	 * Convert token to authentication dto.
	 *
	 * @param token
	 * @return
	 */
	public IdmJwtAuthenticationDto toDto(IdmTokenDto token) {
		Assert.notNull(token, "Token is required.");
		//
		IdmJwtAuthenticationDto authenticationDto = new IdmJwtAuthenticationDto();
		authenticationDto.setCurrentUsername(token.getProperties().getString(PROPERTY_CURRENT_USERNAME));
		authenticationDto.setCurrentIdentityId(token.getProperties().getUuid(PROPERTY_CURRENT_IDENTITY_ID));
		authenticationDto.setOriginalUsername(token.getProperties().getString(PROPERTY_ORIGINAL_USERNAME));
		authenticationDto.setOriginalIdentityId(token.getProperties().getUuid(PROPERTY_ORIGINAL_IDENTITY_ID));
		authenticationDto.setExpiration(token.getExpiration());
		authenticationDto.setFromModule(token.getModuleId());
		authenticationDto.setIssuedAt(token.getIssuedAt());
		authenticationDto.setId(token.getId());
		//
		return authenticationDto;
	}

	/**
	 * Transforms authentication authorities to list of dtos
	 *
	 * @param authentication
	 * @return
	 */
	public List<DefaultGrantedAuthorityDto> getDtoAuthorities(Authentication authentication) {
		return getDtoAuthorities(authentication.getAuthorities());
	}

	public List<DefaultGrantedAuthorityDto> getDtoAuthorities(Collection<? extends GrantedAuthority> authorities) {
		List<DefaultGrantedAuthorityDto> grantedAuthorities = new ArrayList<>();
		if (authorities != null) {
			for (GrantedAuthority a : authorities) {
				grantedAuthorities.add(new DefaultGrantedAuthorityDto(a.getAuthority()));
			}
		}
		return grantedAuthorities;
	}

	@SuppressWarnings("unchecked")
	public List<DefaultGrantedAuthorityDto> getDtoAuthorities(IdmTokenDto token) {
		List<DefaultGrantedAuthorityDto> authorities = (List<DefaultGrantedAuthorityDto>) token.getProperties().get(PROPERTY_AUTHORITIES);
		if (authorities == null) {
			return new ArrayList<>();
		}
		return authorities;
	}

	public IdmJwtAuthenticationDto getClaims(Jwt jwt) throws IOException {
		return mapper.readValue(jwt.getClaims(), IdmJwtAuthenticationDto.class);
	}

	private UUID getIdentityId(IdmIdentityDto dto) {
		return dto == null ? null : dto.getId();
	}

	/**
	 * Storing raw token is dangerous (could be used for identity login) - hash is stored instead
	 *
	 * @param token
	 * @return
	 * @throws IOException
	 */
	private String getTokenHash(IdmTokenDto token) {
		return Hashing.sha256().hashString(writeToken(toDto(token)), StandardCharsets.UTF_8).toString();
	}

}
