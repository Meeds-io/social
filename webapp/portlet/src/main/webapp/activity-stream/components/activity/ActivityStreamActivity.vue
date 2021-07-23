<template>
  <div
    :id="id"
    class="white border-radius activity-detail flex d-flex flex-column">
    <template v-if="!noExtension && extendedComponent">
      <activity-head
        v-if="!extendedComponent.overrideHeader"
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension"
        :hide-menu="hideMenu"
        :class="isActivityShared && 'py-4 px-0' || 'py-4 ps-4 pe-1'" />
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
        :activity-type-extension="activityTypeExtension"
        :hide-menu="hideMenu"
        :class="isActivityShared && 'py-4 px-0' || 'py-4 ps-4 pe-1'" />
      <v-card v-if="!loading" flat>
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
  </div>
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
      return this.activityTypes[this.activity.type] || this.activityTypes['default'] || {};
    },
    sharedActivity() {
      return this.activity && this.activity.originalActivity;
    },
    sharedActivityTypeExtension() {
      if (this.noExtension || !this.sharedActivity || !this.activityTypes) {
        return null;
      }
      return this.activityTypes[this.sharedActivity.type] || this.activityTypes['default'] || null;
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
  },
  watch: {
    loading() {
      if (!this.loading) {
        this.refreshTipTip();
        this.setWindowTitle();
      }
    },
  },
  created() {
    this.$root.$on('activity-extension-abort', this.abortSpecificExtension);
    this.$root.$on('activity-refresh-ui', this.retrieveActivityProperties);
    this.retrieveActivityProperties();
  },
  mounted() {
    if (this.$root.selectedCommentId && this.activity && this.activity.id === this.$root.selectedActivityId) {
      window.setTimeout(() => {
        document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
          activity: this.activity,
          offset: 0,
          limit: 200, // To display all
          noAuitomaticScroll: true,
        }}));
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
      if (activityId && activityId !== this.activityId) {
        return;
      }
      this.loading = true;
      this.$nextTick(() => {
        if (this.init) {
          const initPromise = this.init(this.activity, this.isActivityDetail);
          if (initPromise && initPromise.then) {
            return initPromise
              .finally(() => {
                this.loading = false;
                this.initialized = true;
                this.setWindowTitle();
              });
          }
        }
        this.loading = false;
        this.initialized = true;
      });
    },
    setWindowTitle() {
      if (this.isActivityDetail && this.activityTypeExtension) {
        if (this.activityTypeExtension.getWindowTitle) {
          const title = this.activityTypeExtension.getWindowTitle(this.activity) || '';
          document.title = this.$t('activity.window.title', {0: this.$utils.htmlToText(title)});
        } else if (this.activityTypeExtension.getTitle) {
          const title = this.activityTypeExtension.getTitle(this.activity) || '';
          document.title = this.$t('activity.window.title', {0: this.$utils.htmlToText(title)});
        } else {
          const title = this.activity.title || '';
          document.title = this.$t('activity.window.title', {0: this.$utils.htmlToText(title)});
        }
      }
    },
    abortSpecificExtension(activityId) {
      if (activityId === this.activityId) {
        this.noExtension = true;
      }
    },
    refreshTipTip() {
      window.setTimeout(() => {
        this.$utils.initTipTip(this.$el, this.$userPopupLabels);
      }, 200);
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