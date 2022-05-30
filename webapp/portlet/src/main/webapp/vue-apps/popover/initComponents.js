import Popover from './components/Popover.vue';
import PopoverUser from './components/PopoverUser.vue';

const components = {
  'popover': Popover,
  'popover-user': PopoverUser,
};

for (const key in components) {
  Vue.component(key, components[key]);
}