import ConfetiAnimation from './components/ConfetiAnimation.vue';

const components = {
  'confeti-animation': ConfetiAnimation,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
