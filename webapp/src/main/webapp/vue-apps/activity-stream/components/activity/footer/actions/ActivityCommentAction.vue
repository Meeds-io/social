<template>
  <div class="d-inline-flex ms-xl-4 ms-lg-3">
    <!-- Added for mobile -->
    <v-tooltip
      bottom
      :disabled="isMobile">
      <template #activator="{ on, attrs }">
        <v-btn
          :id="`CommentLink${activityId}`"
          class="pa-0 mt-0"
          :class="commentTextColorClass"
          link
          small
          text
          v-bind="attrs"
          v-on="on"
          @click="openCommentsDrawer">
          <div class="d-flex flex-lg-row flex-column">
            <v-icon
              class="baseline-vertical-align mx-auto"
              :class="commentColorClass"
              :size="isMobile && '20' || '16'">
              fa-comment
            </v-icon>
            <span
              v-if="!isMobile"
              class="mx-auto mt-1 mt-lg-0 ms-lg-1 text-body">
              {{ $t('UIActivity.label.Comment') }}
            </span>
          </div>
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
      isMobile: {
        type: Boolean,
        default: () => false,
      },
    },
    data: () => ({
      hasCommented: false,
    }),
    computed: {
      activityId () {
        return this.activity && this.activity.id;
      },
      commentColorClass () {
        return this.hasCommented && 'primary--text' || 'disabled--text';
      },
      commentTextColorClass () {
        return this.hasCommented && 'primary--text' || '';
      },
    },
    watch: {
      activity () {
        this.checkWhetherCommented();
      },
    },
    created () {
      this.$root.$on('activity-comment-created', this.handleCommentCreated);
      this.$root.$on('activity-comment-deleted', this.handleCommentDeleted);
      this.checkWhetherCommented();
    },
    beforeUnmount () {
      this.$root.$off('activity-comment-created', this.handleCommentCreated);
      this.$root.$off('activity-comment-deleted', this.handleCommentDeleted);
    },
    methods: {
      handleCommentCreated (comment) {
        if (comment.activityId === this.activityId) {
          this.hasCommented = true;
        }
      },
      handleCommentDeleted (event) {
        const activityId = event?.activityId;
        if (activityId === this.activityId) {
          this.checkWhetherCommented();
        }
      },
      checkWhetherCommented () {
        this.hasCommented = this.activity && this.activity.hasCommented === 'true';
      },
      openCommentsDrawer () {
        document.dispatchEvent(new CustomEvent('activity-comments-display', { detail: {
          activity: this.activity,
          newComment: true,
        } }));
      },
    },
  };
</script>
