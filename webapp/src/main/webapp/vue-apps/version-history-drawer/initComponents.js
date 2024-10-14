import VersionHistoryDrawer from './components/VersionHistoryDrawer.vue';
import VersionCard from './components/VersionCard.vue';

const components = {
  'version-history-drawer': VersionHistoryDrawer,
  'version-card': VersionCard,
};

for (const key in components) {
  Vue.component(key, components[key]);
}