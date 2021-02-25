<template>
  <v-app class="white">
    <div class="space-title-action-components">
      <div
        v-for="action in enabledComponents"
        :key="action.key"
        :class="`${action.appClass} ${action.typeClass}`" :ref="action.key">
      </div>
    </div>
  </v-app>
</template>
<script>
import { spaceTitleActionComponents } from '../extension.js';
export default {
  components: {
    spaceTitleActionComponents
  },
  computed: {
    enabledComponents() {
      return spaceTitleActionComponents.filter(action => action.enabled);
    },
  },
  mounted() {
    spaceTitleActionComponents.map(action => this.initTitleActionComponent(action));
  },
  methods: {
    initTitleActionComponent(action) {
      if (action.init && !action.isStartedInit && action.enabled) {
        action.isStartedInit = true;
        let container = this.$refs[action.key];
        if (container && container.length > 0) {
          container = container[0];
          action.init(container, eXo.env.portal.spaceName);
        } else {
          // eslint-disable-next-line no-console
          console.error(
            `Error initialization of the ${action.key} action component: empty container`
          );
        }
      }
    }
  }
};
</script>
