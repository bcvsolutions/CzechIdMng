import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
//
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { ContractSliceManager } from '../../../redux';

const manager = new ContractSliceManager();

/**
 * Extended identity contract's attributes
 *
 * @author Radek Tomiška
 */
class ContractSliceEav extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  componentDidMount() {
    this.selectSidebarItem('contract-slice-eav');
  }

  getContentKey() {
    return 'content.identity-contract.eav';
  }

  render() {
    const { entityId} = this.props.match.params;
    const { _entity, _permissions } = this.props;
    //
    return (
      <Advanced.EavContent
        formableManager={manager}
        entityId={entityId}
        contentKey={this.getContentKey()}
        showSaveButton={ manager.canSave(_entity, _permissions) }/>
    );
  }
}

ContractSliceEav.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
ContractSliceEav.defaultProps = {
  _entity: null,
  _permissions: null
};

function select(state, component) {
  return {
    _entity: manager.getEntity(state, component.match.params.entityId),
    _permissions: manager.getPermissions(state, null, component.match.params.entityId)
  };
}

export default connect(select)(ContractSliceEav);
