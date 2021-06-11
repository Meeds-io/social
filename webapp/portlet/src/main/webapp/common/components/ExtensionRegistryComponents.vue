<template>
  <ul v-if="parentElement === 'ul'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :params="params" />
  </ul>
  <li v-else-if="parentElement === 'li'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :params="params" />
  </li>
  <span v-else-if="parentElement === 'span'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :params="params" />
  </span>
  <a v-else-if="parentElement === 'a'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :params="params" />
  </a>
  <div v-else>
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :params="params" />
  </div>
</template>

<script>
export default {
  props: {
    name: {
      type: String,
      default: null,
    },
    type: {
      type: String,
      default: null,
    },
    parentElement: {
      type: Object,
      default: () => null,
    },
    element: {
      type: Object,
      default: () => null,
    },
    params: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    components: [],
  }),
  created() {
    document.addEventListener(`component-${this.name}-${this.type}-updated`, event => {
      if (event && event.detail) {
        const components = this.components.slice();
        if (!components.find(component => component.componentOptions.id === event.detailcomponentOptions.id)) {
          components.push(event.detail);
          components.sort(this.sort);
          this.components = components;
        }
      }
    });
    const components = extensionRegistry.loadComponents(this.name);
    components.sort(this.sort);
    this.components = components;
  },
  methods: {
    sort(comp1, comp2) {
      return (comp1.componentOptions.rank || 0) - (comp2.componentOptions.rank || 0);
    },
  },
};
</script>