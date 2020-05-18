package eu.bcvsolutions.idm.rpt.report.general;

import org.springframework.context.annotation.Description;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.executor.AbstractReportExecutor;

/**
 * This executor only exists so that {@link AbstractEntityExport} and {@link FormableEntityXlsxRenderer} are able to
 * work together.
 *
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Component
@Description("General export of entities")
@Order(Integer.MAX_VALUE)
public class FormableEntityReportExecutor extends AbstractReportExecutor {

	public static final String NAME = "formable-entity-report";

	@Override
	protected IdmAttachmentDto generateData(RptReportDto report) {
		return null;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public boolean isDisabled() {
		return true;
	}

}
