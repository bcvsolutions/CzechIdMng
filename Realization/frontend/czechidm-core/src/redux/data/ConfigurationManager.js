import _ from 'lodash';
import Immutable from 'immutable';
//
import EntityManager from './EntityManager';
import { ConfigurationService } from '../../services';
import DataManager from './DataManager';
import { Actions, Properties } from '../config/constants';

export const EMPTY = 'VOID_ACTION'; // dispatch cannot return null
export const DEFAULT_SIZE_OPTIONS = [10, 25, 50, 100];

/**
 * Application configuration.
 *
 * @author Radek Tomiška
 */
export default class ConfigurationManager extends EntityManager {

  constructor() {
    super();
    this.service = new ConfigurationService();
    this.dataManager = new DataManager();
  }

  getService() {
    return this.service;
  }

  getEntityType() {
    return 'Configuration';
  }

  getCollectionType() {
    return 'configurations';
  }

  fetchPublicConfigurations(cb = null) {
    const uiKey = ConfigurationManager.PUBLIC_CONFIGURATIONS;
    //
    return (dispatch) => {
      dispatch(this.dataManager.requestData(uiKey));
      this.getService().getPublicConfigurations()
        .then(json => {
          let publicConfigurations = new Immutable.Map();
          json.forEach(item => {
            publicConfigurations = publicConfigurations.set(item.name, item);
          });
          // receive public configurations
          dispatch({
            type: Actions.CONFIGURATION_RECEIVED,
            data: publicConfigurations
          });
          dispatch(this.dataManager.stopRequest(uiKey));
          if (cb) {
            cb(publicConfigurations);
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  fetchAllConfigurationsFromFile() {
    const uiKey = ConfigurationManager.FILE_CONFIGURATIONS;
    //
    return (dispatch, getState) => {
      const fileConfigurations = DataManager.getData(getState(), uiKey);
      if (fileConfigurations) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAllConfigurationsFromFile()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }

  fetchAllConfigurationsFromEnvironment() {
    const uiKey = ConfigurationManager.ENVIRONMENT_CONFIGURATIONS;
    //
    return (dispatch, getState) => {
      const environmentConfigurations = DataManager.getData(getState(), uiKey);
      if (environmentConfigurations) {
        // we dont need to load them again - change depends on BE restart
      } else {
        dispatch(this.dataManager.requestData(uiKey));
        this.getService().getAllConfigurationsFromEnvironment()
          .then(json => {
            dispatch(this.dataManager.receiveData(uiKey, json));
          })
          .catch(error => {
            // TODO: data uiKey
            dispatch(this.receiveError(null, uiKey, error));
          });
      }
    };
  }

  /**
   * Returns true, if configurationName should be guarded (contains guarded token, password etc.)
   *
   * @param  {string} configurationName
   * @return {bool}
   */
  shouldBeGuarded(configurationName) {
    return _.intersection(_.split(configurationName, '.'), ConfigurationManager.GUARDED_PROPERTY_NAMES).length > 0;
  }

  /**
   * Returns true, if configurationName should be secured (contains idm.sec. prefix)
   *
   * @param  {string} configurationName
   * @return {bool}
   */
  shouldBeSecured(configurationName) {
    if (!configurationName) {
      return false;
    }
    return configurationName.lastIndexOf('idm.sec.', 0) === 0;
  }

  /**
   * create entities
   *
   * @param  {String} entities - Entities to add
   * @param  {func} cb - function will be called after entity is updated or error occured
   */
  addMoreEntities(entities, uiKey = null, cb = null) {
    if (!entities) {
      return {
        type: EMPTY
      };
    }
    uiKey = this.resolveUiKey(uiKey);
    return (dispatch) => {
      this.getService().addMoreEntities(entities)
        .then(json => {
          if (cb) {
            cb(json, null, uiKey);
          }
        })
        .catch(error => {
          // TODO: data uiKey
          dispatch(this.receiveError(null, uiKey, error, cb));
        });
    };
  }

  /**
   * Returns public setting value
   *
   * @deprecated @since 9.2.2 use getValue
   */
  static getPublicValue(state, key) {
    return ConfigurationManager.getValue(state, key);
  }

  /**
   * Returns setting value from loaded properties
   *
   * @since 9.2.2
   */
  static getValue(state, key) {
    const loadedProperties = state.config.get(Properties.PROPERTIES);
    if (!loadedProperties) {
      return null;
    }
    if (!loadedProperties.has(key)) {
      return null;
    }
    return loadedProperties.get(key).value;
  }

  /**
   * Returns setting value as boolean. Return false, when setting is null,
   * or given value in third parameter defaultValue.
   *
   * @param  {redux} state
   * @param  {string} key
   * @param  {boolean} defautl value
   * @return {boolean}
   */
  static getPublicValueAsBoolean(state, key, defaultValue = false) {
    const publicStringValue = ConfigurationManager.getPublicValue(state, key);
    if (publicStringValue === null || publicStringValue === undefined) {
      return defaultValue;
    }
    return publicStringValue === 'true';
  }

  /**
   * Returns setting value as array of strings. Comma is used as separator
   *
   * @param  {redux} state
   * @param  {string} key
   * @param  {boolean} defautl value
   * @return {array[string]}
   * @since 11.0.0
   */
  static getPublicValueAsArray(state, key, defaultValue = null) {
    const publicStringValue = ConfigurationManager.getPublicValue(state, key);
    if (publicStringValue === null || publicStringValue === undefined) {
      return defaultValue;
    }
    //
    return publicStringValue.split(',').map(column => column.trim());
  }

  /**
   * Returns true, when module is enabled, false when disabled, null when configuration is not found.
   */
  static isModuleEnabled(state, moduleId) {
    const isModuleEnabled = ConfigurationManager.getPublicValue(state, `idm.pub.${moduleId}.enabled`);
    if (isModuleEnabled === null) {
      return null;
    }
    return isModuleEnabled === 'true';
  }

  /**
   * Returns environment stage [development, production, test]
   *
   * @param  {redux} state
   * @return {string} stage [development, production, test]
   */
  static getEnvironmentStage(state) {
    const environment = ConfigurationManager.getPublicValue(state, 'idm.pub.app.stage');
    if (environment) {
      return environment.toLowerCase();
    }
    return null;
  }

  /**
   * Show internal system information.
   *
   * @param  {redux} state redux state
   * @return {boolean}
   */
  static showSystemInformation(state) {
    const userContext = state.security.userContext;
    if (userContext && userContext.profile) {
      return !!userContext.profile.systemInformation;
    }
    //
    return false;
  }

  /**
   * Show internal entity idednifier.
   *
   * @param  {redux} state redux state
   * @return {boolean}
   * @since 10.2.0
   */
  static showId(state) {
    let show = ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.id', null);
    if (show === null || show === undefined) {
      // by app prop
      show = ConfigurationManager.getEnvironmentStage(state) === 'development';
    }
    //
    return show || ConfigurationManager.showSystemInformation(state);
  }

  /**
   * Show user transaction identifier.
   *
   * @param  {redux} state redux state
   * @return {boolean}
   * @since 10.2.0
   */
  static showTransactionId(state) {
    const show = ConfigurationManager.getPublicValueAsBoolean(state, 'idm.pub.app.show.transactionId', false);
    //
    return show || ConfigurationManager.showSystemInformation(state);
  }

  /**
   * Configured size options for table pagination.
   *
   * @param  {redux} state
   * @return {array}
   * @since 10.2.0
   */
  static getSizeOptions(state) {
    const sizeOptionValue = ConfigurationManager.getPublicValue(state, 'idm.pub.app.show.sizeOptions');
    //
    if (!sizeOptionValue) {
      return DEFAULT_SIZE_OPTIONS;
    }
    //
    const sizeOptions = sizeOptionValue
      .split(',')
      .map(size => {
        return parseInt(size.trim(), 10);
      })
      .filter(size => {
        return size !== undefined && size !== null && !_.isNaN(size);
      });
    //
    if (sizeOptions.length === 0) {
      return DEFAULT_SIZE_OPTIONS;
    }
    //
    return sizeOptions;
  }

  /**
   * Show user transaction identifier.
   *
   * @param  {redux} state redux state
   * @return {int} dafault page size
   * @since 10.2.0
   */
  static getDefaultPageSize(state) {
    const userContext = state.security.userContext;
    if (userContext && userContext.profile) {
      return userContext.profile.defaultPageSize;
    }
    //
    return null; // TODO: config loaded default?
  }
}

ConfigurationManager.PUBLIC_CONFIGURATIONS = 'public-configurations'; // ui key only
ConfigurationManager.ENVIRONMENT_CONFIGURATIONS = 'environment-configurations'; // ui key to data redux
ConfigurationManager.FILE_CONFIGURATIONS = 'file-configurations'; // ui key to data redux
ConfigurationManager.GUARDED_PROPERTY_NAMES = ['password', 'token', 'secret']; // automatically guarded property names
