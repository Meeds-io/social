import SpaceHeader from './components/SpaceHeader.vue';
import SpaceHeaderBannerButton from './components/SpaceHeaderBannerButton.vue';

const components = {
  'space-header': SpaceHeader,
  'space-header-banner-button': SpaceHeaderBannerButton,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
