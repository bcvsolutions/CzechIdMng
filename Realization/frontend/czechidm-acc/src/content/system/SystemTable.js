import React from 'react';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';
import _ from 'lodash';
//
import { Basic, Advanced, Utils, Managers, Domain } from 'czechidm-core';
import uuid from 'uuid';
import ProvisioningOperationTypeEnum from '../../domain/ProvisioningOperationTypeEnum';
//

/**
* Table of target systems
*
* @author Radek Tomiška
*
*/
export class SystemTable extends Advanced.AbstractTableContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      filterOpened: this.props.filterOpened,
    };
  }

  getContentKey() {
    return 'acc:content.systems';
  }

  getManager() {
    const { manager } = this.props;
    //
    return manager;
  }

  componentDidMount() {
    super.componentDidMount();
    //
    if (this.refs.text) {
      this.refs.text.focus();
    }
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
    if (Utils.Entity.isNew(entity)) {
      const uuidId = uuid.v1();
      this.context.history.push(`/system/${uuidId}/new?new=1`);
    } else {
      this.context.history.push(`/system/${entity.id}/detail`);
    }
  }

  onDuplicate(bulkActionValue, selectedRows) {
    const { manager, uiKey } = this.props;
    const selectedEntities = manager.getEntitiesByIds(this.context.store.getState(), selectedRows);
    //
    this.refs['confirm-duplicate'].show(
      this.i18n(`action.${bulkActionValue}.message`, {
        count: selectedEntities.length,
        record: manager.getNiceLabel(selectedEntities[0]),
        records: manager.getNiceLabels(selectedEntities).join(', ')
      }),
      this.i18n(`action.${bulkActionValue}.header`, {
        count: selectedEntities.length,
        records: manager.getNiceLabels(selectedEntities).join(', ')
      })
    ).then(() => {
      this.context.store.dispatch(manager.duplicateEntities(selectedEntities, uiKey, (entity, error) => {
        if (entity && error) {
          this.addErrorMessage({ title: this.i18n(`action.delete.error`, { record: manager.getNiceLabel(entity) }) }, error);
        } else {
          this.refs.table.reload();
        }
      }));
    }, () => {
      // nothing
    });
  }

  getTableButtons(showAddButton) {
    return (
      [
        <Basic.Button
          level="success"
          key="add_button"
          className="btn-xs"
          onClick={ this.showDetail.bind(this, { }) }
          rendered={ Managers.SecurityManager.hasAuthority('SYSTEM_CREATE') && showAddButton }
          icon="fa:plus">
          { this.i18n('button.add') }
        </Basic.Button>
      ]);
  }

  /**
   * Return div with all labels for system with information about blocked operations.
   * There can't be used EnumLabel because every enumlable is on new line
   */
  _getBlockedOperations(system) {
    if (system && system.blockedOperation) {
      // every blocked operation has same level in this table
      const level = 'error';
      //
      const createKey = ProvisioningOperationTypeEnum.findKeyBySymbol(ProvisioningOperationTypeEnum.CREATE);
      const updateKey = ProvisioningOperationTypeEnum.findKeyBySymbol(ProvisioningOperationTypeEnum.UPDATE);
      const deleteKey = ProvisioningOperationTypeEnum.findKeyBySymbol(ProvisioningOperationTypeEnum.DELETE);
      return (
        <Basic.Div>
          {
            !system.blockedOperation.createOperation
            ||
            <span>
              {' '}
              <Basic.Label
                level={level}
                value={ProvisioningOperationTypeEnum.getNiceLabel(createKey)}/>
            </span>
          }
          {
            !system.blockedOperation.updateOperation
            ||
            <span>
              {' '}
              <Basic.Label
                level={level}
                value={ProvisioningOperationTypeEnum.getNiceLabel(updateKey)}/>
            </span>
          }
          {
            !system.blockedOperation.deleteOperation
            ||
            <span>
              {' '}
              <Basic.Label
                level={level}
                value={ProvisioningOperationTypeEnum.getNiceLabel(deleteKey)}/>
            </span>
          }
        </Basic.Div>
      );
    }
  }

  render() {
    const { uiKey, manager, columns, forceSearchParameters, showAddButton, showRowSelection } = this.props;
    const { filterOpened } = this.state;
    const showFilterVirtual = !forceSearchParameters.filters.get('virtual');

    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-duplicate" level="danger"/>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Advanced.Table
          ref="table"
          uiKey={ uiKey }
          manager={ manager }
          filterOpened={ filterOpened }
          forceSearchParameters={ forceSearchParameters }
          showRowSelection={ showRowSelection }
          filter={
            <Advanced.Filter onSubmit={ this.useFilter.bind(this) }>
              <Basic.AbstractForm ref="filterForm">
                <Basic.Row className="last">
                  <Basic.Col lg={ 4 }>
                    <Advanced.Filter.TextField
                      ref="text"
                      placeholder={ this.i18n('acc:entity.System.name') }
                      help={ Advanced.Filter.getTextHelp() }/>
                  </Basic.Col>
                  <Basic.Col lg={ 2 }>
                    <Advanced.Filter.BooleanSelectBox
                      ref="virtual"
                      placeholder={ this.i18n('acc:entity.System.systemType.label') }
                      rendered={showFilterVirtual}
                      options={ [
                        { value: 'true', niceLabel: this.i18n('acc:entity.System.systemType.virtual') },
                        { value: 'false', niceLabel: this.i18n('acc:entity.System.systemType.notVirtual') }
                      ]}/>
                  </Basic.Col>
                  <Basic.Col lg={ 6 } className="text-right">
                    <Advanced.Filter.FilterButtons cancelFilter={ this.cancelFilter.bind(this) }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.AbstractForm>
            </Advanced.Filter>
          }
          buttons={ this.getTableButtons(showAddButton) }
          _searchParameters={ this.getSearchParameters() }>

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
          <Advanced.ColumnLink to="/system/:id/detail" property="name" width="15%" sort face="text" rendered={ _.includes(columns, 'name') }/>
          <Advanced.Column property="description" sort face="text" rendered={ _.includes(columns, 'description') }/>
          <Advanced.Column property="queue" sort face="bool" width={ 75 } rendered={ _.includes(columns, 'queue') }/>
          <Advanced.Column
            property="state"
            header={ this.i18n('acc:entity.System.state.label')}
            face="bool"
            width={ 75 }
            rendered={ _.includes(columns, 'state') }
            cell={
              ({ rowIndex, data }) => {
                const entity = data[rowIndex];
                if (entity.disabledProvisioning) {
                  if (entity.disabled) {
                    return this.i18n('acc:entity.System.disabledProvisioning.label');
                  }
                  return this.i18n('acc:entity.System.readonlyDisabledProvisioning.label');
                }
                if (entity.disabled) {
                  return this.i18n('acc:entity.System.disabled.label');
                }
                if (entity.readonly) {
                  return this.i18n('acc:entity.System.readonly.label');
                }
                return null;
              }
            }/>
          <Advanced.Column
            property="blockedOperation"
            width="12%"
            cell={({ rowIndex, data }) => {
              return (
                this._getBlockedOperations(data[rowIndex])
              );
            }}
            rendered={ _.includes(columns, 'blockedOperation') }/>
        </Advanced.Table>
      </Basic.Div>
    );
  }
}

SystemTable.propTypes = {
  uiKey: PropTypes.string.isRequired,
  manager: PropTypes.object.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string),
  filterOpened: PropTypes.bool,
  forceSearchParameters: PropTypes.object,
  showAddButton: PropTypes.bool,
  showRowSelection: PropTypes.bool
};

SystemTable.defaultProps = {
  columns: ['name', 'description', 'state', 'virtual', 'queue', 'blockedOperation'],
  filterOpened: false,
  _showLoading: false,
  forceSearchParameters: new Domain.SearchParameters(),
  showAddButton: true,
  showRowSelection: true
};

function select(state, component) {
  return {
    i18nReady: state.config.get('i18nReady'),
    _searchParameters: Utils.Ui.getSearchParameters(state, component.uiKey),
    _showLoading: component.manager.isShowLoading(state, `${component.uiKey}-detail`)
  };
}

export default connect(select, null, null, { forwardRef: true })(SystemTable);
