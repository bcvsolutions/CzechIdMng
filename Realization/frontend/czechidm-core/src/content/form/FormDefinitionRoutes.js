import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { FormDefinitionManager } from '../../redux';
import FormDefinitionDetail from './FormDefinitionDetail';

const manager = new FormDefinitionManager();

/**
 * Form content -> detail with vertical menu
 *
 * @author Ondřej Kopr
 * @author Radek Tomiška
 */
class FormDefinitionRoutes extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
  }

  getContentKey() {
    return 'content.formDefinitions';
  }

  componentDidMount() {
    const { entityId } = this.props.match.params;

    if (!this._getIsNew()) {
      this.getLogger().debug(`[FormContent] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId));
    }
  }

  /**
   * Method check if exist params new
   */
  _getIsNew() {
    const { query } = this.props.location;
    if (query) {
      return query.new ? true : false;
    }
    return false;
  }

  render() {
    const { entity } = this.props;
    return (
      <div>
        {
          this._getIsNew()
          ?
          <Helmet title={this.i18n('create.title')} />
          :
          <Helmet title={this.i18n('edit.title')} />
        }
        {
          (this._getIsNew() || !entity )
          ||
          <Basic.PageHeader>
            <span>{entity.name} <small>{this.i18n('edit')}</small></span>
          </Basic.PageHeader>
        }
        {
          this._getIsNew()
          ?
          <FormDefinitionDetail isNew match={ this.props.match } />
          :
          <Advanced.TabPanel position="left" parentId="forms" match={ this.props.match }>
            {this.getRoutes()}
          </Advanced.TabPanel>
        }

      </div>
    );
  }
}

FormDefinitionRoutes.propTypes = {
  entity: PropTypes.object,
  showLoading: PropTypes.bool
};
FormDefinitionRoutes.defaultProps = {
};

function select(state, component) {
  const { entityId } = component.match.params;
  //
  return {
    entity: manager.getEntity(state, entityId),
    showLoading: manager.isShowLoading(state, null, entityId)
  };
}

export default connect(select)(FormDefinitionRoutes);
