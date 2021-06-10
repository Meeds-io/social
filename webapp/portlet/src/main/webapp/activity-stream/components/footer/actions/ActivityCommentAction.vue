<template>
  <v-btn
    :id="`CommentLink${activityId}`"
    :title="$t('UIActivity.label.Comment')"
    :data-activity="activityId"
    class="pa-0 primary--text"
    text
    link
    small>
    <v-icon
      class="me-1 pb-1"
      size="12">
      fa-comment
    </v-icon>
    <span>{{ $t('UIActivity.label.Comment') }}</span>
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
};
</script>