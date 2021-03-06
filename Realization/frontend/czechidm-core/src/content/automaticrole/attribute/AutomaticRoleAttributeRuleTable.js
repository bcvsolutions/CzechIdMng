import PropTypes from 'prop-types';
import React from 'react';
import uuid from 'uuid';
import { connect } from 'react-redux';
//
import Helmet from 'react-helmet';
import * as Utils from '../../../utils';
import * as Basic from '../../../components/basic';
import * as Advanced from '../../../components/advanced';
import { SecurityManager, AutomaticRoleAttributeManager } from '../../../redux';
import AutomaticRoleAttributeRuleTypeEnum from '../../../enums/AutomaticRoleAttributeRuleTypeEnum';
import AutomaticRoleAttributeRuleComparisonEnum from '../../../enums/AutomaticRoleAttributeRuleComparisonEnum';
import IdentityAttributeEnum from '../../../enums/IdentityAttributeEnum';
import ContractAttributeEnum from '../../../enums/ContractAttributeEnum';

const automaticRoleAttributeManager = new AutomaticRoleAttributeManager();

/**
 * Table with rules for automatic role by attribute
 *
 * @author Ondřej Kopr
 */
export class AutomaticRoleAttributeRuleTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    // default filter status
    // true - open
    // false - close
    this.state = {
      filterOpened: false
    };
  }

  getManager() {
    return this.props.manager;
  }

  getContentKey() {
    return 'content.automaticRoles.attribute.rule';
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

  /**
   * Recive new form for create new type else show detail for existing automatic role.
   */
  showDetail(entity, event) {
    const { attributeId } = this.props;
    if (event) {
      event.preventDefault();
    }
    if (entity.id === undefined) {
      const uuidId = uuid.v1();
      this.context.history.push(`/automatic-role/attributes/${attributeId}/rule/${uuidId}?new=1`);
    } else {
      this.context.history.push(`/automatic-role/attributes/${attributeId}/rule/${entity.id}`);
    }
  }

  /**
   * Return name of attribute for evaluating
   *
   * @param  {[String]} automaticRole
   * @return {[String]}
   */
  _getAttributeName(automaticRole) {
    if (automaticRole) {
      if (automaticRole.type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.IDENTITY)) {
        return IdentityAttributeEnum.getNiceLabel(IdentityAttributeEnum.findKeyBySymbol(IdentityAttributeEnum.getEnum(automaticRole.attributeName)));
      } else if (automaticRole.type === AutomaticRoleAttributeRuleTypeEnum.findKeyBySymbol(AutomaticRoleAttributeRuleTypeEnum.CONTRACT)) {
        return ContractAttributeEnum.getNiceLabel(ContractAttributeEnum.findKeyBySymbol(ContractAttributeEnum.getEnum(automaticRole.attributeName)));
      } else if (automaticRole._embedded && automaticRole._embedded.formAttribute) {
        return automaticRole._embedded.formAttribute.name;
      }
    }
  }

  afterDelete() {
    super.afterDelete();
    //
    this.context.store.dispatch(automaticRoleAttributeManager.fetchEntity(this.props.attributeId));
  }

  render() {
    const { uiKey, manager, rendered, attributeId, className } = this.props;
    const { filterOpened } = this.state;
    //
    if (!rendered) {
      return null;
    }
    //
    let forceSearchParameters = manager.getDefaultSearchParameters();
    if (attributeId) {
      forceSearchParameters = forceSearchParameters.setFilter('automaticRoleAttributeId', attributeId);
    }
    //
    return (
      <Basic.Div>
        <Helmet title={ this.i18n('content.automaticRoles.attribute.edit.title') } />
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ SecurityManager.hasAuthority('AUTOMATICROLERULE_DELETE') }
          noData={ this.i18n('content.automaticRoles.emptyRules') }
          filter={
            <Advanced.Filter onSubmit={this.useFilter.bind(this)}>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row>
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={this.i18n('filter.text')}/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          filterOpened={ filterOpened }
          _searchParameters={ this.getSearchParameters() }
          className={ className }>
          <Advanced.Column
            header=""
            className="detail-button"
            cell={
              ({ rowIndex, data }) => {
                return (
                  <Advanced.DetailButton
                    title={ this.i18n('button.detail') }
                    onClick={ this.showDetail.bind(this, data[rowIndex]) }/>
                );
              }
            }
            sort={false}/>
          <Advanced.Column
            property="type"
            face="enum"
            enumClass={ AutomaticRoleAttributeRuleTypeEnum }
            header={ this.i18n('entity.AutomaticRole.attribute.type.label') }/>
          <Advanced.Column
            property="attributeName"
            header={ this.i18n('entity.AutomaticRole.attribute.attributeName') }
            cell={
              ({ rowIndex, data }) => {
                return this._getAttributeName(data[rowIndex]);
              }
            }/>
          <Advanced.Column
            property="comparison"
            face="enum"
            enumClass={ AutomaticRoleAttributeRuleComparisonEnum }
            header={ this.i18n('entity.AutomaticRole.attribute.comparison') }/>
          <Advanced.Column
            property="value"
            header={ this.i18n('entity.AutomaticRole.attribute.value.label') }
            cell={
              ({ rowIndex, data }) => {
                const value = data[rowIndex].value;
                if (!value || value === undefined || value === 'null') {
                  return '';
                }
                return value;
              }
            }/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

AutomaticRoleAttributeRuleTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  rendered: PropTypes.bool.isRequired
};

AutomaticRoleAttributeRuleTable.defaultProps = {
  rendered: true
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey)
  };
}

export default connect(select)(AutomaticRoleAttributeRuleTable);
