import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import { Advanced, Basic } from 'czechidm-core';
import { SystemManager } from '../../redux';

const manager = new SystemManager();

/**
 * System detail tabs - route
 *
 * @author Radek Tomiška
 */
class System extends Basic.AbstractContent {

  componentDidMount() {
    const { entityId } = this.props.match.params;
    //
    this.context.store.dispatch(manager.fetchEntityIfNeeded(entityId));
  }

  render() {
    const { entity, showLoading } = this.props;
    //
    return (
      <Basic.Div>
        <Advanced.DetailHeader
          icon="component:system"
          entity={ entity }
          showLoading={ !entity && showLoading }
          back={ (entity && entity.virtual === true) ? '/vs/systems' : '/systems' }>
          { this.i18n('acc:content.system.detail.edit.header', { name: manager.getNiceLabel(entity), escape: false }) }
        </Advanced.DetailHeader>
        <Advanced.TabPanel parentId="sys-systems" match={ this.props.match }>
          { this.getRoutes() }
        </Advanced.TabPanel>
      </Basic.Div>
    );
  }
}

System.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
System.defaultProps = {
  entity: null,
  showLoading: false
};

function select(state, component) {
  const { entityId } = component.match.params;
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(System);
