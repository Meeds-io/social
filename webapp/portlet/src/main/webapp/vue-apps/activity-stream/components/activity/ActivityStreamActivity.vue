<template>
  <unread-badge
    :id="id"
    :unread-metadata="unreadMetadata"
    :space-id="spaceId"
    class="white card-border-radius activity-detail flex flex-column"
    @read="markAsRead">
    <div v-if="displayLoading" class="d-flex">
      <v-progress-circular
        color="primary"
        size="32"
        indeterminate
        class="mx-auto my-10" />
    </div>
    <template v-else-if="!noExtension && extendedComponent">
      <activity-head
        v-if="!extendedComponent.overrideHeader"
        :activity="activity"
        :activity-actions="activityActions"
        :is-activity-detail="isActivityDetail"
        :activity-type-extension="activityTypeExtension"
        :hide-menu="hideMenu"
        :class="isActivityShared && 'py-4 px-0' || 'py-2 ps-4 pe-1'" />
      <template v-if="!loading">
        <extension-registry-component
          :component="extendedComponentOptions"
          :element="extendedComponent.element"
          :element-class="extendedComponent.class"
          :params="extendedComponentParams"
          class="d-flex flex-column" />
      </template>
      <template v-if="!hideFooter">
        <activity-footer
          v-if="!extendedComponent.overrideFooter"
          :activity="activity"
          :is-activity-detail="isActivityDetail"
          :activity-types="activityTypes"
          :activity-type-extension="activityTypeExtension" />
        <activity-comments-preview
          v-if="!extendedComponent.overrideComments"
          :activity="activity"
          :comment-types="commentTypes"
          :comment-actions="commentActions"
          class="px-4" />
      </template>
    </template>
    <template v-else>
      <activity-head
        :activity="activity"
        :activity-actions="activityActions"
        :is-activity-detail="isActivityDetail"
        :activity-type-extension="activityTypeExtension"
        :is-activity-shared="isActivityShared"
        :hide-menu="hideMenu"
        :class="isActivityShared && 'py-4 px-0' || 'py-2 ps-4 pe-1'" />
      <v-card 
        v-if="!loading" 
        color="transparent" 
        flat>
        <extension-registry-components
          v-if="initialized"
          :params="extendedComponentParams"
          :class="!isActivityShared && 'px-4'"
          name="ActivityContent"
          type="activity-content-extensions"
          parent-element="div"
          element="div"
          class="d-flex flex-column" />
      </v-card>
      <template v-if="!hideFooter">
        <activity-footer
          :activity="activity"
          :is-activity-detail="isActivityDetail"
          :activity-types="activityTypes"
          :activity-type-extension="activityTypeExtension" />
        <activity-comments-preview
          :activity="activity"
          :comment-types="commentTypes"
          :comment-actions="commentActions"
          class="px-4" />
      </template>
    </template>
  </unread-badge>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypes: {
      type: Object,
      default: null,
    },
    activityActions: {
      type: Object,
      default: null,
    },
    commentTypes: {
      type: Object,
      default: null,
    },
    commentActions: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
    isActivityShared: {
      type: Boolean,
      default: false,
    },
    hideFooter: {
      type: Boolean,
      default: false,
    },
    hideMenu: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    loading: false,
    initialized: false,
    noExtension: false,
    unreadMetadata: null,
  }),
  computed: {
    id() {
      return `activity-detail-${this.activityId}`;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityTypeExtension() {
      if (this.sharedActivityTypeExtension
          && this.sharedActivityTypeExtension.extendSharedActivity
          && this.sharedActivityTypeExtension.extendSharedActivity(this.activity, this.isActivityDetail)) {
        return this.sharedActivityTypeExtension;
      }
      if (!this.activity || !this.activityTypes) {
        return {};
      }
      return this.activityTypes[this.activityType] || this.activityTypes['default'] || {};
    },
    sharedActivity() {
      return this.activity && this.activity.originalActivity;
    },
    sharedActivityType() {
      return this.sharedActivity && this.sharedActivity.type;
    },
    sharedActivityTypeExtension() {
      if (this.noExtension || !this.sharedActivity || !this.activityTypes) {
        return null;
      }
      return this.activityTypes[this.sharedActivityType] || this.activityTypes['default'] || null;
    },
    extendedComponent() {
      if (this.noExtension) {
        return null;
      }
      const extendedComponent = this.activityTypeExtension && this.activityTypeExtension.getExtendedComponent && this.activityTypeExtension.getExtendedComponent(this.activity, this.isActivityDetail);
      if (extendedComponent) {
        return extendedComponent;
      }
      return this.sharedActivityTypeExtension && this.sharedActivityTypeExtension.getExtendedComponent && this.sharedActivityTypeExtension.getExtendedComponent(this.activity, this.isActivityDetail);
    },
    extendedComponentOptions() {
      return this.extendedComponent && {
        componentName: `Activity-${this.activityTypeExtension.id}`,
        componentOptions: {
          vueComponent: this.extendedComponent.component,
        },
      };
    },
    extendedComponentParams() {
      return {
        activity: this.activity,
        isActivityDetail: this.isActivityDetail,
        activityTypeExtension: this.activityTypeExtension,
        activityTypes: this.activityTypes,
        loading: this.loading,
      };
    },
    init() {
      return this.activityTypeExtension && this.activityTypeExtension.init;
    },
    displayLoading() {
      return this.activityLoading || !this.initialized;
    },
    activityType() {
      return this.activity && this.activity.type;
    },
    activityLoading() {
      return this.activity && this.activity.loading;
    },
    spaceId() {
      return this.activity?.activityStream?.space?.id || '';
    },
  },
  watch: {
    activityLoading() {
      if (!this.activityLoading) {
        this.retrieveActivityProperties();
      }
    },
    loading() {
      if (!this.loading) {
        this.setWindowTitle();
        this.$root.$emit('activity-loaded', this.activityId);
      }
    },
    displayLoading() {
      if (!this.displayLoading) {
        this.$emit('loaded');
      }
    },
    activityTypeExtension() {
      if (this.activityTypeExtension) {
        this.retrieveActivityProperties();
      }
    },
    initialized() {
      if (this.initialized && !this.isActivityShared) {
        this.unreadMetadata = this.activity?.metadatas?.unread?.length && this.activity?.metadatas?.unread[0];
      }
    },
  },
  created() {
    this.$root.$on('activity-extension-abort', this.abortSpecificExtension);
    this.$root.$on('activity-refresh-ui', this.retrieveActivityProperties);
    this.retrieveActivityProperties();
  },
  mounted() {
    if ((this.$root.selectedCommentId || this.$root.replyToComment) && this.activity && this.activity.id === this.$root.selectedActivityId) {
      window.setTimeout(() => {
        document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
          activity: this.activity,
          commentId: this.$root.selectedCommentId,
          newComment: this.$root.replyToComment,
          offset: 0,
          limit: 200, // To display all
        }}));
        this.$root.replyToComment = false;
      }, 50);
    }
    if (this.activity.highlight) {
      this.scrollTo(this.$el);
    }
    this.$root.$emit('activity-refreshed');
  },
  beforeDestroy() {
    this.$root.$off('activity-refresh-ui', this.retrieveActivityProperties);
    this.$root.$off('activity-extension-abort', this.abortSpecificExtension);
  },
  methods: {
    retrieveActivityProperties(activityId) {
      if (this.activityLoading || (activityId && activityId !== this.activityId)) {
        return;
      }
      this.loading = true;
      this.$nextTick(() => {
        if (this.init) {
          const initPromise = this.init(this.activity, this.isActivityDetail);
          if (initPromise && initPromise.then) {
            return initPromise
              .then(() => this.$nextTick())
              .finally(() => {
                this.loading = false;
                this.initialized = true;
              });
          }
        }
        this.loading = false;
        this.initialized = true;
      });
    },
    setWindowTitle() {
      if (this.isActivityDetail && this.activityTypeExtension) {
        let title = document.title || '';
        const titleKey = this.activityTypeExtension.windowTitlePrefixKey || 'activity.window.title';
        if (this.activityTypeExtension.getWindowTitle) {
          title = this.activityTypeExtension.getWindowTitle(this.activity) || '';
          title = this.$t(titleKey, {0: this.$utils.htmlToText(title)});
        } else if (this.activityTypeExtension.getTitle) {
          title = this.activityTypeExtension.getTitle(this.activity) || '';
          if (title && title.key) {
            title = this.$t(title.key, title.params || {});
          }
          title = this.$t(titleKey, {0: this.$utils.htmlToText(title)});
        } else {
          title = this.activity.title || '';
          title = this.$t(titleKey, {0: this.$utils.htmlToText(title)});
        }
        if (title) {
          if (title.length > 100) {
            document.title = `${title.substring(0, 100)}...`;
          } else {
            document.title = title;
          }
        }
      }
    },
    abortSpecificExtension(activityId) {
      if (activityId === this.activityId) {
        this.noExtension = true;
      }
    },
    markAsRead() {
      this.unreadMetadata = null;
      this.$root.$emit('activity-read', this.activityId);
    },
    scrollTo(element) {
      window.setTimeout(() => {
        if (element && element.scrollIntoView) {
          element.scrollIntoView({
            behavior: 'smooth',
            block: 'start',
          });
        }
      }, 10);
    },
  },
};
</script>
