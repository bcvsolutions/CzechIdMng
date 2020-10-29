import RestApiService from './RestApiService';
import AbstractService from './AbstractService';
import * as Utils from '../utils';
import TriggerTypeEnum from '../enums/TriggerTypeEnum';

/**
 * Scheduler administration
 *
 * @author Radek Tomiška
 */
export default class SchedulerService extends AbstractService {

  getApiPath() {
    return '/scheduler-tasks';
  }

  supportsAuthorization() {
    return false;
  }

  getGroupPermission() {
    return 'SCHEDULER';
  }

  /**
   * Returns default searchParameters for current entity type
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().clearSort().setSort('taskType');
  }

  getNiceLabel(entity, showDescription = true) {
    if (!entity) {
      return '';
    }
    let label = this.getSimpleTaskType(entity.taskType);
    if (entity.description && showDescription) {
      label += ` (${Utils.Ui.substringByWord(entity.description, 35, '...')})`;
    }
    return label;
  }

  /**
   * Return simple class name
   *
   * @param  {string} taskType cannonical class name
   * @return {string}
   */
  getSimpleTaskType(taskType) {
    return Utils.Ui.getSimpleJavaType(taskType);
  }

  /**
   * Loads all registered tasks (available for scheduling)
   *
   * @return {promise}
   */
  getSupportedTasks() {
    return RestApiService
      .get(this.getApiPath() + '/search/supported')
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Loads scheduled tasks
   *
   * @return {promise}
   */
  getTasks() {
    return RestApiService
      .get(this.getApiPath())
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Run given task manually
   *
   * @param  {string} taskId
   * @return {promise}
   */
  runTask(taskId) {
    return RestApiService
      .post(this.getApiPath() + `/${taskId}/run`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  dryRunTask(taskId) {
    return RestApiService
      .post(this.getApiPath() + `/${taskId}/dry-run`)
      .then(response => {
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Deletes given trigger
   *
   * @param  {string} taskId
   * @param  {string} triggerId
   * @return {promise}
   */
  deleteTrigger(trigger) {
    return RestApiService
      .delete(this.getApiPath() + `/${trigger.taskId}/triggers/${trigger.id}`)
      .then(response => {
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }

  /**
   * Creates given trigger
   *
   * @param  {string} taskId
   * @param  {string} trigger
   * @return {promise}
   */
  createTrigger(trigger) {
    trigger._type = TriggerTypeEnum.getTriggerType(trigger.type);
    //
    return RestApiService
      .post(this.getApiPath() + `/${trigger.taskId}/triggers/${trigger.type.toLowerCase()}`, trigger)
      .then(response => {
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        if (Utils.Response.hasError(json)) {
          throw Utils.Response.getFirstError(json);
        }
        return json;
      });
  }
}
