<template>
  <v-btn
    :id="`CommentLink${activityId}`"
    :title="$t('UIActivity.label.Comment')"
    :data-activity="activityId"
    class="pa-0 mx-2 primary--text"
    text
    link
    small
    @click="openCommentsDrawer">
    <span>
      <v-icon
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
  computed: {
    activityId() {
      return this.activity && this.activity.id;
    },
  },
  methods: {
    openCommentsDrawer() {
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activityId: this.activityId,
        offset: 0,
        limit: 200, // To display all
      }}));
    },
  },
};
</script>