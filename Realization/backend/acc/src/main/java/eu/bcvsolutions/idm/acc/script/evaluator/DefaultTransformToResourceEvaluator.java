package eu.bcvsolutions.idm.acc.script.evaluator;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;

/**
 * Default evaluator for {@link IdmScriptCategory} TRANSFORM_TO resource.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service("transformToResourceEvaluator")
public class DefaultTransformToResourceEvaluator extends AbstractScriptEvaluator {

	@Override
	public boolean supports(IdmScriptCategory category) {
		return category == IdmScriptCategory.TRANSFORM_TO;
	}

	@Override
	public String generateTemplate(IdmScriptDto script) {
		StringBuilder example = new StringBuilder();
		//
		example.append("// Inserted script: ");
		example.append(script.getCode());
		example.append('\n');
		//
		example.append("/* Description:\n");
		example.append(script.getDescription());
		example.append('\n');
		//
		example.append("*/\n");
		example.append(SCRIPT_EVALUATOR);
		example.append(".evaluate(\n");
		//
		example.append("    scriptEvaluator.newBuilder()\n");
		//
		example.append("        .setScriptCode('");
		example.append(script.getCode());
		example.append("')\n");
		//
		example.append("        .addParameter('");
		example.append(SCRIPT_EVALUATOR);
		example.append("', ");
		example.append(SCRIPT_EVALUATOR);
		example.append(")\n");
		//
		example.append("        .addParameter('");
		example.append(SysSystemAttributeMappingService.ACCOUNT_UID);
		example.append("', ");
		example.append(SysSystemAttributeMappingService.ACCOUNT_UID);
		example.append(")\n");
		//
		example.append("        .addParameter('");
		example.append(SysSystemAttributeMappingService.ATTRIBUTE_VALUE_KEY);
		example.append("', ");
		example.append(SysSystemAttributeMappingService.ATTRIBUTE_VALUE_KEY);
		example.append(")\n");
		//
		example.append("        .addParameter('");
		example.append(SysSystemAttributeMappingService.ENTITY_KEY);
		example.append("', ");
		example.append(SysSystemAttributeMappingService.ENTITY_KEY);
		example.append(")\n");
		//
		example.append("        .addParameter('");
		example.append(SysSystemAttributeMappingService.SYSTEM_KEY);
		example.append("', ");
		example.append(SysSystemAttributeMappingService.SYSTEM_KEY);
		example.append(")\n");
		//
		example.append("        .addParameter('");
		example.append(SysSystemAttributeMappingService.CONTEXT_KEY);
		example.append("', ");
		example.append(SysSystemAttributeMappingService.CONTEXT_KEY);
		example.append(")\n");
		//
		example.append("	.build());\n");
		return example.toString();
	}
}
