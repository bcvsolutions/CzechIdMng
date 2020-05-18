import AbstractEnum from './AbstractEnum';

/**
 * Keys of contract slice fields
 *
 * @author Vít Švanda
 */
export default class ContractSliceAttributeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ContractSliceAttributeEnum.${key}`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`core:enums.ContractSliceAttributeEnum.helpBlock.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getField(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.IDENTITY: {
        return 'identity';
      }
      case this.VALID_FROM: {
        return 'validFrom';
      }
      case this.CONTRACT_VALID_FROM: {
        return 'contractValidFrom';
      }
      case this.CONTRACT_VALID_TILL: {
        return 'contractValidTill';
      }
      case this.WORK_POSITION: {
        return 'workPosition';
      }
      case this.POSITION: {
        return 'position';
      }
      case this.EXTERNE: {
        return 'externe';
      }
      case this.MAIN: {
        return 'main';
      }
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.GUARANTEES: {
        return 'guarantees';
      }
      case this.STATE: {
        return 'state';
      }
      case this.CONTRACT_CODE: {
        return 'contractCode';
      }
      default: {
        return null;
      }
    }
  }

  static getEnum(field) {
    if (!field) {
      return null;
    }

    switch (field) {
      case 'identity': {
        return this.IDENTITY;
      }
      case 'validFrom': {
        return this.VALID_FROM;
      }
      case 'contractValidFrom': {
        return this.CONTRACT_VALID_FROM;
      }
      case 'contratcValidTill': {
        return this.CONTRACT_VALID_TILL;
      }
      case 'workPosition': {
        return this.WORK_POSITION;
      }
      case 'position': {
        return this.POSITION;
      }
      case 'externe': {
        return this.EXTERNE;
      }
      case 'main': {
        return this.MAIN;
      }
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      case 'guarantees': {
        return this.GUARANTEES;
      }
      case 'state': {
        return this.STATE;
      }
      case 'contractCode': {
        return this.CONTRACT_CODE;
      }
      default: {
        return null;
      }
    }
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      default: {
        return 'default';
      }
    }
  }
}

ContractSliceAttributeEnum.CONTRACT_CODE = Symbol('CONTRACT_CODE');
ContractSliceAttributeEnum.IDENTITY = Symbol('IDENTITY');
ContractSliceAttributeEnum.VALID_FROM = Symbol('VALID_FROM');
ContractSliceAttributeEnum.CONTRACT_VALID_FROM = Symbol('CONTRACT_VALID_FROM');
ContractSliceAttributeEnum.CONTRACT_VALID_TILL = Symbol('CONTRACT_VALID_TILL');
ContractSliceAttributeEnum.MAIN = Symbol('MAIN');
ContractSliceAttributeEnum.STATE = Symbol('STATE');
ContractSliceAttributeEnum.POSITION = Symbol('POSITION');
ContractSliceAttributeEnum.WORK_POSITION = Symbol('WORK_POSITION');
ContractSliceAttributeEnum.EXTERNE = Symbol('EXTERNE');
ContractSliceAttributeEnum.GUARANTEES = Symbol('GUARANTEES');
ContractSliceAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
