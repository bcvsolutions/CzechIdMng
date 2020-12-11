import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import { Basic } from 'czechidm-core';
import { VsRequestManager } from '../../redux';
import VsRequestDetail from './VsRequestDetail';

const manager = new VsRequestManager();

/**
 * Virtual system request detail wrapper
 *
 * @author Vít Švanda
 */
class VsRequestContent extends Basic.AbstractContent {

  /**
   * "Shorcut" for localization
   */
  getContentKey() {
    return 'vs:content.vs-request.detail';
  }

  /**
   * Selected navigation item
   */
  getNavigationKey() {
    return 'vs-requests';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    if (this._isNew()) {
      // persist new entity to redux
      this.context.store.dispatch(manager.receiveEntity(entityId, { }));
    } else {
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  // @Deprecated - since V10 ... replaced by dynamic key in Route
  // UNSAFE_componentWillReceiveProps(nextProps) {
  //   const { entityId } = this.props.match.params;
  //   if (entityId && nextProps.match.params.entityId && entityId !== nextProps.match.params.entityId) {
  //     if (this._isNew()) {
  //       // persist new entity to redux
  //       this.context.store.dispatch(manager.receiveEntity(nextProps.match.params.entityId, { }));
  //     } else {
  //       this.context.store.dispatch(manager.fetchEntity(nextProps.match.params.entityId));
  //     }
  //   }
  // }

  /**
   * Helper - returns `true`, when new entity is created
   */
  _isNew() {
    const { query } = this.props.location;
    return (query) ? query.new : null;
  }

  render() {
    const { entity, showLoading} = this.props;
    // Not allows to create VsRequestDetail without entity. Otherwise tables with next and previous requests
    // will be not loaded (because the componentWillReceiveProps is not used).
    if (!entity) {
      return null;
    }

    return (
      <Basic.Row>
        <div className={this._isNew() ? 'col-lg-offset-1 col-lg-10' : 'col-lg-12'}>
          {
            <VsRequestDetail uiKey="vs-request-detail" entity={entity} showLoading={showLoading} />
          }
        </div>
      </Basic.Row>
    );
  }
}

VsRequestContent.propTypes = {
  /**
   * Loaded entity
   */
  entity: PropTypes.object,
  /**
   * Entity is currently loaded from BE
   */
  showLoading: PropTypes.bool
};
VsRequestContent.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(VsRequestContent);
