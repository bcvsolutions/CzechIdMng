import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
import Helmet from 'react-helmet';
//
import * as Basic from '../../components/basic';
import { ScriptManager } from '../../redux';
//
import ScriptTable from './ScriptTable';

// const uiKey = 'script-authorities';
const scriptManager = new ScriptManager();

/**
 * Script usage in other scripts
 *
 * @author Patrik Stloukal
 */
class ScriptReferences extends Basic.AbstractContent {

  getContentKey() {
    return 'content.scripts.references';
  }

  getNavigationKey() {
    return 'script-references';
  }

  useFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.useFilterForm(this.refs.filterForm);
  }

  cancelFilter(event) {
    if (event) {
      event.preventDefault();
    }
    this.refs.table.cancelFilter(this.refs.filterForm);
  }

  showDetail(entity, event) {
    if (event) {
      event.preventDefault();
    }
    // when create new script is generate non existing id
    // and set parameter new to 1 (true)
    // this is necessary for ScriptDetail
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.history.push(`/scripts/${uuidId}?new=1`);
    } else {
      this.context.history.push(`/scripts/${entity.id}/detail`);
    }
  }

  render() {
    const { uiKey, _entity } = this.props;
    if (this.props._entity == null) {
      return null;
    }
    //
    return (
      <div>
        <Helmet title={ this.i18n('title') } />
        <Basic.ContentHeader icon="component:script" text={ this.i18n('header', { escape: false }) } style={{ marginBottom: 0 }}/>

        <ScriptTable
          uiKey={uiKey}
          scriptManager={scriptManager}
          forceSearchParameters={scriptManager.getDefaultSearchParameters().setFilter('usedIn', _entity.code)}
          disableAdd
          filterOpened={ false }
          className="no-margin"/>
      </div>
    );
  }
}

ScriptReferences.propTypes = {
  _entity: PropTypes.object,
  _permissions: PropTypes.arrayOf(PropTypes.string),
  uiKey: PropTypes.string.isRequired,
  script: PropTypes.object.isRequired,
  rendered: PropTypes.bool.isRequired
};
ScriptReferences.defaultProps = {
  _entity: null,
  _permissions: null,
  rendered: true
};

function select(state, component) {
  return {
    _entity: scriptManager.getEntity(state, component.match.params.entityId),
    _permissions: scriptManager.getPermissions(state, null, component.match.params.entityId),
  };
}

export default connect(select)(ScriptReferences);
