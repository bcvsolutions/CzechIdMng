import Immutable from 'immutable';
//
import { Managers, Domain } from 'czechidm-core';
import { SystemService } from '../services';

/**
 * Manager for a target systems
 *
 * @author Vít Švanda
 * @author Radek Tomiška
 * @author Peter Štrunc
 */
export default class SystemManager extends Managers.EntityManager {

  constructor() {
    super();
    this.service = new SystemService();
  }

  getModule() {
    return 'acc';
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'System';
  }

  getCollectionType() {
    return 'systems';
  }

  /**
   * Load connector configuration for given system
   *
   * @param  {string} id system identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchConnectorConfiguration(id, uiKey, cb = null) {
    const getFormDefinitionFun = (identifier) => {
      return this.getService().getConnectorFormDefinition(identifier);
    };
    const getFormValuesFun = (identifier) => {
      return this.getService().getConnectorFormValues(identifier);
    };

    return this.fetchConfiguration(id, uiKey, getFormDefinitionFun, getFormValuesFun, cb);
  }

  /**
   * Load pooling connector configuration for given system
   *
   * @param  {string} id system identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchPoolingConnectorConfiguration(id, uiKey, cb = null) {
    const getFormDefinitionFun = (identifier) => {
      return this.getService().getPoolingConnectorFormDefinition(identifier);
    };
    const getFormValuesFun = (identifier) => {
      return this.getService().getPoolingConnectorFormValues(identifier);
    };

    return this.fetchConfiguration(id, uiKey, getFormDefinitionFun, getFormValuesFun, cb);
  }

  /**
   * Load operation options connector configuration for given system
   *
   * @param  {string} id system identifier
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  fetchOperationOptionsConnectorConfiguration(id, uiKey, cb = null) {
    const getFormDefinitionFun = (identifier) => {
      return this.getService().getOperationOptionsConnectorFormDefinition(identifier);
    };
    const getFormValuesFun = (identifier) => {
      return this.getService().getOperationOptionsConnectorFormValues(identifier);
    };

    return this.fetchConfiguration(id, uiKey, getFormDefinitionFun, getFormValuesFun, cb);
  }


  fetchConfiguration(id, uiKey, getFormDefinitionFun, getFormValuesFun, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));

      const connectorFormDefinitionPromise = getFormDefinitionFun(id);
      const connectorFormValuesPromise = getFormValuesFun(id);

      Promise.all([connectorFormDefinitionPromise, connectorFormValuesPromise])
        .then((jsons) => {
          const formDefinition = jsons[0];
          const formValues = jsons[1].values;

          const formInstance = new Domain.FormInstance(formDefinition, formValues);

          dispatch(this.dataManager.receiveData(uiKey, formInstance));
          if (cb) {
            cb(formInstance);
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Saves connector configuration form values
   *
   * @param  {string} id system identifier
   * @param  {arrayOf(entity)} values filled form values
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  saveConnectorConfiguration(id, values, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().saveConnectorFormValues(id, values)
        .then(() => {
          dispatch(this.fetchConnectorConfiguration(id, uiKey, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Saves pooling connector configuration form values
   *
   * @param  {string} id system identifier
   * @param  {arrayOf(entity)} values filled form values
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  savePoolingConnectorConfiguration(id, values, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().savePoolingConnectorFormValues(id, values)
        .then(() => {
          dispatch(this.fetchPoolingConnectorConfiguration(id, uiKey, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Saves connector configuration form values for operation options
   *
   * @param  {string} id system identifier
   * @param  {arrayOf(entity)} values filled form values
   * @param {string} uiKey
   * @param {func} cb callback
   * @returns {action}
   */
  saveOperationOptionsConnectorConfiguration(id, values, uiKey, cb = null) {
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().saveOperationOptionsConnectorFormValues(id, values)
        .then(() => {
          dispatch(this.fetchOperationOptionsConnectorConfiguration(id, uiKey, cb));
        })
        .catch(error => {
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   *  Fetch all available framworks and their connectors and put them to redux data
   *
   * @return {action}
   */
  fetchAvailableFrameworks() {
    const uiKey = SystemManager.AVAILABLE_CONNECTORS;
    //
    return (dispatch, getState) => {
      let availableFrameworks = Managers.DataManager.getData(getState(), uiKey);
      if (availableFrameworks) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAvailableConnectors()
          .then(json => {
            availableFrameworks = new Immutable.Map();
            for (const framework in json) {
              if (!json.hasOwnProperty(framework)) {
                continue;
              }
              let availableConnectors = new Immutable.Map();
              json[framework].forEach(connector => {
                availableConnectors = availableConnectors.set(connector.connectorKey.fullName, connector);
              });
              availableFrameworks = availableFrameworks.set(framework, availableConnectors);
            }
            dispatch(this.dataManager.receiveData(uiKey, availableFrameworks));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }

  /**
   * Fetch available remote connectors by framewrok
   * cb - callback
   */
  fetchAvailableRemoteConnector(systemId, cb) {
    const uiKey = SystemManager.AVAILABLE_REMOTE_CONNECTORS;
    //
    return (dispatch) => {
      let availableFrameworks = new Immutable.Map();
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getAvailableRemoteConnectors(systemId)
        .then(json => {
          for (const framework in json) {
            if (!json.hasOwnProperty(framework)) {
              continue;
            }
            let availableConnectors = new Immutable.Map();
            if (json[framework] == null) {
              continue;
            }
            json[framework].forEach(connector => {
              availableConnectors = availableConnectors.set(connector.connectorKey.fullName, connector);
            });
            availableFrameworks = availableFrameworks.set(framework, availableConnectors);
          }
          if (cb) {
            cb(availableFrameworks);
          }
          dispatch(this.dataManager.receiveData(uiKey, availableFrameworks));
        })
        .catch(error => {
          if (cb) {
            cb(null, error);
          }
          dispatch(this.receiveError(null, uiKey, error));
        });
    };
  }

  /**
   * Loads all registered connector types.
   *
   * @return {action}
   */
  fetchSupportedTypes() {
    const uiKey = SystemManager.UI_KEY_SUPPORTED_TYPES;
    //
    return (dispatch, getState) => {
      const loaded = Managers.DataManager.getData(getState(), uiKey);
      if (loaded) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.getDataManager().requestData(uiKey));
        this.getService().getSupportedTypes()
          .then(json => {
            let types = new Immutable.Map();
            if (json._embedded && json._embedded.connectorTypes) {
              json._embedded.connectorTypes.forEach(item => {
                types = types.set(item.id, item);
              });
            }
            dispatch(this.getDataManager().receiveData(uiKey, types));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.getDataManager().receiveError(null, uiKey, error));
          });
      }
    };
  }
}

SystemManager.AVAILABLE_CONNECTORS = 'connectors-available';
SystemManager.AVAILABLE_REMOTE_CONNECTORS = 'remote-connectors-available';
SystemManager.UI_KEY_SUPPORTED_TYPES = 'connector-supported-types';
