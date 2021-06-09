<template>
  <v-app v-if="loaded" class="white">
    <activity-stream-feature-switch />
    <activity-stream-list
      :activity-id="activityId"
      :activity-types="activityTypes"
      :activity-actions="activityActions" />
    <share-activity-drawer />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    loaded: false,
    activityId: null,
    activityTypes: {},
    activityActions: {},
    extensionApp: 'activity',
    activityTypeExtension: 'type',
    activityActionExtension: 'action',
  }),
  created() {
    document.addEventListener(`extension-${this.extensionApp}-${this.activityTypeExtension}-updated`, this.refreshActivityTypes);
    document.addEventListener(`extension-${this.extensionApp}-${this.activityActionExtension}-updated`, this.refreshActivityActions);
    this.refreshActivityTypes();
    this.refreshActivityActions();
    if (window.location.pathname.indexOf(this.$root.activityBaseLink) === 0) {
      this.activityId = this.getQueryParam('id');
    }
    this.loaded = true;
  },
  methods: {
    refreshActivityTypes() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.activityTypeExtension);
      extensions.forEach(extension => {
        if (extension.type && extension.options && (!this.activityTypes[extension.type] || this.activityTypes[extension.type] !== extension.options)) {
          this.activityTypes[extension.type] = extension.options;
        }
      });
    },
    refreshActivityActions() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.activityActionExtension);
      extensions.forEach(extension => {
        if (extension.id) {
          this.activityActions[extension.id] = extension;
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