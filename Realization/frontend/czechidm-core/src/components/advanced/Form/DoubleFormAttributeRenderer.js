import Joi from 'joi';
//
import TextFormAttributeRenderer from './TextFormAttributeRenderer';

/**
 * Double form value component
 * - supports multiple and confidential attributes
 * - TODO: validation for multiple attrs
 *
 * @author Radek Tomiška
 */
export default class DoubleFormAttributeRenderer extends TextFormAttributeRenderer {

  /**
   * Returns joi validator by persistent type
   *
   * @param  {FormAttribute} attribute
   * @return {Joi}
   */
  getInputValidation() {
    const { attribute } = this.props;
    const min = attribute.min || -(10 ** 33);
    const max = attribute.max || (10 ** 33);
    //
    let validation = Joi.number().min(min).max(max);
    if (!this.isRequired()) {
      validation = validation.concat(Joi.number().allow(null));
    }
    return validation;
  }

  fillFormValue(formValue, rawValue) {
    formValue.doubleValue = rawValue;
    // common value can be used without persistent type knowlege (e.g. conversion to properties object)
    formValue.value = formValue.doubleValue;
    //
    // TODO: validations for numbers
    return formValue;
  }

  /**
   * Returns value to ipnut from given (persisted) form value
   *
   * @param  {FormValue} formValue
   * @return {object} value by persistent type
   */
  getInputValue(formValue) {
    return formValue.doubleValue ? formValue.doubleValue : formValue.value;
  }
}
