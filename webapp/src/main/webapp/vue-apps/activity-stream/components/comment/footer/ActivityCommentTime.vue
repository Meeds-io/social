<template>
  <div class="text-subtitle text-truncate">
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          class="d-flex hover-underline width-auto text-capitalize-first-letter d-inline px-0"
          :height="20"
          :href="commentLink"
          link
          plain
          text
          x-small
          v-bind="attrs"
          v-on="on"
          @click="openCommentsDrawer">
          <relative-date-format
            v-if="isActivityEdited"
            class="text-capitalize-first-letter text-subtitle text-truncate ps-1"
            label="TimeConvert.label.Short.Edited"
            short
            :value="comment.updateDate" />
          <relative-date-format
            v-else
            class="text-capitalize-first-letter text-subtitle text-truncate ps-1"
            short
            :value="comment.createDate" />
        </v-btn>
      </template>
      <date-format
        :format="dateFormat"
        :value="activityPostedTime" />
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
      isActivityEdited () {
        return this.comment && this.comment.updateDate !== this.comment.createDate;
      },
      commentId () {
        return this.comment && this.comment.id;
      },
      activityPostedTime () {
        return this.comment && (this.comment.updateDate || this.comment.createDate);
      },
      commentLink () {
        return `${this.$root.activityBaseLink}?id=${this.commentId}`;
      },
    },
    methods: {
      openCommentsDrawer (event) {
        if (event) {
          event.preventDefault();
          event.stopPropagation();
        }
        document.dispatchEvent(new CustomEvent('activity-comments-display', { detail: {
          activity: this.activity,
          selectedCommentId: this.commentId,
          selectedActivityId: this.activity.id,
          offset: 0,
          limit: 200, // To display all
        } }));
      },
    },
  };
</script>