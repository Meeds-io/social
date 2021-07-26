import SpaceMenu from './components/SpaceMenu.vue';
import SpaceTitleActionComponents from './components/SpaceTitleActionComponents.vue';


const components = {
  'space-menu': SpaceMenu,
  'space-title-action-components': SpaceTitleActionComponents,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
