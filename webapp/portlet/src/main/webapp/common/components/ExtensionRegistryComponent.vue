<template>
  <li v-if="element === 'li'">
    <span :id="id"></span>
  </li>
  <span v-else-if="element === 'span'">
    <span :id="id"></span>
  </span>
  <a v-else-if="element === 'a'">
    <span :id="id"></span>
  </a>
  <div v-else-if="element === 'div'">
    <span :id="id"></span>
  </div>
  <div v-else :id="id"></div>
</template>

<script>
export default {
  props: {
    component: {
      type: Object,
      default: null,
    },
    params: {
      type: Object,
      default: null,
    },
    element: {
      type: String,
      default: () => null,
    },
  },
  data: () => ({
    id: `ExtRegistryComp${parseInt(Math.random() * 100000)}`,
  }),
  mounted() {
    const VueComponent = Vue.extend(this.component.componentOptions.vueComponent);
    const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);
    new VueComponent({
      propsData: this.params,
      i18n: new VueI18n({
        locale: this.$i18n.locale,
        messages: this.$i18n.messages,
      }),
      vuetify: vuetify,
      el: `#${this.id}`,
    });
  },
};
</script>