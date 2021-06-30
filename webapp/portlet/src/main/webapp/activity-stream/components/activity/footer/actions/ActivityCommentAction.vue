<template>
  <v-btn
    :id="`CommentLink${activityId}`"
    :title="$t('UIActivity.label.Comment')"
    :class="commentTextColorClass"
    class="pa-0 mx-2"
    text
    link
    small
    @click="openCommentsDrawer">
    <span>
      <v-icon
        :class="commentColorClass"
        class="baseline-vertical-align"
        size="14">
        fa-comment
      </v-icon>
      {{ $t('UIActivity.label.Comment') }}
    </span>
  </v-btn>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    hasCommented: false,
  }),
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
    commentColorClass() {
      return this.hasCommented && 'primary--text' || 'disabled--text';
    },
    commentTextColorClass() {
      return this.hasCommented && 'primary--text' || '';
    },
  },
  created() {
    this.$root.$on('activity-comment-created', this.handleCommentCreated);
    this.hasCommented = this.activity && this.activity.hasCommented === 'true';
  },
  beforeDestroy() {
    this.$root.$off('activity-comment-created', this.handleCommentCreated);
  },
  methods: {
    handleCommentCreated(comment) {
      if (comment.activityId === this.activityId) {
        this.hasCommented = true;
      }
    },
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        newComment: true,
        offset: 0,
        limit: 200, // To display all
      }}));
    },
  },
};
</script>