<template>
  <div>
    <activity-share-information
      v-if="displayShareInformation"
      :activity="activity"
      :activity-types="activityTypes"
      class="actionsDetailsWrapper no-border-bottom mb-0 py-3" />
    <div class="actionsDetailsWrapper mb-0 py-0 px-4 no-border-bottom">
      <activity-reactions
        :activity-id="activityId"
        :activity="activity"
        :likers="likers"
        :likers-number="likersCount"
        :comment-number="commentsCount"
        class="activityReactionsContainer" />
      <activity-actions
        :activity="activity"
        :activity-type-extension="activityTypeExtension" />
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
    kudosCount: 0,
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
  },
  created() {
    this.$root.$on('activity-comment-created', this.checkCommentsSize);
    this.$root.$on('activity-comment-updated', this.checkCommentsSize);
    this.$root.$on('activity-comment-deleted', this.checkCommentsSize);
    this.$root.$on('activity-comments-retrieved', this.checkCommentsSize);
    this.$root.$on('activity-liked', this.checkLikesSize);
    this.checkCommentsSize();
    this.checkLikesSize();
  },
  beforeDestroy() {
    this.$root.$off('activity-comment-created', this.checkCommentsSize);
    this.$root.$off('activity-comment-updated', this.checkCommentsSize);
    this.$root.$off('activity-comment-deleted', this.checkCommentsSize);
    this.$root.$off('activity-comments-retrieved', this.checkCommentsSize);
    this.$root.$off('activity-liked', this.checkLikesSize);
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