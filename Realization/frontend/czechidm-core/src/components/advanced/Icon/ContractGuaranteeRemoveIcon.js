import React from 'react';
import { faUserTie, faMinusSquare } from '@fortawesome/free-solid-svg-icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
//
import AbstractIcon from './AbstractIcon';

/**
 * Remove assigned identity role.
 *
 * @author Ondrej Husnik
 * @since 10.8.0
 */
export default class ContractGuaranteeRemoveIcon extends AbstractIcon {

  renderIcon() {
    return (
      <span className={ this.getClassName('fa-layers fa-fw') }>
        <FontAwesomeIcon icon={ faUserTie } transform="left-2"/>
        <FontAwesomeIcon icon={ faMinusSquare } transform="up-3 right-7 shrink-6" className="icon-danger"/>
      </span>
    );
  }
}
