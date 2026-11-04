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
    /**
     * Optional predicate on a registered component: a component it refuses is
     * neither initialized nor rendered by this instance, so several instances
     * can split one extension point between them.
     */
    filter: {
      type: Function,
      default: null,
    },
  },
  data: () => ({
    components: [],
  }),
  computed: {
    /**
     * Whether at least one component is rendered.
     *
     * @returns {Number} the number of rendered components, falsy when none
     */
    isEnabled() {
      return this.components.length;
    },
  },
  /**
   * Listens to the extension point's registrations, then registers the
   * components already registered there.
   *
   * @returns {void}
   */
  created() {
    // Not reactive: the latest registered component of each id; an earlier one whose check resolves later is not added
    this.latestRegistrations = new Map();
    document.addEventListener(`component-${this.name}-${this.type}-updated`, this.updateComponents);
    const registeredComponents = extensionRegistry.loadComponents(this.name);
    if (this.strictType) {
      registeredComponents.filter(comp => comp.componentName === this.type).forEach(component => {
        this.registerComponent(component);
      });
    } else {
      registeredComponents.forEach(component => {
        this.registerComponent(component);
      });
    }
  },
  /**
   * Stops listening to the extension point's registrations.
   *
   * @returns {void}
   */
  beforeDestroy() {
    document.removeEventListener(`component-${this.name}-${this.type}-updated`, this.updateComponents);
  },
  methods: {
    /**
     * Registers the component an extension point event carries.
     *
     * @param {CustomEvent} event the registration event, whose detail is the
     *          registered component
     * @returns {void}
     */
    updateComponents(event) {
      const component = event && event.detail;
      if (component) {
        if (!this.strictType || component.componentName === this.type) {
          this.registerComponent(component);
        }
      }
    },
    /**
     * Initializes a component, then adds it to the rendered rows when its
     * filter and isEnabled accept it. A component with an id becomes the
     * latest registration of that id.
     *
     * @param {Object} component the registered component
     * @returns {Promise|void} the pending addition when init is asynchronous
     */
    registerComponent(component) {
      if (this.filter && !this.filter(component)) {
        return;
      }
      if (component.componentOptions.id) {
        this.latestRegistrations.set(component.componentOptions.id, component);
      }
      if (component.componentOptions.init) {
        const initResult = component.componentOptions.init(this.params);
        if (initResult.then) {
          return initResult
            .then(() => this.addComponent(component));
        }
      }
      this.addComponent(component);
    },
    /**
     * Adds a component to the rendered rows once its isEnabled resolves to true.
     * The rows are read after that resolution, never before it: another
     * registration may have replaced them meanwhile, and a row added to an
     * earlier copy would never render. A component that a later registration
     * of its id has superseded is not added, whichever check resolves first.
     *
     * @param {Object} component the registered component
     * @returns {Promise<void>} resolved once the component is added or skipped
     */
    async addComponent(component) {
      if (!component.componentOptions.isEnabled || await Promise.resolve(component.componentOptions.isEnabled(this.params))) {
        if (component.componentOptions.id && this.latestRegistrations.get(component.componentOptions.id) !== component) {
          return;
        }
        const components = this.components.slice();
        const existingComponentIndex = components.findIndex(cmp => cmp.componentOptions.id === component.componentOptions.id);
        if (existingComponentIndex >= 0) {
          components.splice(existingComponentIndex, 1, component);
        } else {
          components.push(component);
        }
        components.sort(this.sort);
        this.components = components;
      }
    },
    /**
     * Orders two components by their rank, a missing rank counting as 0.
     *
     * @param {Object} comp1 the first component
     * @param {Object} comp2 the second component
     * @returns {Number} negative, zero or positive, as Array.sort expects
     */
    sort(comp1, comp2) {
      return (comp1.componentOptions.rank || 0) - (comp2.componentOptions.rank || 0);
    },
  },
};
</script>