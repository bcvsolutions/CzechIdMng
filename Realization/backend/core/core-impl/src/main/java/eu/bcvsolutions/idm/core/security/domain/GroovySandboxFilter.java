package eu.bcvsolutions.idm.core.security.domain;

import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import groovy.lang.Closure;
import groovy.lang.GString;
import groovy.lang.Script;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.runtime.GStringImpl;
import org.kohsuke.groovy.sandbox.GroovyValueFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;

/**
 * This {@link org.kohsuke.groovy.sandbox.GroovyInterceptor} implements a
 * security check.
 *
 * @author Svanda
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class GroovySandboxFilter extends GroovyValueFilter {

	private static final Set<Class<?>> ALLOWED_TYPES = Sets.newHashSet(String.class, Integer.class, Double.class,
			Long.class, Date.class, Enum.class, Boolean.class, BigDecimal.class, UUID.class, Character.class,
			GuardedString.class, DateTimeFormatter.class, org.joda.time.LocalDateTime.class, String[].class,
			LocalDateTime.class, ZonedDateTime.class, java.util.Date.class, ZoneId.class, Instant.class, LocalDate.class, LocalTime.class,
			org.joda.time.LocalDate.class, OffsetTime.class, OffsetDateTime.class, Map.class, HashMap.class, List.class,
			ArrayList.class, Set.class, HashSet.class, LoggerFactory.class, Logger.class,
			ch.qos.logback.classic.Logger.class, GString.class, GStringImpl.class, MessageFormat.class, Arrays.class,
			Collections.class, DtoUtils.class, StringUtils.class, Collection.class);

	private final LinkedList<Set<Class<?>>> allowedCustomTypes = new LinkedList<>();

	public GroovySandboxFilter() {
	}

	public GroovySandboxFilter(Set<Class<?>> allowedTypes) {
		if (allowedTypes != null) {
			allowedCustomTypes.add(allowedTypes);
		}
	}

	public void addCustomTypes(Set<Class<?>> allowedTypes) {
		if (allowedTypes != null) {
			allowedCustomTypes.add(allowedTypes);
		}
	}

	protected Collection<Class<?>> getCustomTypes() {
		return allowedCustomTypes.getLast();
	}

	public Collection<Class<?>> removeLastCustomTypes() {
		return allowedCustomTypes.removeLast();
	}

	public boolean isCustomTypesLast() {
		return this.allowedCustomTypes.size() == 1;
	}

	@Override
	public Object filter(Object o) {
		if (o == null) {
			return o;
		}
		Class<?> targetClass = null;
		if (AopUtils.isAopProxy(o)) {
			targetClass = AopUtils.getTargetClass(o);
		} else if (o instanceof Class<?>) {
			targetClass = (Class<?>) o;
		} else if ((o.getClass()).isArray()) {
			targetClass = o.getClass().getComponentType();
		} else {
			targetClass = o.getClass();
		}
		if (targetClass.isPrimitive()) {
			return o;
		}
		if (ALLOWED_TYPES.contains(targetClass) || getCustomTypes().contains(targetClass)) {
			return o;
		}
		// TODO: check if this is necessary?
		if (o instanceof Class && ALLOWED_TYPES.contains(o) || getCustomTypes().contains(o)) {
			return o;
		}

		Class<?> finalTargetClass = targetClass;
		if (ALLOWED_TYPES.stream()
				.filter(allowedType -> !Object.class.equals(allowedType)) // Access directly via Object is not allowed.
				.filter(allowedType -> allowedType.isAssignableFrom(finalTargetClass))
				.findFirst()
				.isPresent()) {
			return o;
		}

		if (getCustomTypes().stream()
				.filter(allowedType -> !Object.class.equals(allowedType)) // Access directly via Object is not allowed.
				.filter(allowedType -> allowedType.isAssignableFrom(finalTargetClass))
				.findFirst()
				.isPresent()) {
			return o;
		}

		if (o instanceof Script || o instanceof Closure) {
			return o; // access to properties of compiled groovy script
		}
		// check for exceptions
		if (Throwable.class.isAssignableFrom(targetClass)) {
			return o; // access for all exception
		}
		throw new SecurityException(
				MessageFormat.format("Script wants to use unauthorized class: [{0}] ", targetClass));
	}

}
