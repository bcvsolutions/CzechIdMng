import React from 'react';
import PropTypes from 'prop-types';
import _ from 'lodash';
import { connect } from 'react-redux';
import Helmet from 'react-helmet';
import Joi from 'joi';
import * as Utils from '../../utils';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import { IdentityManager } from '../../redux';

const PASSWORD_PREVALIDATION = 'PASSWORD_PREVALIDATION';
const identityManager = new IdentityManager();

/**
 * Create identity form.
 *
 * @author Radek Tomiška
 */
class Create extends Basic.AbstractContent {

  constructor(props) {
    super(props);
    this.state = {
      detail: {
        entity: null
      },
      showLoading: false,
      generatePassword: true,
      generatePasswordShowLoading: false
    };
  }

  initData() {
    // TODO: load data from redux store
    const { generatePassword } = this.state;
    this.transformData({
      generatePassword
    }, () => {
      this.refs.username.focus();
      if (generatePassword) {
        this.generatePassword();
      }
      this._preValidate();
    });
  }

  componentDidMount() {
    this.selectNavigationItem('identities');
    // TODO: load data from redux store
    this.initData();
  }

  /*
   * Method shows password rules before applying change of password
   */
  _preValidate() {
    const requestData = {
      accounts: []
    };
    requestData.idm = true;

    identityManager.preValidate(requestData)
      .then(response => {
        if (response.status === 204) {
          return {};
        }
        return response.json();
      })
      .then(json => {
        let error;
        if (Utils.Response.getFirstError(json)) {
          error = Utils.Response.getFirstError(json);
        } else if (json._errors) {
          error = json._errors.pop();
        }

        if (error) {
          this.setState({
            validationError: error,
            validationDefinition: true
          });

          throw error;
        }
        return json;
      })
      .catch(error => {
        if (!error) {
          return;
        }
        if (error.statusEnum === PASSWORD_PREVALIDATION) {
          this.addErrorMessage({hidden: true}, error);
        } else {
          this.addError(error);
        }
      });
  }

  save(editContinue = 'CLOSE', event) {
    if (event) {
      event.preventDefault();
    }
    if (!this.refs.form.isFormValid()) {
      return;
    }
    const formData = this.refs.form.getData();

    // add data from child component to formData
    formData.password = this.refs.passwords.getValue();

    if (!this.refs.passwords.isValid()) {
      return;
    }
    //
    this.setState({
      showLoading: true
    }, () => {
      const result = _.merge({}, formData, {
        password: formData.password
      });
      delete result.passwordAgain;
      delete result.generatePassword;
      // TODO: transform to redux
      identityManager.getService().create(result)
        .then(json => {
          this.setState({
            showLoading: false,
            validationError: null,
            validationDefinition: false
          }, () => {
            this.addMessage({
              message: this.i18n('content.identity.create.message.success', { username: json.username})
            });
            switch (editContinue) {
              case 'EDIT': {
                this.context.history.replace(`/identity/${encodeURIComponent(json.username)}/profile`);
                break;
              }
              case 'NEW': {
                this.initData();
                this.context.history.replace(`/identity/new`);
                break;
              }
              default: {
                // this.context.history.push(`identities`); // TODO: has goBack?
                this.context.history.goBack();
              }
            }
          });
        }).catch(error => {
          this.setState({
            showLoading: false,
            validationError: error,
            validationDefinition: false
          }, () => {
            this.setState({
              password: formData.password,
              passwordAgain: formData.password
            }, () => {
              this.transformData(null, () => {
                this.addError(error);
              });
            });
          });
        });
    });
  }

  transformData(json, cb = null) {
    let result;
    if (json) {
      result = _.merge({}, json, { email: '' });
    }
    this.setState({
      detail: {
        entity: result
      }
    }, () => {
      if (cb) {
        cb();
      }
    });
  }

  setNewPassword(password) {
    const formData = _.merge(this.refs.form.getData(), {
      generatePassword: true
    });
    this.setState({
      detail: {
        entity: formData,
      },
      password,
      passwordAgain: password
    });
  }

  generatePassword(event = null) {
    const generate = event ? event.currentTarget.checked : true;
    this.setState({
      generatePassword: generate
    }, () => {
      if (!generate) {
        return;
      }
      // generate
      this.setState({
        generatePasswordShowLoading: true
      }, () => {
        identityManager
          .getService()
          .generatePassword()
          .then(response => response.json())
          .then(json => {
            if (!json.error) {
              this.setState({
                generatePasswordShowLoading: false,
                generatePassword: true
              }, () => {
                this.setNewPassword(json.content);
              });
            } else {
              this.setState({
                generatePasswordShowLoading: false,
                generatePassword: false
              }, () => {
                this.addError(json.error);
              });
            }
          })
          .catch(error => {
            this.setState({
              generatePasswordShowLoading: false,
              generatePassword: false
            }, () => {
              this.addError(error);
            });
          });
      });
    });
  }

