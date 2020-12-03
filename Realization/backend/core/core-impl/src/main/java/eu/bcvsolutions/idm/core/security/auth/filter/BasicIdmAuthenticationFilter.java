package eu.bcvsolutions.idm.core.security.auth.filter;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.MustChangePasswordException;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.filter.AbstractAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;

/**
 * Authentication filter implementing Basic authentication scheme.
 * Creates {@link LoginDto} from username and password provided
 * in HTTP request and passes these to {@link AuthenticationManager}.
 * 
 * @author Jan Helbich
 *
 */
@Order(100)
@Component
public class BasicIdmAuthenticationFilter extends AbstractAuthenticationFilter {
	
	private static final Logger LOG = LoggerFactory.getLogger(BasicIdmAuthenticationFilter.class);
	private static final String AUTHORIZATION_TYPE_BASIC_PREFIX = "Basic ";
	//
	@Autowired private AuthenticationManager authManager;
	
	@Override
	public boolean authorize(String token, HttpServletRequest req, HttpServletResponse res) {
		try {
			LoginDto loginDto = createLoginDto(getBasicCredentials(token));
			authManager.authenticate(loginDto);
			LOG.debug("User [{}] successfully logged in.", loginDto.getUsername());
			return true;
		} catch (MustChangePasswordException | TwoFactorAuthenticationRequiredException ex) {
			// publish additional authentication requirement
			throw ex;
		} catch (IdmAuthenticationException e) {
			LOG.warn("Authentication exception raised during basic authentication: [{}].", e.getMessage());
		} catch (Exception e) {
			LOG.warn("Exception was raised during basic authentication: [{}].", e.getMessage());
		}
		return false;
	}
	
	@Override
	public String getAuthorizationHeaderPrefix() {
		return AUTHORIZATION_TYPE_BASIC_PREFIX;
	}
	
	private String[] getBasicCredentials(String token) {
		return new String(Base64.decodeBase64(token), StandardCharsets.UTF_8).split(":");
	}

	private LoginDto createLoginDto(String[] credentials) {
		Assert.notNull(credentials, "Credentials are required.");
		Assert.isTrue(credentials.length == 2, "Credentials username and password is requered.");
		//
		LoginDto ldto = new LoginDto();
		ldto.setUsername(credentials[0]);
		ldto.setPassword(new GuardedString(credentials[1]));
		//
		return ldto;
	}
	
}
