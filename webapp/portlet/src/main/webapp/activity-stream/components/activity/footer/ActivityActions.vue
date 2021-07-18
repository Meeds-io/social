<template>
  <extension-registry-components
    name="ActivityFooter"
    type="activity-footer-action"
    parent-element="div"
    class="d-flex flex-wrap activity-footer-actions"
    element="div"
    element-class="mx-auto ma-lg-0 activity-footer-action"
    :params="params" />
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
  },
  computed: {
    params() {
      return {
        activity: this.activity,
        activityTypeExtension: this.activityTypeExtension,
      };
    },
  },
};
</script>