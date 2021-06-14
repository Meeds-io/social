<template>
  <div class="actionBar clearfix">
    <extension-registry-components
      name="ActivityFooter"
      type="activity-footer-action"
      parent-element="ul"
      element="li"
      element-class="ma-0"
      class="pull-right statusAction"
      :params="params" />
  </div>
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