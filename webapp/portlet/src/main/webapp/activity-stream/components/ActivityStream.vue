<template>
  <v-app v-if="loaded">
    <activity-stream-feature-switch />
    <exo-activity-composer v-if="spaceId" id="activityComposer" />
    <activity-stream-list
      :activity-id="activityId"
      :activity-types="activityTypes"
      :activity-actions="activityActions"
      :comment-types="activityTypes"
      :comment-actions="commentActions"
      @activity-select="displayActivityDetail" />
    <div class="drawer-parent">
      <activity-comments-drawer
        :comment-types="activityTypes"
        :comment-actions="commentActions" />
    </div>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    loaded: false,
    spaceId: eXo.env.portal.spaceId,
    activityId: null,
    activityTypes: {},
    activityActions: {},
    commentActions: {},
    extensionApp: 'activity',
    activityTypeExtension: 'type',
    activityActionExtension: 'action',
    commentActionExtension: 'comment-action',
  }),
  created() {
    document.addEventListener(`extension-${this.extensionApp}-${this.activityTypeExtension}-updated`, this.refreshActivityTypes);
    document.addEventListener(`extension-${this.extensionApp}-${this.activityActionExtension}-updated`, this.refreshActivityActions);
    document.addEventListener(`extension-${this.extensionApp}-${this.commentActionExtension}-updated`, this.refreshCommentActions);
    this.refreshActivityTypes();
    this.refreshActivityActions();
    this.refreshCommentActions();
    if (window.location.pathname.indexOf(this.$root.activityBaseLink) === 0) {
      this.$root.selectedActivityId = this.getQueryParam('id');
      if (window.location.hash) {
        this.$root.selectedCommentId = window.location.hash.replace('#comment-', '');
      }
    }
    this.displayActivityDetail(this.$root.selectedActivityId, this.$root.selectedCommentId);
  },
  methods: {
    displayActivityDetail(activityId, commentId) {
      this.loaded = false;
      this.$root.selectedActivityId = this.activityId = activityId;
      this.$root.selectedCommentId = window.location.hash.replace('#comment-', '');
      if (commentId && !this.$root.selectedCommentId) {
        this.$root.selectedCommentId = commentId;
        window.history.replaceState('', window.document.title, `${window.location.pathname}?id=${activityId}#comment-${commentId}`);
      }
      this.$nextTick().then(() => this.loaded = true);
    },
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