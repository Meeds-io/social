<template>
  <div class="caption text-light-color text-truncate">
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :height="20"
          :href="commentLink"
          class="d-flex hover-underline width-auto text-capitalize-first-letter d-inline px-0"
          x-small
          link
          text
          plain
          @click="openCommentsDrawer"
          v-bind="attrs"
          v-on="on">
          <relative-date-format
            v-if="isActivityEdited"
            :value="comment.updateDate"
            label="TimeConvert.label.Short.Edited"
            class="text-capitalize-first-letter text-light-color text-truncate ps-1"
            short />
          <relative-date-format
            v-else
            :value="comment.createDate"
            class="text-capitalize-first-letter text-light-color text-truncate ps-1"
            short />
        </v-btn>
      </template>
      <date-format :value="activityPostedTime" :format="dateFormat" />
    </v-tooltip>
  </div>
</template>

<script>
export default {
  props: {
    comment: {
      type: Object,
      default: null,
    },
    activity: {
      type: Object,
      default: null,
    },
    noIcon: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    dateFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: 'numeric',
      minute: 'numeric',
    },
  }),
  computed: {
    isActivityEdited() {
      return this.comment && this.comment.updateDate !== this.comment.createDate;
    },
    commentId() {
      return this.comment && this.comment.id;
    },
    activityPostedTime() {
      return this.comment && (this.comment.updateDate || this.comment.createDate);
    },
    commentLink() {
      return `${this.$root.activityBaseLink}?id=${this.commentId}`;
    },
  },
  methods: {
    openCommentsDrawer(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      document.dispatchEvent(new CustomEvent('activity-comments-display', {detail: {
        activity: this.activity,
        selectedCommentId: this.commentId,
        selectedActivityId: this.activity.id,
        offset: 0,
        limit: 200, // To display all
      }}));
    },
  }
};
</script>