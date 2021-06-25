<template>
  <extension-registry-components
    name="ActivityCommentFooter"
    type="activity-comment-footer-action"
    element-class="ma-0 d-flex"
    :params="params" />
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
      default: null,
    },
    commentTypeExtension: {
      type: Object,
      default: null,
    },
  },
  computed: {
    params() {
      return {
        activity: this.activity,
        comment: this.comment,
        commentTypeExtension: this.commentTypeExtension,
      };
    },
  },
};
</script>