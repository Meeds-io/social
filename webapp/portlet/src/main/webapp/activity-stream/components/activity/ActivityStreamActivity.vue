<template>
  <div :id="id" class="white border-radius activity-detail">
    <template v-if="extendedComponent">
      <activity-head
        v-if="!extendedComponent.overrideHeader"
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension" />
      <extension-registry-component
        :component="extendedComponentOptions"
        :element="extendedComponent.element"
        :element-class="extendedComponent.class"
        :params="extendedComponentParams" />
      <activity-footer
        v-if="!extendedComponent.overrideFooter"
        :activity="activity"
        :activity-type-extension="activityTypeExtension" />
      <activity-comments-preview
        v-if="!extendedComponent.overrideComments"
        :activity-id="activityId"
        :comment-actions="commentActions" />
    </template>
    <template v-else>
      <activity-head
        :activity="activity"
        :activity-actions="activityActions"
        :activity-type-extension="activityTypeExtension" />
      <v-card :loading="loading" flat>
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
        :activity-id="activityId"
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
      };
    },
    init() {
      return this.activityTypeExtension && this.activityTypeExtension.init;
    },
  },
  watch: {
    activity() {
      this.retrieveActivityProperties();
    },
    activityTypeExtension() {
      this.retrieveActivityProperties();
    },
  },
  created() {
    this.$root.$on('activity-stream-updating-activity-start', () => this.loading = true);
    this.$root.$emit('activity-stream-updating-activity-end', () => this.loading = false);

    document.addEventListener('activity-stream-activity-updated', event => {
      const activityId = event && event.detail;
      if (activityId === this.activityId) {
        this.retrieveActivityProperties();
      }
    });

    this.retrieveActivityProperties();
  },
  methods: {
    retrieveActivityProperties() {
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