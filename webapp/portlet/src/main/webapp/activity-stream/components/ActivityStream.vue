<template>
  <v-app v-if="loaded" class="white">
    <activity-stream-feature-switch />
    <activity-stream-list
      :activity-id="activityId"
      :activity-types="activityTypes"
      :activity-actions="activityActions"
      :comment-types="commentTypes"
      :comment-actions="commentActions" />
    <div class="drawer-parent">
      <activity-comments-drawer
        :comment-types="commentTypes"
        :comment-actions="commentActions" />
    </div>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    loaded: false,
    activityId: null,
    activityTypes: {},
    activityActions: {},
    commentTypes: {},
    commentActions: {},
    extensionApp: 'activity',
    activityTypeExtension: 'type',
    activityActionExtension: 'action',
    commentTypeExtension: 'comment-type',
    commentActionExtension: 'comment-action',
  }),
  created() {
    document.addEventListener(`extension-${this.extensionApp}-${this.activityTypeExtension}-updated`, this.refreshActivityTypes);
    document.addEventListener(`extension-${this.extensionApp}-${this.activityActionExtension}-updated`, this.refreshActivityActions);
    document.addEventListener(`extension-${this.extensionApp}-${this.commentTypeExtension}-updated`, this.refreshCommentTypes);
    document.addEventListener(`extension-${this.extensionApp}-${this.commentActionExtension}-updated`, this.refreshCommentActions);
    this.refreshActivityTypes();
    this.refreshActivityActions();
    this.refreshCommentTypes();
    this.refreshCommentActions();
    if (window.location.pathname.indexOf(this.$root.activityBaseLink) === 0) {
      this.activityId = this.getQueryParam('id');
    }
    this.loaded = true;
  },
  methods: {
    refreshActivityTypes() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.activityTypeExtension);
      let changed = false;
      extensions.forEach(extension => {
        if (extension.type && extension.options && (!this.activityTypes[extension.type] || this.activityTypes[extension.type] !== extension.options)) {
          this.activityTypes[extension.type] = extension.options;
          changed = true;
        }
      });
      // force update of attribute to re-render switch new extension type
      if (changed) {
        this.activityTypes = Object.assign({}, this.activityTypes);
      }
    },
    refreshActivityActions() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.activityActionExtension);
      extensions.forEach(extension => {
        if (extension.id) {
          this.activityActions[extension.id] = extension;
        }
      });
    },
    refreshCommentTypes() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.commentTypeExtension);
      let changed = false;
      extensions.forEach(extension => {
        if (extension.type && extension.options && (!this.commentTypes[extension.type] || this.commentTypes[extension.type] !== extension.options)) {
          this.commentTypes[extension.type] = extension.options;
          changed = true;
        }
      });
      // force update of attribute to re-render switch new extension type
      if (changed) {
        this.commentTypes = Object.assign({}, this.commentTypes);
      }
    },
    refreshCommentActions() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.commentActionExtension);
      extensions.forEach(extension => {
        if (extension.id) {
          this.commentActions[extension.id] = extension;
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