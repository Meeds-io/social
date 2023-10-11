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
      };
    },
    actionBarBorderClass() {
      return !this.isDesktop && 'border-top-color border-light-color' || '';
    },
    isDesktop() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name !== 'xs' && this.$vuetify.breakpoint.name !== 'sm' && this.$vuetify.breakpoint.name !== 'md';
    },
  },
};
</script>