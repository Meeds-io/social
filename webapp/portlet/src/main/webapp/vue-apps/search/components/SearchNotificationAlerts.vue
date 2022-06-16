<template>
  <v-snackbar
    v-model="snackbar"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    color="transparent"
    elevation="0"
    app
    class="z-index-modal" >
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
    document.addEventListener('search-notification-alert', event => {
      if (event?.detail) {
        this.alert = event.detail;
      }});
  },
  methods: {
    clear() {
      this.alert = null;
    },
  },
};
</script>