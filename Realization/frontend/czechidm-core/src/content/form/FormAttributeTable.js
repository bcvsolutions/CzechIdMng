import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import uuid from 'uuid';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { SecurityManager, FormAttributeManager } from '../../redux';
import SearchParameters from '../../domain/SearchParameters';
import PersistentTypeEnum from '../../enums/PersistentTypeEnum';

const attributeManager = new FormAttributeManager();

/**
* Table of forms attributes.
*
* @author Ondřej Kopr
* @author Radek Tomiška
*/
class FormAttributeTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: props.filterOpened,
      showLoading: true,
      lastOrder: null
    };
  }

  getContentKey() {
    return 'content.formAttributes';
  }

  getManager() {
    return attributeManager;
  }

  getUiKey() {
    return this.props.uiKey;
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

  showDetail(entity) {
    const { definitionId } = this.props;
    if (entity.id === undefined) {
      // load form attributes and add next order
      const searchParameters = new SearchParameters().setSize(1).setFilter('definitionId', definitionId).setSort('seq', 'desc');
      this.context.store.dispatch(this.getManager().fetchEntities(searchParameters, null, json => {
        const newId = uuid.v1();
        entity = {
          ...entity,
          seq: 0,
          formDefinition: definitionId,
          persistentType: PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.SHORTTEXT),
          unmodifiable: false
        };
        // seq is used => error is not resolved (~ order is set optionaly)
        if (json) {
          const data = json._embedded[this.getManager().getCollectionType()] || [];
          if (data.length > 0) {
            entity.seq = data[0].seq + 1;
            if (entity.seq > 32767) {
              // short max => at end => order will be decremented by dnd in table eventually
              entity.seq = 32767;
            }
          }
        }
        //
        this.context.store.dispatch(this.getManager().receiveEntity(newId, entity));
        this.context.history.push(`/form-definitions/attribute/${ newId }/detail?new=1&formDefinition=${ definitionId }`);
      }));
    } else {
      this.context.history.push(`/form-definitions/attribute/${ entity.id }/detail`);
    }
  }

  render() {
    const { uiKey, definitionId, className, showAddLoading } = this.props;
    const { filterOpened } = this.state;
    //
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>
        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          showRowSelection={ SecurityManager.hasAuthority('FORMATTRIBUTE_DELETE') }
          manager={ attributeManager }
          forceSearchParameters={ new SearchParameters().setFilter('definitionId', definitionId) }
          rowClass={ ({rowIndex, data}) => { return data[rowIndex].disabled ? 'disabled' : ''; } }
          className={ className }
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 6 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('filter.text.placeholder') }/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={this.cancelFilter.bind(this)}/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          actions={
            [
              { value: 'delete', niceLabel: this.i18n('action.delete.action'), action: this.onDelete.bind(this), disabled: false }
            ]
          }
          buttons={
            [
              <Basic.Button
                level="success"
                key="add_button"
                className="btn-xs"
                onClick={ this.showDetail.bind(this, {}) }
                rendered={ SecurityManager.hasAuthority('FORMATTRIBUTE_CREATE') }
                showLoading={ showAddLoading }
                showLoadingIcon
                icon="fa:plus">
                { this.i18n('button.add') }
              </Basic.Button>
            ]
          }
          filterOpened={ !filterOpened }
          draggable={ SecurityManager.hasAuthority('FORMATTRIBUTE_UPDATE') }>
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
            sort={ false }
            _searchParameters={ this.getSearchParameters() }/>
          <Advanced.Column property="code" header={ this.i18n('entity.FormAttribute.code.label') } sort/>
          <Advanced.Column property="name" header={ this.i18n('entity.FormAttribute.name.label') } sort/>
          <Advanced.Column property="persistentType" sort face="enum" enumClass={ PersistentTypeEnum }/>
          <Advanced.Column
            property="faceType"
            cell={
              ({ data, rowIndex, property }) => {
                const faceType = data[rowIndex][property] || data[rowIndex].persistentType;
                const formComponent = attributeManager.getFormComponent(data[rowIndex]);
                //
                if (!formComponent) {
                  return (
                    <Basic.Label
                      level="warning"
                      value={ faceType }
                      title={
                        this.i18n('component.advanced.EavForm.persistentType.unsupported.title', {
                          name: data[rowIndex].persistentType,
                          face: faceType
                        })
                      }/>
                  );
                }
                if (formComponent.persistentType === PersistentTypeEnum.findKeyBySymbol(PersistentTypeEnum.CODELIST)) {
                  return (
                    <span>{ faceType }</span>
                  );
                }
                return (
                  <span>{ formComponent.labelKey ? this.i18n(formComponent.labelKey) : faceType }</span>
                );
              }
            }/>
          <Advanced.Column
            property="defaultValue"
            face="text"
            header={ this.i18n('entity.RoleFormAttribute.defaultValue.label') }/>
          <Advanced.Column
            property="required"
            face="bool"
            header={ this.i18n('entity.FormAttribute.required') }/>
          <Advanced.Column
            property="unique"
            face="bool"
            header={ this.i18n('entity.FormAttribute.unique.short') }
            width={ 75 }/>
          <Advanced.Column
            property="min"
            face="text"
            header={ this.i18n('entity.FormAttribute.min.short') }/>
          <Advanced.Column
            property="max"
            face="text"
            header={ this.i18n('entity.FormAttribute.max.short') }/>
          <Advanced.Column
            property="regex"
            face="text"
            header={ this.i18n('entity.FormAttribute.regex.short') }/>
          <Advanced.Column property="unmodifiable" header={this.i18n('entity.FormAttribute.unmodifiable.label')} face="bool" sort rendered={ false }/>
          <Advanced.Column property="seq" header={ this.i18n('entity.FormAttribute.seq.label') } sort width="5%"/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

FormAttributeTable.propTypes = {
  filterOpened: PropTypes.bool,
  uiKey: PropTypes.string.isRequired,
  definitionId: PropTypes.string.isRequired
};

FormAttributeTable.defaultProps = {
  filterOpened: true,
};

function select(state, component) {
  return {
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    showAddLoading: attributeManager.isShowLoading(state),
  };
}

export default connect(select)(FormAttributeTable);
