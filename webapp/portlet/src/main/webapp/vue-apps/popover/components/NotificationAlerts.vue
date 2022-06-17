<template>
  <v-snackbar
    v-model="snackbar"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    class="z-index-modal"
    color="transparent"
    elevation="0"
    app>
    <exo-notification-alert
      :alert="alert"
      no-timeout
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
      this.snackbar = !this.alert;
      this.$nextTick().then(() => this.snackbar = !!this.alert);
    },
  },
  created() {
    document.addEventListener('notification-alert', event => this.alert = event?.detail);
  },
  methods: {
    clear() {
      this.alert = null;
    },
  },
};
</script>