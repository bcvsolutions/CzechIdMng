package eu.bcvsolutions.idm.core.security.evaluator.eav;

import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmCodeListItemDto;
import eu.bcvsolutions.idm.core.eav.api.service.CodeListManager;
import eu.bcvsolutions.idm.core.eav.api.service.IdmCodeListItemService;
import eu.bcvsolutions.idm.core.eav.entity.IdmCodeListItem;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Authorization policy evaluator test.
 * 
 * @author Radek Tomiška
 */
@Transactional
public class CodeListItemByCodeEvaluatorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private CodeListManager codeListManager;
	@Autowired private IdmCodeListItemService codeListItemService;
	
	@Test
	public void testPermissions() {
		// create codelist and items
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmCodeListDto codeListOne = codeListManager.create(getHelper().createName());
		IdmCodeListItemDto itemOne = codeListManager.createItem(codeListOne.getId(), getHelper().createName(), getHelper().createName());
		IdmCodeListItemDto itemTwo = codeListManager.createItem(codeListOne.getId(), getHelper().createName(), getHelper().createName());
		IdmCodeListDto codeListTwo = codeListManager.create(getHelper().createName());
		codeListManager.createItem(codeListTwo.getId(), getHelper().createName(), getHelper().createName()); // other
		//
		List<IdmCodeListItemDto> items = null;
		IdmRoleDto roleOne = getHelper().createRole();
		//
		getHelper().createIdentityRole(identity, roleOne);
		//
		// check - read without policy
		try {			
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			items = codeListItemService.find(null, IdmBasePermission.AUTOCOMPLETE).getContent();
			Assert.assertTrue(items.isEmpty());	
		} finally {
			logout();
		}
		//
		// without login
		items = codeListItemService.find(null, IdmBasePermission.AUTOCOMPLETE).getContent();
		Assert.assertTrue(items.isEmpty());
		//
		// create authorization policies - assign to role
		getHelper().createUuidPolicy(roleOne.getId(), codeListOne.getId(), IdmBasePermission.AUTOCOMPLETE);
		ConfigurationMap properties = new ConfigurationMap();
		properties.put(CodeListItemByCodeEvaluator.PARAMETER_CODELIST, codeListOne.getId());
		properties.put(CodeListItemByCodeEvaluator.PARAMETER_ITEM_CODES, itemOne.getCode());
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.CODELISTITEM,
				IdmCodeListItem.class,
				CodeListItemByCodeEvaluator.class,
				properties,
				IdmBasePermission.AUTOCOMPLETE);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			// without read permission
			items = codeListItemService.find(null, IdmBasePermission.READ).getContent();
			Assert.assertTrue(items.isEmpty());
			//
			// evaluate	access
			items = codeListItemService.find(null, IdmBasePermission.AUTOCOMPLETE).getContent();
			Assert.assertEquals(1, items.size());	
			Assert.assertEquals(itemOne.getId(), items.get(0).getId());
			//
			Set<String> permissions = codeListItemService.getPermissions(itemOne);
			Assert.assertEquals(1, permissions.size());
			Assert.assertTrue(permissions.stream().anyMatch(p -> p.equals(IdmBasePermission.AUTOCOMPLETE.name())));
		} finally {
			logout();
		}
		// all items by default
		properties = new ConfigurationMap();
		properties.put(CodeListItemByCodeEvaluator.PARAMETER_CODELIST, codeListOne.getId());
		getHelper().createAuthorizationPolicy(
				roleOne.getId(),
				CoreGroupPermission.CODELISTITEM,
				IdmCodeListItem.class,
				CodeListItemByCodeEvaluator.class,
				properties,
				IdmBasePermission.AUTOCOMPLETE);
		//
		try {
			getHelper().login(identity.getUsername(), identity.getPassword());
			//
			items = codeListItemService.find(null, IdmBasePermission.AUTOCOMPLETE).getContent();
			Assert.assertEquals(2, items.size());	
			Assert.assertTrue(items.stream().anyMatch(i -> i.getId().equals(itemOne.getId())));
			Assert.assertTrue(items.stream().anyMatch(i -> i.getId().equals(itemTwo.getId())));
		} finally {
			logout();
		}
	}

}
