<template>
  <v-app :id="id" class="shareActivity">
    <share-activity-drawer
      ref="activityDrawer"
      class="activityDrawer"
      @share-activity="shareActivity" />
    <share-activity-notification-alerts />
  </v-app>
</template>

<script>
export default {
  props: {
    id: {
      type: String,
      default: ''
    }
  },
  data: () => ({
    activityId: '',
    activityType: '',
  }),
  created() {
    this.$root.$on('open-share-activity-drawer', (params) => {
      this.activityId = params.activityId;
      this.activityType = params.activityType;
      this.openDrawer();
    });
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
        this.$root.$emit('clear-suggester', spacesList);
      });
    }
  }

};
</script>
