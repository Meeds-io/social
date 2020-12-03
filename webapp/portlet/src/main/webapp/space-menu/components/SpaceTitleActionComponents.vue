<template>
  <v-app class="white">
    <div class="space-title-action-components">
      <div
        v-for="action in spaceTitleActionComponents"
        v-if="action.enabled"
        :key="action.key"
        :class="`${action.appClass} ${action.typeClass}`"
        :ref="action.key"
      >
        <div v-if="action.component">
          <component
            v-dynamic-events="action.component.events"
            v-bind="action.component.props ? action.component.props : {}"
            :is="action.component.name"
          ></component>
        </div>
        <div v-else-if="action.element" v-html="action.element.outerHTML"></div>
        <div v-else-if="action.html" v-html="action.html"></div>
        {{ initTitleActionComponents }}
      </div>
    </div>
  </v-app>
</template>
<script>
import { spaceTitleActionComponents } from '../extension.js';
export default {
  data() {
    return {
      spaceTitleActionComponents: spaceTitleActionComponents,
      isMounted: null,
      resolveMounting: null
    };
  },
  computed: {
    initTitleActionComponents() {
      let components;
      spaceTitleActionComponents.map(action => components = this.initTitleActionComponent(action));
      return components;
    }
  },
  created() {
    const thevue = this;
    this.isMounted = new Promise(function(resolve) {
      thevue.resolveMounting = resolve;
    });
  },
  mounted() {
    this.resolveMounting();
  },
  methods: {
    initTitleActionComponent(action) {
      if (action.init && !action.isStartedInit && action.enabled) {
        action.isStartedInit = true;
        this.isMounted.then(() => {
          let container = this.$refs[action.key];
          if (container && container.length > 0) {
            container = container[0];
            action.init(container, eXo.env.portal.spaceName);
          } else {
            console.error(
              `Error initialization of the ${action.key} action component: empty container`
            );
          }
        });
      }
    }
  }
};
</script>