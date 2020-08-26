import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../components/basic';
import { RoleManager} from '../../redux';
import RoleDetail from './RoleDetail';

let roleManager = null;

/**
 * Role content with role form - first tab on role detail
 *
 * @author Radek Tomiška
 */
class Content extends Basic.AbstractContent {

  getContentKey() {
    return 'content.roles';
  }

  getNavigationKey() {
    return this.getRequestNavigationKey('role-detail', this.props.match.params);
  }

  componentDidMount() {
    super.componentDidMount();
    const { entityId } = this.props.match.params;

    // Init manager - evaluates if we want to use standard (original) manager or
    // universal request manager (depends on existing of 'requestId' param)
    roleManager = this.getRequestManager(this.props.match.params, new RoleManager());
    if (this._isNew()) {
      this.context.store.dispatch(roleManager.receiveEntity(entityId, { }));
    } else {
      this.context.store.dispatch(roleManager.fetchEntity(entityId, null, (entity, error) => {
        this.handleError(error);
      }));
    }
  }

  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { role, showLoading } = this.props;
    if (!roleManager) {
      return null;
    }
    return (
      <Basic.Row>
        <div className={ this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12' }>
          {
            !role
            ||
            <RoleDetail entity={role} showLoading={showLoading} match={ this.props.match }/>
          }
        </div>
      </Basic.Row>
    );
  }
}
Content.propTypes = {
};

Content.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  if (!roleManager) {
    return null;
  }
  return {
    role: roleManager.getEntity(state, entityId),
    showLoading: roleManager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(Content);
