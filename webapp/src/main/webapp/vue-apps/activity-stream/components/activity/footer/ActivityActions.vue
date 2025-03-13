<template>
  <extension-registry-components
    class="d-flex flex-no-wrap ms-lg-auto py-2 activity-footer-actions"
    :class="actionBarBorderClass"
    element="div"
    element-class="mx-auto ma-lg-0 activity-footer-action"
    name="ActivityFooter"
    :params="params"
    parent-element="div"
    type="activity-footer-action" />
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
      params () {
        return {
          activity: this.activity,
          activityTypeExtension: this.activityTypeExtension,
          isActivityDetail: this.isActivityDetail,
          isMobile: !this.isDesktop,
        };
      },
      actionBarBorderClass () {
        return !this.isDesktop && 'border-top-color border-light-color' || '';
      },
      isDesktop () {
        return eXo.vuetify.display.width.value >= eXo.vuetify.display.thresholds.value.lg;
      },
    },
  };
</script>