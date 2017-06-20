package eu.bcvsolutions.idm.core.security.rest.impl;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginRequestDto;
import eu.bcvsolutions.idm.core.security.service.LoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Identity authentication
 * 
 * @author Radek Tomiška 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/authentication", produces = "application/hal+json")
@Api(value = "Authentication", description = "Authentication endpoint", tags = { "Authentication" })
public class LoginController {
	
	public static final String REMOTE_AUTH_PATH = "/remote-auth";
	//
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private LoginService loginService;
	
	@ResponseBody
	@ApiOperation(
			value = "Login an get the CIDMST token", 
			notes= "Login an get the CIDMST token",
			response = LoginDto.class,
			tags = { "Authentication" } )
	@RequestMapping(method = RequestMethod.POST)
	public Resource<LoginDto> login(@Valid @RequestBody(required = true) LoginRequestDto loginDto) {
		if(loginDto == null || loginDto.getUsername() == null || loginDto.getPassword() == null){
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Username and password must be filled");
		}
		return new Resource<LoginDto>(authenticationManager.authenticate(new LoginDto(loginDto)));
	}
	
	@ApiOperation(
			value = "Login with remote token", 
			notes= "Login with remote token an get the CIDMST token",
			response = LoginDto.class,
			tags = { "Authentication" })
	@RequestMapping(path = REMOTE_AUTH_PATH, method = RequestMethod.GET)
	public Resource<LoginDto> loginWithRemoteToken() {
		return new Resource<LoginDto>(loginService.loginAuthenticatedUser());
	}
	
}
