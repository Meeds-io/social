<template>
  <v-snackbar
    v-model="snackbar"
    color="transparent"
    elevation="0"
    app
    left>
    <exo-notification-alert
      :alert="alert"
      @dismissed="clear" />
  </v-snackbar>
</template>
<script>
export default {
  data: () => ({
    snackbar: false,
    alert: null,
  }),
  watch: {
    alert() {
      this.snackbar = !!this.alert;
    },
  },
  created() {
    this.$root.$on('activity-notification-alert', alert => this.alert = alert);
    this.$root.$on('activity-shared', (activityId, spaces) => {
      if (spaces && spaces.length > 0) {
        const spaceDisplayNames = spaces.map(space => space.displayName || '');
        const message = `${this.$t('UIActivity.share.message')} ${spaceDisplayNames.join(', ')}`;
        this.alert = {
          activityId,
          message,
          type: 'success',
        };
      }
    });
  },
  methods: {
    clear() {
      this.alert = null;
    },
  },
};
</script>