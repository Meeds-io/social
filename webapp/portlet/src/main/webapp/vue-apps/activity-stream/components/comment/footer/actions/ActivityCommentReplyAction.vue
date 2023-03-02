<template>
  <div class="d-inline-flex pe-1">
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :id="`CommentLink${activityId}`"
          :class="commentTextColorClass"
          class="pa-0 me-0"
          text
          link
          x-small
          v-bind="attrs"
          v-on="on"
          @click="openCommentsDrawer">
          <span>
            {{ $t('UIActivity.label.Reply') }}
          </span>
        </v-btn>
      </template>
      <span>
        {{ $t('UIActivity.label.Comment') }}
      </span>
    </v-tooltip>
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          v-show="subCommentsSize"
          :id="`RepliesListLink${commentId}`"
          :title="$t('UIActivity.label.ViewAllReplies', {0: subCommentsSize})"
          class="primary--text font-weight-bold"
          x-small
          icon
          v-bind="attrs"
          v-on="on"
          @click="openReplies">
          ({{ subCommentsSize }})
        </v-btn>
      </template>
      <span>
        {{ $t('UIActivity.label.ViewAllReplies', {0: subCommentsSize}) }}
      </span>
    </v-tooltip>
  </div>
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
  },
  data: () => ({
    hasCommented: false,
  }),
  computed: {
    activityId() {
      return this.comment && this.comment.activityId;
    },
    commentId() {
      return this.comment && (this.comment.parentCommentId || this.comment.id) || '';
    },
    commentTextColorClass() {
      return this.hasCommented && 'primary--text' || '';
    },
    subCommentsSize() {
      return this.comment && this.comment.subCommentsSize || 0;
    },
  },
  watch: {
    comment() {
      this.checkWhetherCommented();
    },
  },
  created() {
    this.$root.$on('activity-comment-created', this.handleCommentCreated);
    this.$root.$on('activity-comment-deleted', this.handleCommentDeleted);
    this.checkWhetherCommented();
  },
  beforeDestroy() {
    this.$root.$off('activity-comment-created', this.handleCommentCreated);
    this.$root.$off('activity-comment-deleted', this.handleCommentDeleted);
  },
  methods: {
    handleCommentCreated(comment) {
      if (comment.activityId === this.activityId && this.comment.id === comment.parentCommentId) {
        this.hasCommented = true;
      }
    },
    handleCommentDeleted(event) {
      const activityId = event?.activityId;
      const parentCommentId = event?.parentCommentId;
      if (activityId === this.activityId && this.comment.id === parentCommentId) {
        this.checkWhetherCommented();
      }
    },
    checkWhetherCommented() {
      this.hasCommented = this.comment && this.comment.hasCommented === 'true';
    },
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        commentId: this.commentId,
        newComment: true,
      }}));
    },
    openReplies() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        commentId: this.commentId,
        highlightRepliesCommentId: this.commentId,
      }}));
    },
  },
};
</script>