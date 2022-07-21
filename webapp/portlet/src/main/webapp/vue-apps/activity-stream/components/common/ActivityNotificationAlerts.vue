<template>
  <v-snackbar
    v-model="snackbar"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    color="transparent"
    elevation="0"
    app>
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
    this.$root.$on('activity-shared', (activityId, spaces) => {
      if (spaces && spaces.length > 0) {
        const spaceDisplayNames = spaces.map(space => space.displayName || '');
        const message = `${this.$t('UIActivity.share.message')} ${spaceDisplayNames.join(', ')}`;
        this.alert = {
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