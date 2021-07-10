<template>
  <div
    :id="id"
    class="white border-radius activity-detail flex d-flex flex-column">
    <template v-if="extendedComponent">
      <activity-head
        v-if="!extendedComponent.overrideHeader"
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension"
        :hide-menu="hideMenu" />
      <template v-if="!loading">
        <extension-registry-component
          :component="extendedComponentOptions"
          :element="extendedComponent.element"
          :element-class="extendedComponent.class"
          :params="extendedComponentParams"
          class=" d-flex flex-column" />
      </template>
      <template v-if="!hideFooter">
        <activity-footer
          v-if="!extendedComponent.overrideFooter"
          :activity="activity"
          :is-activity-detail="isActivityDetail"
          :activity-type-extension="activityTypeExtension" />
        <activity-comments-preview
          v-if="!extendedComponent.overrideComments"
          :activity="activity"
          :comment-types="commentTypes"
          :comment-actions="commentActions" />
      </template>
    </template>
    <template v-else>
      <activity-head
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension"
        :hide-menu="hideMenu" />
      <v-card v-if="!loading" flat>
        <extension-registry-components
          v-if="initialized"
          :params="extendedComponentParams"
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
          :activity-type-extension="activityTypeExtension" />
        <activity-comments-preview
          :activity="activity"
          :comment-types="commentTypes"
          :comment-actions="commentActions" />
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
  }),
  computed: {
    id() {
      return `activity-detail-${this.activityId}`;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityTypeExtension() {
      if (!this.activity || !this.activityTypes) {
        return {};
      }
      return this.activityTypes[this.activity.type] || this.activityTypes['default'] || {};
    },
    extendedComponent() {
      return this.activityTypeExtension && this.activityTypeExtension.getExtendedComponent && this.activityTypeExtension.getExtendedComponent(this.activity, this.isActivityDetail);
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
        originalSharedActivity: this.originalSharedActivity,
        isActivityDetail: this.isActivityDetail,
        activityTypeExtension: this.activityTypeExtension,
        activityTypes: this.activityTypes,
        loading: this.loading,
      };
    },
    init() {
      return this.activityTypeExtension && this.activityTypeExtension.init;
    },
    sharedActivityId() {
      return this.activity && this.activity.templateParams && this.activity.templateParams.originalActivityId;
    },
  },
  watch: {
    loading() {
      if (!this.loading) {
        this.refreshTipTip();
      }
    },
  },
  created() {
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
              });
          }
        }
        this.loading = false;
        this.initialized = true;
      });
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