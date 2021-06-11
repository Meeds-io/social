<template>
  <ul v-if="parentElement === 'ul'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :element-class="elementClass"
      :params="params" />
  </ul>
  <li v-else-if="parentElement === 'li'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :element-class="elementClass"
      :params="params" />
  </li>
  <span v-else-if="parentElement === 'span'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :element-class="elementClass"
      :params="params" />
  </span>
  <a v-else-if="parentElement === 'a'">
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :element-class="elementClass"
      :params="params" />
  </a>
  <div v-else>
    <extension-registry-component
      v-for="component in components"
      :key="component.componentOptions.id || component.componentOptions.componentName"
      :component="component"
      :element="element"
      :element-class="elementClass"
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
    elementClass: {
      type: Object,
      default: () => '',
    },
    params: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    components: [],
    loaded: false,
  }),
  created() {
    document.addEventListener(`component-${this.name}-${this.type}-updated`, event => {
      const component = event && event.detail;
      if (component) {
        const components = this.components.slice();
        this.registerComponent(component, components);
        this.components = components;
      }
    });
    const registeredComponents = extensionRegistry.loadComponents(this.name);
    const components = [];
    registeredComponents.forEach(component => {
      this.registerComponent(component, components);
    });
    this.components = components;
  },
  methods: {
    registerComponent(component, components) {
      if (component.componentOptions.init) {
        const initResult = component.componentOptions.init(this.params);
        if (initResult.then) {
          return initResult
            .then(() => this.addComponent(component, components));
        }
      }
      this.addComponent(component, components);
    },
    addComponent(component, components) {
      if (!component.componentOptions.isEnabled || component.componentOptions.isEnabled(this.params)) {
        const existingComponentIndex = components.findIndex(cmp => cmp.componentOptions.id === component.componentOptions.id);
        if (existingComponentIndex >= 0) {
          components.splice(existingComponentIndex, 1, component);
        } else {
          components.push(component);
        }
        components.sort(this.sort);
      }
    },
    sort(comp1, comp2) {
      return (comp1.componentOptions.rank || 0) - (comp2.componentOptions.rank || 0);
    },
  },
};
</script>