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
    this.$root.$on('space-notification-alert', alert => this.alert = alert);
  },
  methods: {
    clear() {
      this.alert = null;
    },
  },
};
</script>