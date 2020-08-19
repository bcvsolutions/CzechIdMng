package eu.bcvsolutions.idm.core;

import java.io.InputStream;

import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.scheduler.config.AbstractScheduledTaskInitializer;

/**
 * Test initializer for test scheduled tasks.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@SuppressWarnings("deprecation")
@DependsOn("initApplicationData")
public class InitTestScheduledTask extends AbstractScheduledTaskInitializer {

	public static String TEST_MODULE = "core-test";
	private static String TEST_SCHEDULED_TASK_XML = "IdmTestScheduledTasks.xml";
	
	@Override
	protected InputStream getTasksInputStream() {
		return this.getClass().getClassLoader().getResourceAsStream(getTasksXmlPath());
	}

	@Override
	protected String getModule() {
		return TEST_MODULE;
	}

	@Override
	protected String getTasksXmlPath() {
		return DEFAULT_RESOURCE + TEST_SCHEDULED_TASK_XML;
	}

}
