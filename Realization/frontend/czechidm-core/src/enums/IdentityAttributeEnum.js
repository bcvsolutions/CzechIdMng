import AbstractEnum from './AbstractEnum';

/**
 * Keys of Identity fields
 */
export default class IdentityAttributeEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.IdentityAttributeEnum.${key}`);
  }

  static getHelpBlockLabel(key) {
    return super.getNiceLabel(`core:enums.IdentityAttributeEnum.helpBlock.${key}`);
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
      case this.USERNAME: {
        return 'username';
      }
      case this.EXTERNAL_CODE: {
        return 'externalCode';
      }
      case this.DISABLED: {
        return 'disabled';
      }
      case this.FIRSTNAME: {
        return 'firstName';
      }
      case this.LASTNAME: {
        return 'lastName';
      }
      case this.EMAIL: {
        return 'email';
      }
      case this.PHONE: {
        return 'phone';
      }
      case this.TITLE_BEFORE: {
        return 'titleBefore';
      }
      case this.TITLE_AFTER: {
        return 'titleAfter';
      }
      case this.DESCRIPTION: {
        return 'description';
      }
      case this.ASSIGNED_ROLES: {
        return 'assignedRoles';
      }
      case this.ASSIGNED_ROLES_FOR_SYSTEM: {
        return 'assignedRolesForSystem';
      }
      case this.FORM_PROJECTION: {
        return 'formProjection';
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
      case 'username': {
        return this.USERNAME;
      }
      case 'externalCode': {
        return this.EXTERNAL_CODE;
      }
      case 'disabled': {
        return this.DISABLED;
      }
      case 'firstName': {
        return this.FIRSTNAME;
      }
      case 'lastName': {
        return this.LASTNAME;
      }
      case 'email': {
        return this.EMAIL;
      }
      case 'phone': {
        return this.PHONE;
      }
      case 'titleBefore': {
        return this.TITLE_BEFORE;
      }
      case 'titleAfter': {
        return this.TITLE_AFTER;
      }
      case 'description': {
        return this.DESCRIPTION;
      }
      case 'assignedRoles': {
        return this.ASSIGNED_ROLES;
      }
      case 'assignedRolesForSystem': {
        return this.ASSIGNED_ROLES_FOR_SYSTEM;
      }
      case 'formProjection': {
        return this.FORM_PROJECTION;
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

IdentityAttributeEnum.USERNAME = Symbol('USERNAME');
IdentityAttributeEnum.EXTERNAL_CODE = Symbol('EXTERNAL_CODE');
IdentityAttributeEnum.DISABLED = Symbol('DISABLED');
IdentityAttributeEnum.FIRSTNAME = Symbol('FIRSTNAME');
IdentityAttributeEnum.LASTNAME = Symbol('LASTNAME');
IdentityAttributeEnum.EMAIL = Symbol('EMAIL');
IdentityAttributeEnum.PHONE = Symbol('PHONE');
IdentityAttributeEnum.TITLE_BEFORE = Symbol('TITLE_BEFORE');
IdentityAttributeEnum.TITLE_AFTER = Symbol('TITLE_AFTER');
IdentityAttributeEnum.DESCRIPTION = Symbol('DESCRIPTION');
IdentityAttributeEnum.ASSIGNED_ROLES = Symbol('ASSIGNED_ROLES');
IdentityAttributeEnum.ASSIGNED_ROLES_FOR_SYSTEM = Symbol('ASSIGNED_ROLES_FOR_SYSTEM');
IdentityAttributeEnum.FORM_PROJECTION = Symbol('FORM_PROJECTION');
