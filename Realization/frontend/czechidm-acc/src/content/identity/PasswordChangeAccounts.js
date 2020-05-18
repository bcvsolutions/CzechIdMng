import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import { Basic, Domain, Utils, Managers } from 'czechidm-core';
import PasswordChangeForm from 'czechidm-core/src/content/identity/PasswordChangeForm';
import { AccountManager } from '../../redux';
//
const IDM_NAME = Utils.Config.getConfig('app.name', 'CzechIdM');
const RESOURCE_IDM = `0:${IDM_NAME}`;
//
const accountManager = new AccountManager();
const identityManager = new Managers.IdentityManager();

/**
 * In this component include password change and send props with account options
 *
 * @author Ondřej Kopr
 */
class PasswordChangeAccounts extends Basic.AbstractContent {

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const defaultSearchParameters = accountManager
      .getDefaultSearchParameters()
      .setName(Domain.SearchParameters.NAME_AUTOCOMPLETE)
      .setFilter('ownership', true)
      .setFilter('supportChangePassword', true)
      .setFilter('identity', entityId);
    this.context.store.dispatch(accountManager.fetchEntities(defaultSearchParameters, `${ entityId }-accounts`, (accounts, error) => {
      // Prevent to show error, when logged identity cannot read identity accounts => password change for IdM only.
      if (error && error.statusCode !== 403) {
        this.addError(error);
      }
    }));
  }

  _getOptions() {
    const { entityId } = this.props.match.params;
    const { accounts, showLoading } = this.props;

    if (showLoading) {
      return null;
    }

    const identity = identityManager.getEntity(this.context.store.getState(), entityId);
    const options = [
      { value: RESOURCE_IDM, niceLabel: `${ IDM_NAME }${ identity ? ` (${ identity.username })` : '' }` }
    ];

    accounts.forEach(acc => {
      // Skip account in protection
      if (acc.inProtection) {
        return;
      }
      const niceLabel = `${ acc._embedded.system.name } (${ acc.uid })`;
      options.push({
        value: acc.id,
        niceLabel
      });
    });

    return options;
  }

  render() {
    const { passwordChangeType, userContext, requireOldPassword, showLoading } = this.props;
    const { entityId } = this.props.match.params;
    const options = this._getOptions();
    //
    return (
      <Basic.Div>
        {
          showLoading
          ?
          <Basic.Loading isStatic show/>
          :
          <PasswordChangeForm
            userContext={ userContext }
            entityId={ entityId }
            passwordChangeType={ passwordChangeType }
            requireOldPassword={ requireOldPassword }
            accountOptions={ options }/>
        }
      </Basic.Div>
    );
  }

}

PasswordChangeAccounts.propTypes = {
  showLoading: PropTypes.bool,
  userContext: PropTypes.object,
  accounts: PropTypes.object
};
PasswordChangeAccounts.defaultProps = {
  userContext: null,
  showLoading: true,
  accounts: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    userContext: state.security.userContext,
    accounts: accountManager.getEntities(state, `${entityId}-accounts`),
    showLoading: accountManager.isShowLoading(state, `${entityId}-accounts`)
  };
}
export default connect(select)(PasswordChangeAccounts);
