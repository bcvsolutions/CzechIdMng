

import merge from 'object-assign';
import Immutable from 'immutable';
//
import { SELECT_NAVIGATION_ITEMS, SELECT_NAVIGATION_ITEM, I18N_READY, NAVIGATION_INIT, getNavigationItems, getNavigationItem } from './layoutActions';
import { AuthenticateService, ConfigService } from '../../modules/core/services';

const configService = new ConfigService();

const INITIAL_STATE = Immutable.Map({
  navigation: configService.getNavigation(), // all navigation items from enabled modules as Map
  selectedNavigationItems: ['home'], // homepage by default
  i18nReady: false              // localization context is ready
});

export function layout(state = INITIAL_STATE, action) {
  switch (action.type) {
    case SELECT_NAVIGATION_ITEMS: {
      const prevState = state.get('selectedNavigationItems');
      const newState = [];
      for (let i = 0; i < action.selectedNavigationItems.length; i++) {
        newState[i] = action.selectedNavigationItems[i] || (prevState.length > i ? prevState[i] : null);
      }
      return state.set('selectedNavigationItems', newState);
    }
    case SELECT_NAVIGATION_ITEM: {
      const prevState = state.get('selectedNavigationItems');
      const newState = [];
      // traverse to item parent
      let itemId = action.selectedNavigationItemId;
      while (itemId !== null) {
        let item = getNavigationItem(state.get('navigation'), itemId);
        if (!item) {
          break;
        }
        newState.splice(0, 0, item.id); // insert at start
        itemId = item.parentId;
      }
      return state.set('selectedNavigationItems', newState);
    }
    case I18N_READY: {
      return state.set('i18nReady', action.ready);
    }
    case NAVIGATION_INIT: {
      return state.set('navigation', action.navigation);
    }
    default:
      return state;
  }
}
