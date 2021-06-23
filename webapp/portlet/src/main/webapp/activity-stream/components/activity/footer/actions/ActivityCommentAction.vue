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
    document.addEventListener('activity-commented', (event) => {
      const activityId = event && event.detail && event.detail.activityId;
      if (activityId === this.activityId) {
        this.hasCommented = true;
      }
    });
    this.hasCommented = this.activity && this.activity.hasCommented === 'true';
  },
  methods: {
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activityId: this.activityId,
        offset: 0,
        limit: 200, // To display all
        displayComment: true,
      }}));
    },
  },
};
</script>