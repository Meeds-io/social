<template>
  <div>
    <activity-share-information
      v-if="displayShareInformation"
      :activity="activity"
      :activity-types="activityTypes"
      class="no-border-bottom mb-0 pa-3" />
    <div
      :class="actionBarBorderClass"
      class="mb-0 d-flex flex-wrap flex-column flex-lg-row align-lg-center">
      <activity-reactions
        :activity-id="activityId"
        :activity="activity"
        :likers="likers"
        :likers-number="likersCount"
        :comment-number="commentsCount"
        class="flex-grow-1 ps-4" />
      <activity-actions
        :activity="activity"
        :activity-type-extension="activityTypeExtension"
        :is-activity-detail="isActivityDetail"
        class="px-4" />
    </div>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    isActivityDetail: {
      type: Boolean,
      default: false,
    },
    activityTypes: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    likersCount: 0,
    commentsCount: 0,
    likers: [],
  }),
  computed: {
    displayShareInformation() {
      return this.activity
        && this.activity.originalActivity
        && this.activityTypeExtension
        && this.activityTypeExtension.showSharedInformationFooter
        && this.activityTypeExtension.showSharedInformationFooter(this.activity, this.isActivityDetail);
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    actionBarBorderClass() {
      return this.isDesktop && 'border-top-color border-light-color' || '';
    },
    isDesktop() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name !== 'xs' && this.$vuetify.breakpoint.name !== 'sm' && this.$vuetify.breakpoint.name !== 'md';
    },
  },
  created() {
    this.$root.$on('activity-comment-created', this.checkCommentsSize);
    this.$root.$on('activity-comment-updated', this.checkCommentsSize);
    this.$root.$on('activity-comment-deleted', this.checkCommentsSize);
    this.$root.$on('activity-comments-retrieved', this.checkCommentsSize);
    document.addEventListener('activity-liked', event => {
      if (event && event.detail && event.detail === this.activityId) {
        this.checkLikesSize();
      }
    });
    this.checkCommentsSize();
    this.checkLikesSize();
  },
  beforeDestroy() {
    this.$root.$off('activity-comment-created', this.checkCommentsSize);
    this.$root.$off('activity-comment-updated', this.checkCommentsSize);
    this.$root.$off('activity-comment-deleted', this.checkCommentsSize);
    this.$root.$off('activity-comments-retrieved', this.checkCommentsSize);
    document.removeEventListener(`activity-liked-${this.activityId}`,this.checkLikesSize);
  },
  methods: {
    checkLikesSize() {
      this.likers = this.activity.likes || [];
      this.likersCount = this.activity.likesCount && Number(this.activity.likesCount);
    },
    checkCommentsSize() {
      this.commentsCount = this.activity.commentsSize;
    },
  },
};
</script>
