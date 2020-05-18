import AbstractEnum from './AbstractEnum';

/**
 * Script category enums.
 * More information see BE class IdmScriptCategory.java
 * with all definition.
 */
export default class ScriptCategoryEnum extends AbstractEnum {

  static getNiceLabel(key) {
    return super.getNiceLabel(`core:enums.ScriptCategoryEnum.${key}`);
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
    //
    const sym = super.findSymbolByKey(this, key);
    //
    switch (sym) {
      case this.TRANSFORM_FROM: {
        return 'info';
      }
      case this.TRANSFORM_TO: {
        return 'success';
      }
      case this.SYSTEM: {
        return 'warning';
      }
      default: {
        return 'default';
      }
    }
  }
}

ScriptCategoryEnum.DEFAULT = Symbol('DEFAULT');
ScriptCategoryEnum.TRANSFORM_FROM = Symbol('TRANSFORM_FROM');
ScriptCategoryEnum.TRANSFORM_TO = Symbol('TRANSFORM_TO');
ScriptCategoryEnum.SYSTEM = Symbol('SYSTEM');
