<template>
  <extension-registry-components
    name="ActivityCommentFooter"
    type="activity-comment-footer-action"
    element-class="ma-0"
    :params="params" />
</template>

<script>
export default {
  props: {
    comment: {
      type: Object,
      default: null,
    },
  },
  computed: {
    params() {
      return {
        comment: this.comment,
      };
    },
  },
};
</script>