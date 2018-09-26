import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';
import * as Utils from '../utils';

/**
 * Generated values service
 *
 * @author Ondřej Kopr
 */
class GeneratedValueService extends AbstractService {

  getApiPath() {
    return '/generated-values';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.generatorType;
  }

  // dto
  supportsPatch() {
    return false;
  }

  /**
   * Returns default searchParameters for scripts
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('generatorType');
  }

  /**
   * Loads all registered entities wich supports generating
   *
   * @return {promise}
   */
  getSupportedEntities() {
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
   * Loads all available generators
   *
   * @return {promise}
   */
  getAvailableGenerators(entityType) {
    let entityTypeUrl = '/search/generators';
    if (entityType) {
      entityTypeUrl = `${entityTypeUrl}?entityType=${encodeURIComponent(entityType)}`;
    }
    return RestApiService
    .get(this.getApiPath() + entityTypeUrl)
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
}

export default GeneratedValueService;
