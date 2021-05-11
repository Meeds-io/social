<template>
  <v-app>
    <v-snackbar
      :value="displayAlerts"
      color="transparent"
      elevation="0"
      app
      left>
      <exo-notification-alert
        v-for="alert in alerts"
        :key="alert.message"
        :alert="alert"
        @dismissed="deleteAlert(alert)" />
    </v-snackbar>
  </v-app>
</template>

<script>
export default {
  props: {
    name: {
      type: String,
      default: null
    },
  },
  data: () => ({
    alerts: [],
  }),
  computed: {
    displayAlerts() {
      return this.alerts && this.alerts.length;
    },
  },
  created() {
    this.$root.$on('activity-notification-alert', alert => this.alerts.push(alert));
    this.$root.$on('activity-shared', (spaces) => {
      const spacesList = [];
      if (spaces && spaces.length > 0) {
        spaces.forEach(space => spacesList.push(this.truncateString(space)));
        const message = `${this.$t('UIActivity.share.message')} ${spacesList.join(', ')}`;
        this.$root.$emit('activity-notification-alert', {
          message,
          type: 'success',
          spaces,
        });
      }
    });
  },
  methods: {
    deleteAlert(alert) {
      const index = this.alerts.indexOf(alert);
      this.alerts.splice(index, 1);
      this.$forceUpdate();
    },
    truncateString(str) {
      if (str.length <= 10) {
        return str;
      }
      return str.slice(0, 10).concat('...');
    }
  },
};
</script>