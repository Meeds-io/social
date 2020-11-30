<template>
  <v-app class="white">
    <div class="space-title-action-components">
      <div
        v-for="action in spaceMenuCallActionComponents"
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
        {{ initTitleActionComponent(action) }}
      </div>
    </div>
  </v-app>
</template>
<script>
import { spaceMenuCallActionComponents } from '../extension.js';
export default {
  props: {
    navigations: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      spaceMenuCallActionComponents: spaceMenuCallActionComponents,
      isMounted: null,
      resolveMounting: null
    };
  },
  created() {
    document.addEventListener('refreshSpaceNavigations', () => {
      this.$spaceService
        .getSpaceNavigations(eXo.env.portal.spaceId)
        .then(data => {
          // Compute URI of nodes of old navigation
          if (data && data.length) {
            data.forEach(nav => {
              const oldNav = this.navigations.find(
                oldNav => oldNav.id === nav.id
              );
              if (oldNav) {
                nav.uri = oldNav.uri;
              } else if (nav.uri && nav.uri.indexOf('/') >= 0) {
                nav.uri = nav.uri.split('/')[1];
              }
            });
            this.navigations = data;
          }
          return this.$nextTick();
        })
        .then(() => this.$root.$emit('application-loaded'));
    });

    const thevue = this;
    this.isMounted = new Promise(function(resolve) {
      thevue.resolveMounting = resolve;
    });
  },
  mounted() {
    this.$root.$emit('application-loaded');
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