<template>
  <div>
    <div class="white mb-3 py-2 primary--text">
      <v-btn
        link
        text
        class="primary--text font-weight-bold"
        @click="switchActivityStream">
        <v-icon class="me-3" size="18">far fa-window-restore</v-icon>
        {{ buttonText }}
      </v-btn>
    </div>
    <div v-if="useNewApp" class="white text-center pb-4">
      <v-img src="/social-portlet/activity-stream/comingSoon.png" />
      <h2>{{ $t('activityStream.comingSoon') }}</h2>
    </div>
  </div>
</template>

<script>
export default {
  data: () => ({
    useNewApp: false,
  }),
  computed: {
    buttonText() {
      if (this.useNewApp) {
        return this.$t('activityStream.switchToOldApp');
      } else {
        return this.$t('activityStream.switchToNewApp');
      }
    },
  },
  watch: {
    useNewApp() {
      if (this.useNewApp) {
        $('.uiActivityStreamPortlet').hide();
      } else {
        $('.uiActivityStreamPortlet').show();
      }
    },
  },
  methods: {
    switchActivityStream() {
      this.useNewApp = !this.useNewApp;
    },
  },
};
</script>