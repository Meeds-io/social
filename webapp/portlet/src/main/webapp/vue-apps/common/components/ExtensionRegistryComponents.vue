<template>
  <ul v-if="parentElement === 'ul' && isEnabled">
    <template v-if="$slots.header && components.length">
      <slot name="header"></slot>
    </template>
    <template v-for="(component, index) in components">
      <extension-registry-component
        :key="component.componentOptions.id || component.componentOptions.componentName"
        :component="component"
        :element="element"
        :element-class="elementClass"
        :params="params" />
      <template v-if="$slots.separator && (index + 1) < components.length">
        <slot name="separator"></slot>
      </template>
    </template>
    <template v-if="$slots.footer && components.length">
      <slot name="footer"></slot>
    </template>
  </ul>
  <li v-else-if="parentElement === 'li' && isEnabled">
    <template v-if="$slots.header && components.length">
      <slot name="header"></slot>
    </template>
    <template v-for="(component, index) in components">
      <extension-registry-component
        :key="component.componentOptions.id || component.componentOptions.componentName"
        :component="component"
        :element="element"
        :element-class="elementClass"
        :params="params" />
      <template v-if="$slots.separator && (index + 1) < components.length">
        <slot name="separator"></slot>
      </template>
    </template>
    <template v-if="$slots.footer && components.length">
      <slot name="footer"></slot>
    </template>
  </li>
  <span v-else-if="parentElement === 'span' && isEnabled">
    <template v-if="$slots.header && components.length">
      <slot name="header"></slot>
    </template>
    <template v-for="(component, index) in components">
      <extension-registry-component
        :key="component.componentOptions.id || component.componentOptions.componentName"
        :component="component"
        :element="element"
        :element-class="elementClass"
        :params="params" />
      <template v-if="$slots.separator && (index + 1) < components.length">
        <slot name="separator"></slot>
      </template>
    </template>
    <template v-if="$slots.footer && components.length">
      <slot name="footer"></slot>
    </template>
  </span>
  <a v-else-if="parentElement === 'a' && isEnabled">
    <template v-if="$slots.header && components.length">
      <slot name="header"></slot>
    </template>
    <template v-for="(component, index) in components">
      <extension-registry-component
        :key="component.componentOptions.id || component.componentOptions.componentName"
        :component="component"
        :element="element"
        :element-class="elementClass"
        :params="params" />
      <template v-if="$slots.separator && (index + 1) < components.length">
        <slot name="separator"></slot>
      </template>
    </template>
    <template v-if="$slots.footer && components.length">
      <slot name="footer"></slot>
    </template>
  </a>
  <div v-else-if="isEnabled">
    <template v-if="$slots.header && components.length">
      <slot name="header"></slot>
    </template>
    <template v-for="(component, index) in components">
      <extension-registry-component
        :key="component.componentOptions.id || component.componentOptions.componentName"
        :component="component"
        :element="element"
        :element-class="elementClass"
        :params="params" />
      <template v-if="$slots.separator && (index + 1) < components.length">
        <slot name="separator"></slot>
      </template>
    </template>
    <template v-if="$slots.footer && components.length">
      <slot name="footer"></slot>
    </template>
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
      type: String,
      default: () => '',
    },
    params: {
      type: Object,
      default: null,
    },
    strictType: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    components: [],
  }),
  computed: {
    isEnabled() {
      return this.components.length;
    },
  },
  created() {
    document.addEventListener(`component-${this.name}-${this.type}-updated`, event => {
      const component = event && event.detail;
      if (component) {
        if (!this.strictType || component.componentName === this.type) {
          const components = this.components.slice();
          this.registerComponent(component, components);
          this.components = components;
        }
      }
    });
    const registeredComponents = extensionRegistry.loadComponents(this.name);
    const components = [];
    if (this.strictType) {
      registeredComponents.filter(comp => comp.componentName === this.type).forEach(component => {
        this.registerComponent(component, components);
      });
    } else {
      registeredComponents.forEach(component => {
        this.registerComponent(component, components);
      });
    }
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