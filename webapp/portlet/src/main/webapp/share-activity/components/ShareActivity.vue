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
      :activity-type="activityType"
      @share-activity="shareActivity" />
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
      this.$refs.activityDrawer.open();
    },
    shareActivity(spaces, description) {
      const spacesList = [];
      spaces.forEach(space => {
        this.$spaceService.getSpaceByPrettyName(space,'identity').then(data => {
          spacesList.push(data.displayName);
        });
      });
      const sharedActivity = {
        title: description,
        type: this.activityType,
        targetSpaces: spaces,
      };
      this.$spaceService.shareActivityOnSpaces(this.activityId, sharedActivity).then(() =>
      {
        this.$refs.activityDrawer.close();
      }).then(() => {
        this.$root.$emit('activity-shared', spacesList);
      });
    }
  }

};
</script>
