<template>
  <div class="d-inline-flex ms-md-4">
    <!-- Added for mobile -->
    <v-tooltip bottom>
      <template v-slot:activator="{ on, attrs }">
        <v-btn
          :id="`CommentLink${activityId}`"
          :class="commentTextColorClass"
          class="pa-0 mt-0"
          text
          link
          small
          v-bind="attrs"
          v-on="on"
          @click="openCommentsDrawer">
          <v-icon
            :class="commentColorClass"
            class="baseline-vertical-align"
            size="14">
            fa-comment
          </v-icon>
          <span class="hidden-xs-only ms-2">
            {{ $t('UIActivity.label.Comment') }}
          </span>
        </v-btn>
      </template>
      <span>
        {{ $t('UIActivity.label.Comment') }}
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
  watch: {
    activity() {
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
      if (comment.activityId === this.activityId) {
        this.hasCommented = true;
      }
    },
    handleCommentDeleted(comment) {
      if (comment.activityId === this.activityId) {
        this.checkWhetherCommented();
      }
    },
    checkWhetherCommented() {
      this.hasCommented = this.activity && this.activity.hasCommented === 'true';
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