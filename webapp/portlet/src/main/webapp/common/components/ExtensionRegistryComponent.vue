<template>
  <li
    v-if="element === 'li'"
    :id="id"
    :class="elementClass">
    <span></span>
  </li>
  <span
    v-else-if="element === 'span'"
    :id="id"
    :class="elementClass">
    <span></span>
  </span>
  <a
    v-else-if="element === 'a'"
    :id="id"
    :class="elementClass">
    <span></span>
  </a>
  <div
    v-else
    :id="id"
    :class="elementClass">
    <span></span>
  </div>
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
    elementClass: {
      type: String,
      default: () => '',
    },
  },
  data: () => ({
    id: `ExtRegistryComp${parseInt(Math.random() * 100000)}`,
    mounted: false,
  }),
  mounted() {
    if (!this.mounted) {
      this.mounted = true;
      const VueComponent = Vue.extend(this.component.componentOptions.vueComponent);
      const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);
      new VueComponent({
        propsData: this.params,
        i18n: new VueI18n({
          locale: this.$i18n.locale,
          messages: this.$i18n.messages,
        }),
        vuetify: vuetify,
        el: `#${this.id} :first-child`,
      });
    }
  },
};
</script>