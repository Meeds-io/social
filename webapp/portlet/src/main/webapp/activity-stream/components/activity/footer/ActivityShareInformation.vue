<template>
  <div class="activity-share-information">
    <div class="activity-share-stream">
      <div class="newsOwner">
        <exo-user-avatar
          :username="sharedPoster.username"
          :title="sharedPoster.fullname"
          :avatar-url="sharedPoster.avatar"
          :size="30"
          class="activity-share-user" />
        <div class="createIn mx-2">
          <span class="uiIconArrowRightMini uiIconLightGray"></span>
        </div>
        <exo-space-avatar
          :space="space"
          :size="32"
          avatar-class="border-color"
          class="activity-share-space d-inline-block my-auto" />
      </div>
    </div>
    <div
      v-if="sharedTitle"
      v-sanitized-html="sharedTitle"
      class="activity-share-message"></div>
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
  computed: {
    sharedTitle() {
      return this.activity && this.activity.title;
    },
    sharedPoster() {
      return this.activity && this.activity.identity && this.activity.identity.profile;
    },
    originalSharedActivity() {
      return this.activity && this.activity.originalSharedActivity;
    },
    originalStreamSpace() {
      return this.originalSharedActivity && this.originalSharedActivity.activityStream && this.originalSharedActivity.activityStream.space;
    },
  },
};
</script>