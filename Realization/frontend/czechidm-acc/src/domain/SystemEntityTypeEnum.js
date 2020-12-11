import { Enums } from 'czechidm-core';
import TreeAttributeEnum from './TreeAttributeEnum';
import RoleAttributeEnum from './RoleAttributeEnum';
import IdentityRoleAttributeEnum from './IdentityRoleAttributeEnum';
import RoleCatalogueAttributeEnum from './RoleCatalogueAttributeEnum';

/**
 * System entity type
 */
export default class SystemEntityTypeEnum extends Enums.AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`acc:enums.SystemEntityTypeEnum.${key}`);
  }

  static findKeyBySymbol(sym) {
    return super.findKeyBySymbol(this, sym);
  }

  static findSymbolByKey(key) {
    return super.findSymbolByKey(this, key);
  }

  static getLevel(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.IDENTITY: {
        return 'success';
      }
      case this.TREE: {
        return 'primary';
      }
      case this.ROLE: {
        return 'primary';
      }
      case this.ROLE_CATALOGUE: {
        return 'primary';
      }
      case this.CONTRACT: {
        return 'success';
      }
      case this.CONTRACT_SLICE: {
        return 'success';
      }
      case this.IDENTITY_ROLE: {
        return 'success';
      }
      default: {
        return 'default';
      }
    }
  }

  static getIcon(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.IDENTITY: {
        return 'component:identity';
      }
      case this.TREE: {
        return 'fa:folder-open';
      }
      case this.ROLE: {
        return 'component:role';
      }
      case this.ROLE_CATALOGUE: {
        return 'component:role-catalogue';
      }
      case this.CONTRACT: {
        return 'fa:building';
      }
      case this.CONTRACT_SLICE: {
        return 'fa:hourglass-half';
      }
      case this.IDENTITY_ROLE: {
        return 'component:identity-role';
      }
      default: {
        return null;
      }
    }
  }

  static getEntityEnum(key) {
    if (!key) {
      return null;
    }

    const sym = super.findSymbolByKey(this, key);

    switch (sym) {
      case this.IDENTITY: {
        return Enums.IdentityAttributeEnum;
      }
      case this.TREE: {
        return TreeAttributeEnum;
      }
      case this.ROLE: {
        return RoleAttributeEnum;
      }
      case this.ROLE_CATALOGUE: {
        return RoleCatalogueAttributeEnum;
      }
      case this.CONTRACT: {
        return Enums.ContractAttributeEnum;
      }
      case this.CONTRACT_SLICE: {
        return Enums.ContractSliceAttributeEnum;
      }
      case this.IDENTITY_ROLE: {
        return IdentityRoleAttributeEnum;
      }
      default: {
        return null;
      }
    }
  }
}

SystemEntityTypeEnum.IDENTITY = Symbol('IDENTITY');
SystemEntityTypeEnum.TREE = Symbol('TREE');
SystemEntityTypeEnum.ROLE = Symbol('ROLE');
SystemEntityTypeEnum.ROLE_CATALOGUE = Symbol('ROLE_CATALOGUE');
SystemEntityTypeEnum.CONTRACT = Symbol('CONTRACT');
SystemEntityTypeEnum.CONTRACT_SLICE = Symbol('CONTRACT_SLICE');
SystemEntityTypeEnum.IDENTITY_ROLE = Symbol('IDENTITY_ROLE');
