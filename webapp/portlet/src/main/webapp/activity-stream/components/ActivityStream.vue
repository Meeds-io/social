<template>
  <v-app v-if="loaded" class="white">
    <activity-stream-feature-switch />
    <activity-stream-list
      :activity-id="activityId"
      :activity-types="activityTypes" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    loaded: false,
    activityId: null,
    activityTypes: {},
    extensionApp: 'activity',
    extensionType: 'types',
  }),
  created() {
    document.addEventListener(`extension-${this.extensionApp}-${this.extensionType}-updated`, this.refreshActivityTypes);
    this.refreshActivityTypes();
    if (window.location.pathname.indexOf(this.$root.activityBaseLink) === 0) {
      this.activityId = this.getQueryParam('id');
    }
    this.loaded = true;
  },
  methods: {
    refreshActivityTypes() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.extensionType);
      extensions.forEach(extension => {
        if (extension.type && extension.options && (!this.activityTypes[extension.type] || this.activityTypes[extension.type] !== extension.options)) {
          this.activityTypes[extension.type] = extension.options;
        }
      });
    },
    getQueryParam(paramName) {
      const uri = window.location.search.substring(1);
      const params = new URLSearchParams(uri);
      return params.get(paramName);
    },
  },
};
</script>