  render() {
    const {
      detail,
      showLoading,
      generatePassword,
      generatePasswordShowLoading,
      passwordAgain,
      password,
      validationError,
      validationDefinition
    } = this.state;
    //
    return (
      <Basic.Row>
        <div className="col-lg-offset-1 col-lg-10">
          <Helmet title={ this.i18n('content.identity.create.title') } />
          <form onSubmit={ this.save.bind(this, 'CLOSE') }>
            <Basic.Panel showLoading={ showLoading }>
              <Basic.PanelHeader text={ this.i18n('content.identity.create.header') }/>
              <Basic.PanelBody>
                <Basic.Row>
                  <Basic.Col lg={ 7 }>
                    <Basic.AbstractForm
                      ref="form"
                      data={ detail.entity }
                      style={{ paddingTop: 0, paddingBottom: 0 }}>
                      <Basic.TextField ref="username" label={ this.i18n('content.identity.profile.username') } max={ 255 }/>
                      <Basic.TextField ref="firstName" label={ this.i18n('content.identity.profile.firstName') } max={ 255 }/>
                      <Basic.TextField ref="lastName" label={ this.i18n('content.identity.profile.lastName') } max={ 255 }/>
                      <Basic.TextField ref="externalCode" label={ this.i18n('content.identity.profile.externalCode') } max={ 255 }/>
                      <Basic.Row>
                        <Basic.Col lg={ 6 }>
                          <Basic.TextField ref="titleBefore" label={ this.i18n('entity.Identity.titleBefore') } max={ 100 } />
                        </Basic.Col>
                        <Basic.Col lg={ 6 }>
                          <Basic.TextField ref="titleAfter" label={ this.i18n('entity.Identity.titleAfter') } max={ 100 }/>
                        </Basic.Col>
                      </Basic.Row>

                      <Basic.Row>
                        <Basic.Col lg={ 6 }>
                          <Basic.TextField
                            ref="email"
                            label={ this.i18n('content.identity.profile.email.label') }
                            placeholder={ this.i18n('content.identity.profile.email.placeholder') }
                            validation={ Joi.string().email() }/>
                        </Basic.Col>
                        <Basic.Col lg={ 6 }>
                          <Basic.TextField
                            ref="phone"
                            label={ this.i18n('content.identity.profile.phone.label') }
                            placeholder={ this.i18n('content.identity.profile.phone.placeholder') }
                            max={ 30 } />
                        </Basic.Col>
                      </Basic.Row>
                      {/* TODO: support creating disabled identities? */}
                      <Basic.Checkbox ref="disabled" label={ this.i18n('entity.Identity.disabled') } rendered={ false }/>
                      <Basic.TextArea
                        ref="description"
                        label={ this.i18n('content.identity.profile.description.label') }
                        placeholder={ this.i18n('content.identity.profile.description.placeholder') }
                        rows={ 4 }
                        max={ 255 }/>
                    </Basic.AbstractForm>
                  </Basic.Col>
                  <Basic.Col lg={ 5 }>
                    <Basic.Div
                      className="abstract-form"
                      style={{ paddingTop: 0, paddingBottom: 0 }}>
                      <Basic.Checkbox
                        ref="generatePassword"
                        value={ generatePassword }
                        label={ this.i18n('content.identity.create.button.generate') }
                        onChange={ this.generatePassword.bind(this) }/>

                      <Advanced.PasswordField
                        className="form-control"
                        ref="passwords"
                        type={ generatePassword || generatePasswordShowLoading ? 'text' : 'password' }
                        required={ !generatePassword }
                        readOnly={ generatePassword }
                        newPassword={ password }
                        newPasswordAgain={ passwordAgain }/>
                    </Basic.Div>
                    <Advanced.ValidationMessage error={ validationError } validationDefinition={ validationDefinition }/>
                  </Basic.Col>
                </Basic.Row>
              </Basic.PanelBody>
              <Basic.PanelFooter>
                <Basic.Button
                  type="button"
                  level="link"
                  onClick={ this.context.history.goBack }
                  showLoading={ showLoading }>
                  { this.i18n('button.back') }
                </Basic.Button>

                <Basic.SplitButton
                  level="success"
                  title={ this.i18n('button.create') }
                  onClick={ this.save.bind(this, 'CLOSE') }
                  showLoading={ showLoading }
                  pullRight
                  dropup>
                  <Basic.MenuItem
                    eventKey="1"
                    onClick={ this.save.bind(this, 'EDIT') }>
                    { this.i18n('button.createContinue') }
                  </Basic.MenuItem>
                  <Basic.MenuItem
                    eventKey="2"
                    onClick={ this.save.bind(this, 'NEW') }>
                    { this.i18n('button.createNew') }
                  </Basic.MenuItem>
                </Basic.SplitButton>

              </Basic.PanelFooter>
            </Basic.Panel>
            {/* onEnter action - is needed because SplitButton is used instead standard submit button */}
            <input type="submit" className="hidden"/>
          </form>
        </div>
      </Basic.Row>
    );
  }
}

Create.propTypes = {
  userContext: PropTypes.object
};
Create.defaultProps = {
  userContext: null
};

function select(state) {
  return {
    userContext: state.security.userContext
  };
}

export default connect(select)(Create);
