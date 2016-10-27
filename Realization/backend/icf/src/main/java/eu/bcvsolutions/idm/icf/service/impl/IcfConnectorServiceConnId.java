package eu.bcvsolutions.idm.icf.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.Uid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.icf.api.IcfConnectorConfiguration;
import eu.bcvsolutions.idm.icf.api.IcfConnectorInfo;
import eu.bcvsolutions.idm.icf.api.IcfConnectorKey;
import eu.bcvsolutions.idm.icf.api.IcfUidAttribute;
import eu.bcvsolutions.idm.icf.exception.IcfException;
import eu.bcvsolutions.idm.icf.service.api.IcfConnectorService;

@Service
public class IcfConnectorServiceConnId implements IcfConnectorService {

	private IcfConfigurationServiceConnId configurationServiceConnId;

	@Autowired
	public IcfConnectorServiceConnId(IcfConnectorAggregatorService icfConnectorAggregator,
			IcfConfigurationServiceConnId configurationServiceConnId) {
		if (icfConnectorAggregator.getIcfConnectors() == null) {
			throw new IcfException("Map of ICF implementations is not defined!");
		}
		if (icfConnectorAggregator.getIcfConnectors().containsKey(this.getIcfType())) {
			throw new IcfException("ICF implementation duplicity for key: " + this.getIcfType());
		}
		icfConnectorAggregator.getIcfConnectors().put(this.getIcfType(), this);
		this.configurationServiceConnId = configurationServiceConnId;
	}

	@Override
	public String getIcfType() {
		return "connId";
	}

	@Override
	public IcfUidAttribute createObject(IcfConnectorKey key, IcfConnectorConfiguration connectorConfiguration,
			List<IcfAttribute> attributes) {
		Assert.notNull(key);
		Assert.notNull(connectorConfiguration);
		ConnectorInfo connIdInfo = configurationServiceConnId.getConnIdConnectorInfo(key);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = IcfConvertUtilConnId.convertIcfConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);

		// Make sure we have set up the Configuration properly
		conn.validate();

		Set<Attribute> connIdAttributes = new HashSet<>();
		if (attributes != null) {
			for (IcfAttribute icfAttribute : attributes) {
				connIdAttributes.add(IcfConvertUtilConnId.convertIcfAttribute(icfAttribute));
			}
		}
		Uid uid =  conn.create(ObjectClass.ACCOUNT, connIdAttributes, null);
		return IcfConvertUtilConnId.convertConnIdUid(uid);
	}

}
