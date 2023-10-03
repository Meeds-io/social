<template>
  <v-app v-if="loaded" role="main">
    <activity-stream-toolbar
      v-if="canPostInitialized"
      :can-post="canPost"
      :can-filter="canFilter"
      :filter="filter"
      :has-activities="hasActivities" />
    <activity-stream-list
      :activity-id="activityId"
      :activity-types="activityTypes"
      :activity-actions="activityActions"
      :comment-types="commentTypes"
      :comment-actions="commentActions"
      @has-activities="hasActivities = $event"
      @activity-select="displayActivityDetail"
      @can-post-loaded="canPostLoaded($event)" />
    <extension-registry-components
      :params="drawerParams"
      name="ActivityStream"
      type="activity-stream-drawers"
      parent-element="div"
      element="div"
      class="drawer-parent" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    loaded: false,
    canPostInitialized: false,
    spaceId: eXo.env.portal.spaceId,
    forceReload: false,
    canPost: false,
    hasActivities: false,
    activityId: null,
    activityTypes: {},
    activityActions: {},
    commentActions: {},
    extensionApp: 'activity',
    activityTypeExtension: 'type',
    activityActionExtension: 'action',
    commentActionExtension: 'comment-action',
  }),
  computed: {
    commentTypes() {
      // We will keep for now the same declared types
      // for comments and activites
      return this.activityTypes;
    },
    drawerParams() {
      return {
        activityTypes: this.activityTypes,
        activityActions: this.activityActions,
        commentTypes: this.commentTypes,
        commentActions: this.commentActions,
      };
    },
    canFilter() {
      return !this.$root.selectedActivityId;
    }
  },
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
        this.$root.selectedCommentId = window.location.hash.replace('#comment-reply-', '').replace('#comment-reply', '').replace('#comment-', '');
      }
    }
    this.displayActivityDetail(this.$root.selectedActivityId, this.$root.selectedCommentId);
  },
  methods: {
    displayActivityDetail(activityId, commentId) {
      this.loaded = false;
      this.$root.selectedActivityId = this.activityId = activityId;
      this.$root.selectedCommentId = window.location.hash.replace('#comment-reply-', '').replace('#comment-reply', '').replace('#comment-', '');
      if (commentId && !this.$root.selectedCommentId) {
        this.$root.selectedCommentId = commentId;
        window.history.replaceState('', window.document.title, `${window.location.pathname}?id=${activityId}#comment-${commentId}`);
        this.forceReload = true;
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
          this.$set(this.activityActions, extension.id, extension);
        }
      });
    },
    refreshCommentActions() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.commentActionExtension);
      extensions.forEach(extension => {
        if (extension.id) {
          this.$set(this.commentActions, extension.id, extension);
        }
      });
    },
    getQueryParam(paramName) {
      const uri = window.location.search.substring(1);
      const params = new URLSearchParams(uri);
      return params.get(paramName);
    },
    canPostLoaded(canPost) {
      this.canPost = canPost;
      this.canPostInitialized = true;
    },
  },
};
</script>
