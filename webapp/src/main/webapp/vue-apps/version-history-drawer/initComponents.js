import VersionHistoryDrawer from './components/VersionHistoryDrawer.vue';
import VersionCard from './components/VersionCard.vue';
import VersionHistoryTimeline from './components/VersionHistoryTimeline.vue';

const components = {
  'version-history-drawer': VersionHistoryDrawer,
  'version-card': VersionCard,
  'version-history-timeline': VersionHistoryTimeline,
};

for (const key in components) {
  Vue.component(key, components[key]);
}