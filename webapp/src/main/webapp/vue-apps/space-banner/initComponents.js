import SpaceBanner from './components/SpaceBanner.vue';
import SpaceBannerSettingButtons from './components/SpaceBannerSettingButtons.vue';

const components = {
  'space-banner': SpaceBanner,
  'space-banner-setting-buttons': SpaceBannerSettingButtons,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
