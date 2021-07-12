<template>
  <div class="activity-share-information d-flex flex-column align-start">
    <div class="activity-share-stream d-flex flex-row">
      <exo-user-avatar
        :username="sharedPoster.username"
        :title="sharedPoster.fullname"
        :fullname="sharedPoster.fullname"
        :avatar-url="sharedPoster.avatar"
        :size="25"
        class="activity-share-user"
        bold-title
        link-style />
      <div class="createIn me-2 my-auto">
        <span class="uiIconArrowRightMini uiIconLightGray"></span>
      </div>
      <exo-space-avatar
        :space="space"
        :size="30"
        avatar-class="border-color"
        class="activity-share-space d-inline-block my-auto"
        bold-title
        link-style />
    </div>
    <div
      v-if="shareMessage"
      v-sanitized-html="shareMessage"
      class="activity-share-message mt-1"></div>
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
    shareMessage() {
      return this.activity && this.activity.title;
    },
    sharedPoster() {
      return this.activity && this.activity.identity && this.activity.identity.profile;
    },
    space() {
      return this.activity && this.activity.activityStream && this.activity.activityStream.space;
    },
  },
};
</script>