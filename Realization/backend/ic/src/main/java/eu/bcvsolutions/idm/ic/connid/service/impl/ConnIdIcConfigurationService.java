package eu.bcvsolutions.idm.ic.connid.service.impl;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorFacade;
import org.identityconnectors.framework.api.ConnectorFacadeFactory;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.api.ConnectorInfoManager;
import org.identityconnectors.framework.api.ConnectorInfoManagerFactory;
import org.identityconnectors.framework.api.ConnectorKey;
import org.identityconnectors.framework.api.RemoteFrameworkConnectionInfo;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidCredentialException;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VFSUtils;
import org.jboss.vfs.VirtualFile;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcConnector;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorInfo;
import eu.bcvsolutions.idm.ic.api.IcConnectorInstance;
import eu.bcvsolutions.idm.ic.api.IcConnectorServer;
import eu.bcvsolutions.idm.ic.api.IcSchema;
import eu.bcvsolutions.idm.ic.connid.domain.ConnIdIcConvertUtil;
import eu.bcvsolutions.idm.ic.exception.IcCantConnectException;
import eu.bcvsolutions.idm.ic.exception.IcInvalidCredentialException;
import eu.bcvsolutions.idm.ic.exception.IcRemoteServerException;
import eu.bcvsolutions.idm.ic.exception.IcServerNotFoundException;
import eu.bcvsolutions.idm.ic.impl.IcConnectorInfoImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorKeyImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConfigurationService;

@Service

/**
 * Configuration connector service for ConnId framework
 * 
 * @author svandav
 *
 */
public class ConnIdIcConfigurationService implements IcConfigurationService {

	private static final Logger LOG = LoggerFactory.getLogger(ConnIdIcConfigurationService.class);

	// Cached local connid managers
	private List<ConnectorInfoManager> managers;
	@Value("#{'${ic.localconnector.packages}'.split(',')}")
	private List<String> localConnectorsPackages;

	final private static String IMPLEMENTATION_TYPE = "connId";
	final private static String URL_PROTOCOL_FILE = "file";
	final private static String URL_PROTOCOL_VFS = "vfs";

	/**
	 * Return key defined IC implementation
	 * 
	 * @return
	 */
	@Override
	public String getFramework() {
		return IMPLEMENTATION_TYPE;
	}

	/**
	 * Return available local connectors for this IC implementation
	 * 
	 * @return
	 */
	@Override
	public Set<IcConnectorInfo> getAvailableLocalConnectors() {
		LOG.info("Get Available local connectors - ConnId");
		Set<IcConnectorInfo> localConnectorInfos = new HashSet<>();
		List<ConnectorInfoManager> managers = findAllLocalConnectorManagers();
		LOG.info(MessageFormat.format("Managers of local ConnId connectors [{0}]", managers));

		for (ConnectorInfoManager manager : managers) {
			List<ConnectorInfo> infos = manager.getConnectorInfos();
			if (infos == null) {
				continue;
			}
			for (ConnectorInfo info : infos) {
				ConnectorKey key = info.getConnectorKey();
				if (key == null) {
					continue;
				}
				IcConnectorKeyImpl keyDto = new IcConnectorKeyImpl(getFramework(), key.getBundleName(),
						key.getBundleVersion(), key.getConnectorName());
				IcConnectorInfoImpl infoDto = new IcConnectorInfoImpl(info.getConnectorDisplayName(),
						info.getConnectorCategory(), keyDto);
				localConnectorInfos.add(infoDto);
			}
		}
		return localConnectorInfos;
	}

	/**
	 * Return find connector default configuration by connector info
	 * 
	 * @param key
	 * @return
	 */
	@Override
	public IcConnectorConfiguration getConnectorConfiguration(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		ConnectorInfo i = null;
		if (connectorInstance.isRemote()) {
			i = getRemoteConnIdConnectorInfo(connectorInstance);
		} else {
			i = getConnIdConnectorInfo(connectorInstance);
		}

		if (i != null) {
			APIConfiguration apiConf = i.createDefaultAPIConfiguration();
			return ConnIdIcConvertUtil.convertConnIdConnectorConfiguration(apiConf);
		}
		return null;
	}

	private List<ConnectorInfo> getAllRemoteConnectors(IcConnectorServer server) {
		ConnectorInfoManager remoteInfoManager = findRemoteConnectorManager(server);
		//
		return remoteInfoManager.getConnectorInfos();
	}

