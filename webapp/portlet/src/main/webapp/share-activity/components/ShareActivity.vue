<template>
  <v-app :id="id" class="shareActivity">
    <div class="shareActivityButton" @click="openDrawer">
      <i class="uiIconShare uiIconLightBlue uiIcon16x16"></i>
      <span class="share-text">{{ $t('UIActivity.share.share') }}</span>
    </div>
    <share-activity-drawer
      ref="activityDrawer"
      class="activityDrawer"
      :activity-id="activityId"
      :activity-type="activityType" />
    <share-activity-notification-alerts />
  </v-app>
</template>

<script>
export default {
  props: {
    activityId: {
      type: String,
      default: ''
    },
    activityType: {
      type: String,
      default: ''
    },
    id: {
      type: String,
      default: ''
    }
  },
  methods: {
    openDrawer() {
      if (this.$refs.activityDrawer) {
        this.spaces = [];
        this.$refs.activityDrawer.open();
      }
    },
  }
  
};
</script>
