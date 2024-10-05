import SpacesList from './components/SpacesList.vue';
import SpacesToolbar from './components/SpacesToolbar.vue';
import SpacesCardList from './components/SpacesCardList.vue';
import SpacesListFilterDrawer from './components/SpacesListFilterDrawer.vue';
import SpaceCard from './components/SpaceCard.vue';
import SpaceCardButton from './components/SpaceCardButton.vue';
import SpaceCardMembershipOptions from './components/SpaceCardMembershipOptions.vue';
import SpaceCardUnreadBadge from './components/SpaceCardUnreadBadge.vue';
import SpaceFormDrawer from './components/SpaceFormDrawer.vue';
import SpaceFavoriteAction from './components/SpaceFavoriteAction.vue';

const components = {
  'spaces-list': SpacesList,
  'spaces-toolbar': SpacesToolbar,
  'spaces-card-list': SpacesCardList,
  'spaces-list-filter-drawer': SpacesListFilterDrawer,
  'space-card': SpaceCard,
  'space-card-button': SpaceCardButton,
  'space-card-membership-options': SpaceCardMembershipOptions,
  'space-card-unread-badge': SpaceCardUnreadBadge,
  'space-form-drawer': SpaceFormDrawer,
  'space-favorite-action': SpaceFavoriteAction,
};

for (const key in components) {
  Vue.component(key, components[key]);
}

//get overrided components if exists
if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesList');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}