	@Override
	public Set<IcConnectorInfo> getAvailableRemoteConnectors(IcConnectorServer server) {
		Assert.notNull(server, "Connector server is required.");
		//
		List<ConnectorInfo> infos = getAllRemoteConnectors(server);
		//
		Set<IcConnectorInfo> result = new HashSet<>(infos.size());

		for (ConnectorInfo info : infos) {
			ConnectorKey key = info.getConnectorKey();
			if (key == null) {
				continue;
			}
			// transform
			IcConnectorKeyImpl keyDto = new IcConnectorKeyImpl(getFramework(), key.getBundleName(),
					key.getBundleVersion(), key.getConnectorName());
			IcConnectorInfoImpl infoDto = new IcConnectorInfoImpl(info.getConnectorDisplayName(),
					info.getConnectorCategory(), keyDto);

			result.add(infoDto);
		}

		return result;
	}

	@Override
	public IcConnectorConfiguration getRemoteConnectorConfiguration(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorInstance.getConnectorServer(), "Connector server is required.");
		ConnectorInfo info = getRemoteConnIdConnectorInfo(connectorInstance);
		//
		if (info != null) {
			APIConfiguration apiConfiguration = info.createDefaultAPIConfiguration();
			// TODO: same as local???
			return ConnIdIcConvertUtil.convertConnIdConnectorConfiguration(apiConfiguration);
		}
		//
		return null;
	}

	private ConnectorInfo getRemoteConnIdConnectorInfo(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorInstance.getConnectorServer(), "Connector server is required.");
		ConnectorInfoManager remoteInfoManager = findRemoteConnectorManager(connectorInstance.getConnectorServer());

		for (ConnectorInfo info : remoteInfoManager.getConnectorInfos()) {
			ConnectorKey connectorKey = info.getConnectorKey();
			if (connectorKey == null) {
				continue;
			} else if (connectorKey.getConnectorName().equals(connectorInstance.getConnectorKey().getConnectorName())) {
				return info;
			}
		}

		return null;
	}

	public ConnectorInfo getConnIdConnectorInfo(IcConnectorInstance connectorInstance) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		if (connectorInstance.isRemote()) {
			Assert.notNull(connectorInstance.getConnectorServer(), "Connector server is required.");
			return getRemoteConnIdConnectorInfo(connectorInstance);
		} else {
			for (ConnectorInfoManager manager : findAllLocalConnectorManagers()) {
				ConnectorInfo i = manager.findConnectorInfo(ConnIdIcConvertUtil
						.convertConnectorKeyFromDto(connectorInstance.getConnectorKey(), this.getFramework()));
				if (i != null) {
					return i;
				}
			}
		}
		return null;
	}

	@Override
	public void validate(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		if (connectorInstance.isRemote()) {
			LOG.debug("Validate remote connector - ConnId ({})",
					connectorInstance.getConnectorServer().getFullServerName());
		} else {
			LOG.debug("Validate connector - ConnId ({})", connectorInstance.getConnectorKey().toString());
		}
		// Validation is in getConnectorFacade method
		getConnectorFacade(connectorInstance, connectorConfiguration);

	}

	@Override
	public void test(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		if (connectorInstance.isRemote()) {
			Assert.notNull(connectorInstance.getConnectorServer(), "Connector server is required.");
			LOG.debug("Validate remote connector - ConnId ({})",
					connectorInstance.getConnectorServer().getFullServerName());
		} else {
			LOG.debug("Validate connector - ConnId ({})", connectorInstance.getConnectorKey().toString());
		}
		// Validation is in getConnectorFacade method
		getConnectorFacade(connectorInstance, connectorConfiguration).test();

	}

	@Override
	public IcSchema getSchema(IcConnectorInstance connectorInstance, IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		if (connectorInstance.isRemote()) {
			LOG.info(MessageFormat.format("Get Schema of remote connector - ConnId ({0})",
					connectorInstance.getConnectorServer().getFullServerName()));
		} else {
			LOG.info(MessageFormat.format("Get Schema - ConnId ({0})", connectorInstance.getConnectorKey().toString()));
		}
		ConnectorFacade conn = getConnectorFacade(connectorInstance, connectorConfiguration);

		Schema schema = conn.schema();
		return ConnIdIcConvertUtil.convertConnIdSchema(schema);
	}

	private ConnectorInfoManager findRemoteConnectorManager(IcConnectorServer server) {
		// get all saved remote connector servers
		ConnectorInfoManager manager = null;
		try {
		GuardedString pass = server.getPassword();
		if (pass == null) {
			throw new InvalidCredentialException();
		}
		RemoteFrameworkConnectionInfo info = new RemoteFrameworkConnectionInfo(server.getHost(), server.getPort(),
				new org.identityconnectors.common.security.GuardedString(pass.asString().toCharArray()),
				server.isUseSsl(), null, server.getTimeout());
			// flush remote cache
			ConnectorInfoManagerFactory instance = ConnectorInfoManagerFactory.getInstance();
			instance.clearRemoteCache();
			manager = instance.getRemoteManager(info);
		} catch (InvalidCredentialException e) {
			throw new IcInvalidCredentialException(server.getHost(), server.getPort(), e);
		} catch (ConnectorIOException e) {
			throw new IcServerNotFoundException(server.getHost(), server.getPort(), e);
		} catch (ConnectorException e) {
			throw new IcCantConnectException(server.getHost(), server.getPort(), e);
		} catch (Exception e) {
			throw new IcRemoteServerException(server.getHost(), server.getPort(), e);
		}

		return manager;
	}

	private List<ConnectorInfoManager> findAllLocalConnectorManagers() {
		if (managers == null) {
			List<Class<?>> annotated = new ArrayList<>();
			// Find all class with annotation IcConnectorClass under specific
			// packages
			localConnectorsPackages.forEach(packageWithConnectors -> {
				Reflections reflections = new Reflections(packageWithConnectors);
				annotated.addAll(reflections.getTypesAnnotatedWith(ConnectorClass.class));
			});
			
			LOG.info(MessageFormat.format("Found annotated classes with IcConnectorClass [{0}]", annotated));
			
			managers = new ArrayList<>(annotated.size());
			for (Class<?> clazz : annotated) {
				URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
				// Transformation the Url to standard Java Url (for the JBoss VFS problem)
				URL resultUrl = toStandardJavaUrl(url);
				ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory.getInstance();
				ConnectorInfoManager manager = fact.getLocalManager(resultUrl);
				managers.add(manager);
			}
			LOG.info(MessageFormat.format("Found all local connector managers [{0}]", managers.toString()));
		}
		return managers;
	}

	/**
	 * Transformation the Url to standard Java Url (for the JBoss VFS problem)
	 * 
	 * We have to create new instance of URL, because we don't want use the URL with
	 * 'vfs' protocol. This happens on the WildFly server (where are connectors
	 * copied to temp folder -> problem with non exists MANIFEST in the ConnId).
	 * 
	 * @param url
	 * @return
	 */
	private URL toStandardJavaUrl(URL url) {
		if (url == null) {
			return null;
		}
		if (URL_PROTOCOL_VFS.equals(url.getProtocol())) {
			try {
				VirtualFile vf = VFS.getChild(url.toURI());

				URL physicalUrl = VFSUtils.getPhysicalURL(vf);
				// Workaround ... replace contents folder with name of file
				URL resultUrl = new URL(URL_PROTOCOL_FILE, physicalUrl.getHost(),
						physicalUrl.getFile().replace("/contents/", "/" + vf.getName()));

				return resultUrl;
			} catch (Exception e) {
				throw new CoreException("JBoss VFS URL transformation failed", e);
			}
		}
		return url;
	}

	private ConnectorFacade getConnectorFacade(IcConnectorInstance connectorInstance,
			IcConnectorConfiguration connectorConfiguration) {
		Assert.notNull(connectorInstance.getConnectorKey(), "Connector key is required.");
		Assert.notNull(connectorConfiguration, "Configuration is required.");
		if (connectorInstance.isRemote()) {
			Assert.notNull(connectorInstance.getConnectorServer(), "Connector server is required.");
		}
		ConnectorInfo connIdInfo = this.getConnIdConnectorInfo(connectorInstance);
		Assert.notNull(connIdInfo, "ConnId connector info not found!");
		APIConfiguration config = connIdInfo.createDefaultAPIConfiguration();
		Assert.notNull(config.getConfigurationProperties(), "ConnId connector configuration properties not found!");
		config = ConnIdIcConvertUtil.convertIcConnectorConfiguration(connectorConfiguration, config);
		// Use the ConnectorFacadeFactory's newInstance() method to get a new
		// connector.
		ConnectorFacade conn = ConnectorFacadeFactory.getManagedInstance().newInstance(config);

		// Make sure we have set up the Configuration properly
		conn.validate();
		return conn;
	}

	@Override
	public Class<? extends IcConnector> getConnectorClass(IcConnectorInstance connectorInstance) {
		return null;
	}
}
