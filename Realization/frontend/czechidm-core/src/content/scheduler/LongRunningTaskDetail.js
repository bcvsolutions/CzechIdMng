import PropTypes from 'prop-types';
import React from 'react';
import moment from 'moment';
//
import * as Basic from '../../components/basic';
import * as Advanced from '../../components/advanced';
import * as Utils from '../../utils';
import { LocalizationService } from '../../services';

/**
 * Table with long running task items and detail of LRT
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */
export default class LongRunningTaskDetail extends Basic.AbstractContent {

  getContentKey() {
    return 'content.scheduler.all-tasks';
  }

  render() {
    const { entity, supportedTasks } = this.props;
    //
    return (
      <Basic.Div>
        <Basic.Confirm ref="confirm-delete" level="danger"/>

        <Basic.Panel className="no-border last">
          <Basic.PanelBody style={{ padding: 0 }}>

            <Basic.AbstractForm data={ entity } readOnly style={{ paddingTop: 0 }}>
              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.ContentHeader text={ this.i18n('tabs.basic') } />

                  <Basic.LabelWrapper label={ this.i18n('entity.created') }>
                    <Advanced.DateValue value={ entity.created } showTime/>
                  </Basic.LabelWrapper>
                  {
                    !entity.taskStarted
                    ||
                    <Basic.LabelWrapper label={ this.i18n('entity.LongRunningTask.started') }>
                      <Advanced.DateValue value={ entity.taskStarted } showTime/>
                    </Basic.LabelWrapper>
                  }
                  <Basic.LabelWrapper label={ this.i18n('entity.LongRunningTask.taskType') }>
                    <span title={ entity.taskType }>
                      { Utils.Ui.getSimpleJavaType(entity.taskType) }
                    </span>
                  </Basic.LabelWrapper>
                  <Basic.LabelWrapper label={ this.i18n('entity.LongRunningTask.instanceId.label') }>
                    { entity.instanceId }
                    <span className="help-block">{ this.i18n('entity.LongRunningTask.instanceId.help') }</span>
                  </Basic.LabelWrapper>
                  <Basic.Checkbox
                    label={ this.i18n('entity.LongRunningTask.dryRun.label') }
                    helpBlock={ this.i18n('entity.LongRunningTask.dryRun.help') }
                    readOnly
                    value={ entity.dryRun }/>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  <Basic.ContentHeader text={ this.i18n('entity.LongRunningTask.taskProperties.label') } />
                  <Advanced.LongRunningTaskProperties entity={ entity } supportedTasks={ supportedTasks }/>
                </Basic.Col>
              </Basic.Row>

              <Basic.TextArea
                label={ this.i18n('entity.LongRunningTask.taskDescription') }
                disabled
                value={ entity.taskDescription }/>

              <Basic.Row>
                <Basic.Col lg={ 6 }>
                  <Basic.LabelWrapper label={ this.i18n('entity.LongRunningTask.counter') }>
                    {
                      !entity.taskStarted
                      ?
                      this.i18n('entity.LongRunningTask.notstarted')
                      :
                      <Advanced.LongRunningTask entity={ entity } face="count"/>
                    }
                  </Basic.LabelWrapper>
                </Basic.Col>
                <Basic.Col lg={ 6 }>
                  {
                    !entity.taskStarted
                    ||
                    <Basic.LabelWrapper label={ this.i18n('entity.LongRunningTask.duration') }>
                      <Basic.Tooltip
                        ref="popover"
                        placement="bottom"
                        value={
                          moment
                            .utc(moment.duration(moment(entity.modified).diff(moment(entity.taskStarted))).asMilliseconds())
                            .format(this.i18n('format.times'))
                        }>
                        <span>
                          {
                            moment
                              .duration(moment(entity.taskStarted).diff(moment(entity.modified)))
                              .locale(LocalizationService.getCurrentLanguage())
                              .humanize()
                          }
                        </span>
                      </Basic.Tooltip>
                    </Basic.LabelWrapper>
                  }
                </Basic.Col>
              </Basic.Row>

              <Advanced.OperationResult value={ entity.result } face="full" downloadLinkPrefix={ `long-running-tasks/${entity.id}/download` } />

            </Basic.AbstractForm>
          </Basic.PanelBody>

          <Basic.PanelFooter>
            <Basic.Button type="button" level="link" onClick={ this.context.history.goBack }>
              { this.i18n('button.back') }
            </Basic.Button>
          </Basic.PanelFooter>
        </Basic.Panel>
      </Basic.Div>
    );
  }
}

LongRunningTaskDetail.propTypes = {
  /**
   * LRT
   *
   * @type {IdmLongRunningTaskDto}
   */
  entity: PropTypes.object,
  /**
   * Supported schedulable long running task.
   */
  supportedTasks: PropTypes.arrayOf(PropTypes.object)
};
