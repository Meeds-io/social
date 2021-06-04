<template>
  <div class="actionBar clearfix ">
    <ul class="pull-right statusAction">
      <li v-if="hasLikes">
        <a
          :id="`UnLikeLink${activityId}`"
          :title="$t('UIActivity.msg.UnlikeActivity')"
          @click="unlikeActivity">
          <i class="uiIconThumbUp uiIconBlue"></i>
          <span class="LikeLabel reactionLabel uiIconBlue">
            {{ $t('UIActivity.msg.UnlikeActivity') }}
          </span>
        </a>
      </li>
      <li v-else>
        <a
          :id="`LikeLink${activityId}`"
          :title="$t('UIActivity.msg.LikeActivity')"
          @click="likeActivity">
          <i class="uiIconThumbUp uiIconLightGray"></i>
          <span class="LikeLabel reactionLabel">
            {{ $t('UIActivity.msg.LikeActivity') }}
          </span>
        </a>
      </li>
      <li>
        <a
          :id="`CommentLink${activityId}`"
          class="$commentLink"
          data-activity="${activity.id}">
          <i class="uiIconComment uiIconLightGray"></i>
          <span class="CommentLabel reactionLabel">
            {{ $t('UIActivity.label.Comment') }}
          </span>
        </a>
      </li>
      <li v-if="isShareable" class="share-link">
        <a
          :id="`ShareActivity${activityId}`"
          :title="$t('UIActivity.msg.LikeActivity')"
          @click="openShareDrawer()">
          <i class="uiIconShare uiIcon16x16"></i>
          <span class="share-text">
            {{ $t('UIActivity.share.share') }}
          </span>
        </a>
      </li>
      <extension-registry-components name="activity-actions" :params="params" />
    </ul>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
  },
  computed: {
    params() {
      return null;
    },
    isShareable() {
      return this.activity && this.activityTypeExtension && this.activityTypeExtension.canShare && this.activityTypeExtension.canShare(this.activity);
    },
    hasLikes() {
      return this.activity && this.activity.likesCount > 0;
    },
    activityId() {
      return this.activity && this.activity.id;
    },
    activityType() {
      return this.activity && this.activity.type;
    },
  },
  methods: {
    likeActivity() {
      this.$activityService.likeActivity(this.activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(activity => this.$root.$emit('activity-stream-activity-updated', this.activityId, activity));
    },
    unlikeActivity() {
      this.$activityService.unlikeActivity(this.activityId, this.$activityConstants.FULL_ACTIVITY_EXPAND)
        .then(activity => this.$root.$emit('activity-stream-activity-updated', this.activityId, activity));
    },
    openShareDrawer() {
      this.$root.$on('open-share-activity-drawer', {
        activityId: this.activityId,
        activityType: this.activityType,
      });
    },
  },
};
</script>