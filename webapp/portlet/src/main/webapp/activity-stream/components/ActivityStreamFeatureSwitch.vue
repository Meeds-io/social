<template>
  <div>
    <div class="white mb-3 py-2 primary--text">
      <v-btn
        link
        text
        class="primary--text font-weight-bold text-capitalize"
        @click="switchActivityStream">
        <v-icon class="me-3" size="16">far fa-window-restore</v-icon>
        {{ buttonText }}
      </v-btn>
    </div>
    <div v-if="useNewApp" class="white d-flex flex-column text-center mb-4">
      <v-img
        src="/social-portlet/activity-stream/comingSoon.png"
        width="450px"
        max-width="100%"
        class="mx-auto mt-10"
        @load="displayText" />
      <h3 v-if="imageLoaded" class="font-weight-bold">{{ $t('activityStream.comingSoon') }}</h3>
    </div>
  </div>
</template>

<script>
export default {
  data: () => ({
    useNewApp: false,
    imageLoaded: false,
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
        this.$root.$emit('activity-stream-display');
        $('.uiActivityStreamPortlet .uiActivitiesDisplay').hide();
        $('.uiActivityStreamPortlet .openLink').hide();
      } else {
        this.$root.$emit('activity-stream-hide');
        $('.uiActivityStreamPortlet .uiActivitiesDisplay').show();
        $('.uiActivityStreamPortlet .openLink').show();
      }
    },
  },
  methods: {
    switchActivityStream() {
      this.useNewApp = !this.useNewApp;
    },
    displayText() {
      window.setTimeout(() => this.imageLoaded = true, 200);
    },
  },
};
</script>