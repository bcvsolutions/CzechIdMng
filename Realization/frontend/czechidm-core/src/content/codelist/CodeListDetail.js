import PropTypes from 'prop-types';
import React from 'react';
import { connect } from 'react-redux';
import classnames from 'classnames';
//
import * as Utils from '../../utils';
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { CodeListManager } from '../../redux';

const manager = new CodeListManager();

/**
* CodeList detail.
*
* @author Radek Tomiška
* @since 9.4.0
*/
class CodeListDetail extends Basic.AbstractContent {

  constructor(props, context) {
    super(props, context);
    this.state = {
      _showLoading: true
    };
  }

  getContentKey() {
    return 'content.code-lists';
  }

  componentDidMount() {
    super.componentDidMount();
    //
    const { entityId } = this.props.match.params;
    const { isNew } = this.props;
    //
    if (isNew) {
      this.context.store.dispatch(manager.receiveEntity(entityId, { unmodifiable: false }, null, () => {
        this.refs.codeable.focus();
      }));
    } else {
      this.getLogger().debug(`[CodeListDetail] loading entity detail [id:${entityId}]`);
      this.context.store.dispatch(manager.fetchEntity(entityId, null, () => {
        this.refs.codeable.focus();
      }));
    }
  }

  getNavigationKey() {
    return 'code-list-detail';
  }

  save(event) {
    const { uiKey } = this.props;

    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }

    this.setState({
      _showLoading: true
    }, () => {
      this.refs.form.processStarted();
      const entity = this.refs.form.getData();
      entity.code = entity.codeable.code;
      entity.name = entity.codeable.name;
      //
      if (entity.id === undefined) {
        this.context.store.dispatch(manager.createEntity(entity, `${ uiKey }-detail`, (createdEntity, error) => {
          this._afterSave(createdEntity, error);
        }));
      } else {
        this.context.store.dispatch(manager.patchEntity(entity, `${ uiKey }-detail`, this._afterSave.bind(this)));
      }
    });
  }

  /**
  * Just set showloading to false and set processEnded to form.
  * Call after save/create
  */
  _afterSave(entity, error) {
    const { isNew } = this.props;
    if (error) {
      this.setState({
        _showLoading: false
      }, () => {
        this.refs.form.processEnded();
        this.addError(error);
      });
      return;
    }
    this.setState({
      _showLoading: false
    }, () => {
      this.addMessage({ message: this.i18n('save.success', { record: manager.getNiceLabel(entity) }) });
      if (isNew) {
        this.context.history.replace(`/code-lists/${ entity.id }/items`);
      }
    });
  }

  render() {
    const { uiKey, entity, showLoading, _permissions } = this.props;
    //
    return (
      <form onSubmit={ this.save.bind(this) }>
        <Basic.Panel
          className={
            classnames({
              last: !Utils.Entity.isNew(entity),
              'no-border': !Utils.Entity.isNew(entity)
            })
          }>
          <Basic.PanelHeader text={ Utils.Entity.isNew(entity) ? this.i18n('create.header') : this.i18n('content.formDefinitions.detail.title') } />
          <Basic.PanelBody style={ Utils.Entity.isNew(entity) ? { paddingTop: 0, paddingBottom: 0 } : { padding: 0 } }>
            <Basic.AbstractForm
              ref="form"
              uiKey={ uiKey }
              data={ entity }
              readOnly={ !manager.canSave(entity, _permissions) }>

              <Advanced.CodeableField
                ref="codeable"
                codeLabel={ this.i18n('entity.CodeList.code.label') }
                nameLabel={ this.i18n('entity.CodeList.name.label') }/>

              <Basic.TextArea
                ref="description"
                label={ this.i18n('entity.CodeList.description.label') }
                max={ 2000 }/>
            </Basic.AbstractForm>
          </Basic.PanelBody>
          <Basic.PanelFooter showLoading={ showLoading } >
            <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
              { this.i18n('button.back') }
            </Basic.Button>
            <Basic.Button
              type="submit"
              level="success"
              showLoadingIcon
              showLoadingText={ this.i18n('button.saving') }
              rendered={ manager.canSave(entity, _permissions) }>
              { this.i18n('button.save') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </form>
    );
  }
}

CodeListDetail.propTypes = {
  uiKey: PropTypes.string,
  definitionManager: PropTypes.object,
  isNew: PropTypes.bool,
  _permissions: PropTypes.arrayOf(PropTypes.string)
};
CodeListDetail.defaultProps = {
  isNew: false,
  _permissions: null
};

function select(state, component) {
  const { entityId } = component.match.params;
  const entity = manager.getEntity(state, entityId);
  if (entity) {
    entity.codeable = {
      code: entity.code,
      name: entity.name
    };
  }
  //
  return {
    entity,
    showLoading: manager.isShowLoading(state, null, entityId),
    _permissions: manager.getPermissions(state, null, entityId)
  };
}

export default connect(select)(CodeListDetail);
