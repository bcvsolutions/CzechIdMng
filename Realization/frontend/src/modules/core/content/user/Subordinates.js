

import React from 'react';
import Helmet from 'react-helmet';
import Immutable from 'immutable';
import uuid from 'uuid';
import { connect } from 'react-redux';
//
import * as Basic from '../../../../components/basic';
import * as Advanced from '../../../../components/advanced';
import { IdentitySubordinateManager } from '../../../../redux/data';
import ConnectedUserTable, { UserTable } from './UserTable';

class Subordinates extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      userID: null
    }
  }

  componentDidMount() {
    this._selectNavigationItem();
  }

  componentDidUpdate() {
    const { userID } = this.props.params;
    if (userID && (!this.state.userID || this.state.userID !== userID)) {
      this.setState(
        {
          userID: userID
        },
        () => { this.refs.table.getWrappedInstance().cancelFilter(); }
      );
    }
  }

  _selectNavigationItem() {
    this.selectNavigationItems(['user-subordinates','profile-subordinates']);
  }

  render() {
    const { userID } = this.props.params;
    const identitySubordinateManager = new IdentitySubordinateManager(userID);

    return (
      <div>
        <Helmet title={this.i18n('navigation.menu.subordinates.label')} />

        <Basic.ContentHeader style={{ marginBottom: 0 }}>
          {this.i18n('navigation.menu.subordinates.label')}
        </Basic.ContentHeader>

        <Basic.Panel className="no-border last">
          <ConnectedUserTable
            ref="table"
            uiKey='subordinate_table'
            identityManager={identitySubordinateManager}
            columns={UserTable.defaultProps.columns.filter(property => { return property !== 'idmManager'})}/>
        </Basic.Panel>
      </div>
    );
  }
}

Subordinates.propTypes = {
}
Subordinates.defaultProps = {
}

function select(state) {
  return {
  }
}

export default connect(select)(Subordinates);
