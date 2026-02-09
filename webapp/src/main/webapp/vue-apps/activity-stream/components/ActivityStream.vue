<template>
  <v-app v-if="loaded" class="activity-stream">
    <v-main ref="main" class="application-body">
      <activity-stream-toolbar
        v-if="canPostInitialized"
        :can-post="$root.canPost"
        :can-filter="canFilter"
        :filter="filter"
        :has-activities="hasActivities" />
      <categories-filter
        v-if="$root.allowFilteringPerCategory && !$root.selectedActivityId"
        v-show="hasActivities"
        v-model="$root.selectedCategoryId"
        :category-depth="$root.categoryDepth"
        :category-ids="$root.settings?.categoryIds"
        :exclude-category-ids="$root.settings?.excludeCategoryIds"
        :space-id="$root.spaceId"
        class="full-width border-box-sizing application-background-color application-border application-border-radius py-2 px-3 mb-5"
        object-type="activity"
        hide-on-empty />
      <activity-stream-list
        :activity-id="activityId"
        :activity-types="activityTypes"
        :activity-actions="activityActions"
        :comment-types="commentTypes"
        :comment-actions="commentActions"
        @has-activities="hasActivities = $event"
        @activity-select="displayActivityDetail"
        @can-post-loaded="canPostLoaded($event)" />
    </v-main>
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
    hasActivities: false,
    activityId: null,
    activityTypes: {},
    activityActions: {},
    commentActions: {},
    extensionApp: 'activity',
    activityTypeExtension: 'type',
    activityActionExtension: 'action',
    commentActionExtension: 'comment-action',
    activityActionTypeExtension: 'expand-action-type',
    resizeObserver: null
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
    document.addEventListener(`extension-${this.extensionApp}-${this.activityActionTypeExtension}-updated`, this.refreshExpandActionTypes);
    this.refreshExpandActionTypes();
    this.refreshActivityTypes();
    this.refreshActivityActions();
    this.refreshCommentActions();
    const queryParamId = this.$utils.getQueryParam('id');
    if (queryParamId && queryParamId.includes('comment')) {
      this.$root.selectedCommentId = queryParamId;
    }
    if (window.location.pathname.endsWith('/activity')) {
      this.$root.selectedActivityId = queryParamId;
      if (window.location.hash) {
        this.$root.selectedCommentId = window.location.hash.replace('#comment-reply-', '').replace('#comment-reply', '').replace('#comment-', '');
      }
    }
    this.displayActivityDetail(this.$root.selectedActivityId, this.$root.selectedCommentId);
  },
  mounted() {
    const urlHash = window.location.hash;
    if (urlHash) {
      const elementId = urlHash.substring(1);
      const interval = setInterval(() => {
        const targetElement = document.getElementById(elementId);
        if (targetElement) {
          targetElement.scrollIntoView({ behavior: 'smooth' });
          clearInterval(interval);
        }
      }, 500);
    }
    this.initResizeObserver();
  },
  beforeDestroy() {
    document.removeEventListener(`extension-${this.extensionApp}-${this.activityTypeExtension}-updated`, this.refreshActivityTypes);
    document.removeEventListener(`extension-${this.extensionApp}-${this.activityActionExtension}-updated`, this.refreshActivityActions);
    document.removeEventListener(`extension-${this.extensionApp}-${this.commentActionExtension}-updated`, this.refreshCommentActions);
    document.removeEventListener(`extension-${this.extensionApp}-${this.activityActionTypeExtension}-updated`, this.refreshExpandActionTypes);
    this.resizeObserver?.disconnect();
  },
  methods: {
    displayActivityDetail(activityId, commentId) {
      this.loaded = false;
      this.$root.selectedActivityId = this.activityId = activityId;
      const urlHash = window.location.hash;
      if (urlHash && urlHash.includes('#comment') || commentId && commentId.includes('comment')){
        this.$root.selectedCommentId = window.location.hash.replace('#comment-reply-', '').replace('#comment-reply', '').replace('#comment-', '');
        if (commentId && !this.$root.selectedCommentId) {
          this.$root.selectedCommentId = commentId;
        }
        window.history.replaceState('', window.document.title, `${window.location.pathname}?id=${activityId}#comment-${commentId}`);
      } else {
        this.$root.selectedCommentId = '';
        if (activityId) {
          window.history.replaceState('', '', `${window.location.pathname}?id=${activityId}${urlHash}`);
        } else {
          window.history.replaceState('', '', `${window.location.pathname}${urlHash}`);
        }
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
    refreshExpandActionTypes() {
      const extensions = extensionRegistry.loadExtensions(this.extensionApp, this.activityActionTypeExtension);
      extensions.forEach(extension => {
        if (extension.id) {
          this.$root.displayCommentActionTypes.push(extension.id);
        }
      });
    },
    canPostLoaded(canPost) {
      this.$root.canPost = !!canPost;
      this.canPostInitialized = true;
    },
    initResizeObserver() {
      if (this.$refs.main?.$el) {
        this.resizeObserver = new ResizeObserver(this.refreshSize);
        this.resizeObserver.observe(this.$refs.main.$el);
      } else {
        console.debug('Stream main element not found'); // eslint-disable-line no-console
        window.setTimeout(this.initResizeObserver, 50);
      }
    },
    refreshSize() {
      if (this.$refs.main?.$el) {
        const reduced =  this.$refs.main.$el.clientWidth < this.$vuetify.breakpoint.thresholds.sm;
        if (this.$root.reducedWidth !== reduced) {
          this.$root.reducedWidth = reduced;
        }
      }
    }
  },
};
</script>
