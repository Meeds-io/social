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
        src="/social-portlet/activity-stream/comingSoonV2.png"
        width="190px"
        max-width="100%"
        class="mx-auto mt-6"
        @load="displayText" />
      <div
        v-if="imageLoaded"
        class="v-label font-weight-bold mt-0 mb-6 text-color"
        v-sanitized-html="comingSoonText">
      </div>
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
    comingSoonText() {
      return this.$t('activityStream.comingSoonV2', {
        0: `<a href="${eXo.env.portal.context}/${eXo.env.portal.portalName}/tribe-feedback">`,
        1: '</a>',
      });
    },
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
  mounted() {
    if (eXo.developing) {
      this.switchActivityStream();
    }
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