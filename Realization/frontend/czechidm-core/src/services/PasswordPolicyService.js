import AbstractService from './AbstractService';
import SearchParameters from '../domain/SearchParameters';
import RestApiService from './RestApiService';


/**
 * Password policies.
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class PasswordPolicyService extends AbstractService {

  getApiPath() {
    return '/password-policies';
  }

  getNiceLabel(entity) {
    if (!entity) {
      return '';
    }
    return entity.name;
  }

  // dto
  supportsPatch() {
    return false;
  }

  supportsBulkAction() {
    return true;
  }

  /**
   * Returns default searchParameters for password policy
   *
   * @return {object} searchParameters
   */
  getDefaultSearchParameters() {
    return super.getDefaultSearchParameters().setName(SearchParameters.NAME_QUICK).clearSort().setSort('name');
  }

  /**
   * Generates password from password policy with default type
   *
   * @return {Promise}
   */
  generatePassword() {
    return RestApiService.get(`${ this.getApiPath() }/generate/default`);
  }
}

export default PasswordPolicyService;
