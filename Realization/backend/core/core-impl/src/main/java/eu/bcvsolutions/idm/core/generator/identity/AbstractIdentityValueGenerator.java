package eu.bcvsolutions.idm.core.generator.identity;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Abstrac class for same features that use {@link IdentityUsernameGenerator} and {@link IdentityEmailGenerator}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 9.2.0
 */
public abstract class AbstractIdentityValueGenerator extends AbstractValueGenerator<IdmIdentityDto> {

	public static final String FIRST_NAME_CHARACTERS_COUNT = "firstNameCharacterCount";
	public static final String LAST_NAME_CHARACTERS_COUNT = "lastNameCharacterCount";
	public static final String CONNECTING_CHARACTER = "connectionCharacter";
	public static final String FIRST_NAME_FIRST = "firstNameFirst";
	public static final String REPLACE_WHITE_SPACES_CHARACTER = "replaceWhiteSpacesCharacter";

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(CONNECTING_CHARACTER);
		properties.add(FIRST_NAME_CHARACTERS_COUNT);
		properties.add(LAST_NAME_CHARACTERS_COUNT);
		properties.add(FIRST_NAME_FIRST);
		properties.add(REPLACE_WHITE_SPACES_CHARACTER);
		return properties;
	}

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> attributes = super.getFormAttributes();
		attributes.forEach(attribute -> {
			if (attribute.getName().equals(LAST_NAME_CHARACTERS_COUNT)) {
				attribute.setPersistentType(PersistentType.INT);
			} else if (attribute.getName().equals(FIRST_NAME_CHARACTERS_COUNT)) {
				attribute.setPersistentType(PersistentType.INT);
			} else if (attribute.getName().equals(FIRST_NAME_FIRST)) {
				attribute.setPersistentType(PersistentType.BOOLEAN);
				attribute.setDefaultValue(Boolean.TRUE.toString());
			}
		});
		return attributes;
	}
	
	/**
	 * Method generate username from given dto with settings given by {@link IdmGenerateValueDto}
	 *
	 * @param dto
	 * @param valueGenerator
	 * @return
	 */
	protected String generateUsername(IdmIdentityDto dto, IdmGenerateValueDto valueGenerator) {
		String transformedFirstName = StringUtils.stripAccents(dto.getFirstName());
		String transformedLastName = StringUtils.stripAccents(dto.getLastName());
		//
		if (StringUtils.isEmpty(transformedFirstName) || StringUtils.isEmpty(transformedLastName)) {
			// firstname and lastname is required
			return null;
		}
		//
		Integer firstNameCharacterCount = this.getFirstNameCharacterCount(valueGenerator);
		if (firstNameCharacterCount != null) {
			transformedFirstName = StringUtils.substring(transformedFirstName, 0, firstNameCharacterCount);
		}
		//
		Integer lastNameCharacterCount = this.getLastNameCharacterCount(valueGenerator);
		if (lastNameCharacterCount != null) {
			transformedLastName = StringUtils.substring(transformedLastName, 0, lastNameCharacterCount);
		}
		//
		StringBuilder result = new StringBuilder();
		String firstValue = null;
		String lastValue = null;

		if (isFirstNameFirst(valueGenerator)) {
			firstValue = transformedFirstName;
			lastValue = transformedLastName;
		} else {
			firstValue = transformedLastName;
			lastValue = transformedFirstName;
		}

		result.append(firstValue);
		String connectingCharacter = this.getConnectingCharacter(valueGenerator);
		if (connectingCharacter != null) {
			result.append(connectingCharacter);
		}
		result.append(lastValue);
		//
		// username has more character than accept IdM
		String resultUsername = result.toString().toLowerCase();
		if (resultUsername.length() > DefaultFieldLengths.NAME) {
			// TODO: found better solution
			resultUsername = resultUsername.substring(0, DefaultFieldLengths.NAME);
		}

		return replaceAllWhiteSpaces(valueGenerator, resultUsername);
	}

	/**
	 * Replace all white spaces with character/s given in parameters bulk action. If
	 * whiteSpacesCharacter parameter is null all white spaces will be removed.
	 *
	 * @param valueGenerator
	 * @param string
	 * @return
	 */
	protected String replaceAllWhiteSpaces(IdmGenerateValueDto valueGenerator, String string) {
		if (StringUtils.isEmpty(string)) {
			return string;
		}
		// Check if result string contains white space and remove it.
		String whiteSpacesCharacter = getRemoveWhiteSpacesCharacter(valueGenerator);
		if (StringUtils.isEmpty(whiteSpacesCharacter)) {
			// Empty white spaces, just trim
			return StringUtils.deleteWhitespace(string);
		} else {
			// Replace white spaces with character
			return string.replaceAll("\\s", whiteSpacesCharacter);
		}
	}
	
	/**
	 * Get connection characters
	 *
	 * @return
	 */
	protected String getConnectingCharacter(IdmGenerateValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getString(CONNECTING_CHARACTER);
	}

	/**
	 * Get firstName characters length
	 *
	 * @return
	 */
	protected Integer getFirstNameCharacterCount(IdmGenerateValueDto valueGenerator) {
		Object value = valueGenerator.getGeneratorProperties().getOrDefault(FIRST_NAME_CHARACTERS_COUNT, null);
		if (value == null || StringUtils.isEmpty(value.toString())) {
			return null;
		}
		return Integer.valueOf(value.toString());
	}

	/**
	 * Get lastName characters length
	 *
	 * @return
	 */
	protected Integer getLastNameCharacterCount(IdmGenerateValueDto valueGenerator) {
		Object value = valueGenerator.getGeneratorProperties().getOrDefault(LAST_NAME_CHARACTERS_COUNT, null);
		if (value == null || StringUtils.isEmpty(value.toString())) {
			return null;
		}
		return Integer.valueOf(value.toString());
	}

	/**
	 * Is firstname first
	 *
	 * @param valueGenerator
	 * @return
	 */
	protected boolean isFirstNameFirst(IdmGenerateValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getBoolean(FIRST_NAME_FIRST);
	}

	/**
	 * Remove white spaces character/s.
	 * It is possible that method return null, in this case, just trim all white spaces.
	 *
	 * @param valueGenerator
	 * @return
	 */
	protected String getRemoveWhiteSpacesCharacter(IdmGenerateValueDto valueGenerator) {
		return valueGenerator.getGeneratorProperties().getString(REPLACE_WHITE_SPACES_CHARACTER);
	}
}
