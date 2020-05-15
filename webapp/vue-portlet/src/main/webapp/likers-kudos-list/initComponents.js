import ExoLikersAndKudosApp from './components/ExoLikersAndKudosApp.vue';
import ExoLikersAndKudosDrawer from './components/ExoLikersAndKudosDrawer.vue';
import ExoLikersAndKudosListItem from './components/ExoLikersAndKudosListItem.vue';

const components = {
  'exo-likers-kudos': ExoLikersAndKudosApp,
  'exo-likers-kudos-drawer': ExoLikersAndKudosDrawer,
  'exo-likers-kudos-list-item': ExoLikersAndKudosListItem
};

for (const key in components) {
  Vue.component(key, components[key]);
}
