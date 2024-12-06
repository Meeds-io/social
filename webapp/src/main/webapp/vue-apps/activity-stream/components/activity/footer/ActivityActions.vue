<template>
  <extension-registry-components
    :params="params"
    :class="actionBarBorderClass"
    class="d-flex flex-no-wrap ms-lg-auto py-2 activity-footer-actions"
    name="ActivityFooter"
    type="activity-footer-action"
    parent-element="div"
    element="div"
    element-class="mx-auto ma-lg-0 activity-footer-action" />
</template>
<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    params() {
      return {
        activity: this.activity,
        activityTypeExtension: this.activityTypeExtension,
        isActivityDetail: this.isActivityDetail,
        isMobile: !this.isDesktop
      };
    },
    actionBarBorderClass() {
      return !this.isDesktop && 'border-top-color border-light-color' || '';
    },
    isDesktop() {
      return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
    },
  },
};
</script>