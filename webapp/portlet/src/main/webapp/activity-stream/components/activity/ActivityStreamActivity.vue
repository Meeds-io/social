<template>
  <div
    :id="id"
    class="white border-radius activity-detail">
    <template v-if="extendedComponent">
      <activity-head
        v-if="!extendedComponent.overrideHeader"
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension" />
      <template v-if="!loading">
        <extension-registry-component
          :component="extendedComponentOptions"
          :element="extendedComponent.element"
          :element-class="extendedComponent.class"
          :params="extendedComponentParams" />
      </template>
      <activity-footer
        v-if="!extendedComponent.overrideFooter"
        :activity="activity"
        :activity-type-extension="activityTypeExtension" />
      <activity-comments-preview
        v-if="!extendedComponent.overrideComments"
        :activity="activity"
        :comment-types="commentTypes"
        :comment-actions="commentActions" />
    </template>
    <template v-else>
      <activity-head
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension" />
      <v-card v-if="!loading" flat>
        <extension-registry-components
          v-if="initialized"
          name="ActivityContent"
          type="activity-content-extensions"
          parent-element="div"
          element="div"
          :params="extendedComponentParams" />
      </v-card>
      <activity-footer
        :activity="activity"
        :activity-type-extension="activityTypeExtension" />
      <activity-comments-preview
        :activity="activity"
        :comment-types="commentTypes"
        :comment-actions="commentActions" />
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
        isActivityDetail: this.isActivityDetail,
        activityTypeExtension: this.activityTypeExtension,
        loading: this.loading,
      };
    },
    init() {
      return this.activityTypeExtension && this.activityTypeExtension.init;
    },
  },
  created() {
    this.$root.$on('activity-refresh-ui', this.retrieveActivityProperties);
    this.retrieveActivityProperties();
  },
  beforeDestroy() {
    this.$root.$off('activity-refresh-ui', this.retrieveActivityProperties);
  },
  methods: {
    retrieveActivityProperties(activityId) {
      if (activityId && activityId !== this.activityId) {
        return;
      }
      if (this.init) {
        const initPromise = this.init(this.activity, this.isActivityDetail);
        if (initPromise && initPromise.then) {
          this.loading = true;
          return initPromise
            .finally(() => {
              this.loading = false;
              this.initialized = true;
            });
        }
      }
      this.initialized = true;
    },
  },
};
</script>