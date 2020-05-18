
import AbstractEnum from './AbstractEnum';

/**
 * Role request state enum
 */
export default class RoleRequestStateEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.RoleRequestStateEnum.${key}`);
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
      case this.CONCEPT: {
        return 'info';
      }
      case this.EXECUTED: {
        return 'success';
      }
      case this.CANCELED: {
        return 'warning';
      }
      case this.APPROVED: {
        return 'success';
      }
      case this.IN_PROGRESS: {
        return 'warning';
      }
      case this.EXCEPTION: {
        return 'danger';
      }
      case this.DISAPPROVED: {
        return 'danger';
      }
      case this.DUPLICATED: {
        return 'danger';
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
      case this.CONCEPT: {
        return 'fa:circle-o';
      }
      case this.IN_PROGRESS: {
        return 'fa:circle-o';
      }
      case this.APPROVED: {
        return 'fa:check';
      }
      case this.EXECUTED: {
        return 'fa:check';
      }
      case this.DISAPPROVED: {
        return 'fa:circle-thin';
      }
      case this.EXCEPTION: {
        return 'fa:warning';
      }
      case this.CANCELED: {
        return 'fa:ban';
      }
      case this.DUPLICATED: {
        return 'fa:ban';
      }
      default: {
        return 'default';
      }
    }
  }
}

RoleRequestStateEnum.CONCEPT = Symbol('CONCEPT');
RoleRequestStateEnum.EXECUTED = Symbol('EXECUTED');
RoleRequestStateEnum.CANCELED = Symbol('CANCELED');
RoleRequestStateEnum.APPROVED = Symbol('APPROVED');
RoleRequestStateEnum.IN_PROGRESS = Symbol('IN_PROGRESS');
RoleRequestStateEnum.EXCEPTION = Symbol('EXCEPTION');
RoleRequestStateEnum.DUPLICATED = Symbol('DUPLICATED');
RoleRequestStateEnum.DUPLICATED = Symbol('DISAPPROVED');
