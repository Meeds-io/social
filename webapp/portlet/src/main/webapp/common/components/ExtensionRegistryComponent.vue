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
    randomId: String(parseInt(Math.random() * 100000)),
    mounted: false,
  }),
  computed: {
    id() {
      return `Ext${this.component.componentName}-${this.randomId}`;
    },
    ExtVueComponent() {
      return this.component && this.component.componentOptions && Vue.extend(this.component.componentOptions.vueComponent);
    },
  },
  mounted() {
    if (!this.mounted && this.ExtVueComponent) {
      this.mounted = true;
      new this.ExtVueComponent({
        propsData: this.params,
        i18n: this.$i18n,
        vuetify: this.vuetifyOptions,
        el: `#${this.id} > :first-child`,
      });
    }
  },
};
</script>