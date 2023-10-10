<template>
  <v-app v-if="displayed">
    <widget-wrapper extra-class="mb-2 pa-0">
      <template v-if="!displayDetails" #title>
        {{ $t('UserSettings.security') }}
      </template>
      <template v-if="!displayDetails" #action>
        <span
          :title="allowedToChangePassword ? $t('UserSettings.button.tooltip.enabled') : $t('UserSettings.button.tooltip.disabled')">
          <v-btn
            :disabled="!allowedToChangePassword"
            small
            icon
            @click="openSecurityDetail">
            <v-icon size="24" class="text-sub-title">
              {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
            </v-icon>
          </v-btn>
        </span>
      </template>
      <user-setting-security-window
        v-if="displayDetails"
        @back="closeSecurityDetail" />
    </widget-wrapper> 
  </v-app>
</template>

<script>
export default {
  data: () => ({
    id: `Security${parseInt(Math.random() * 10000)
      .toString()
      .toString()}`,
    displayed: true,
    allowedToChangePassword: false,
    displayDetails: false,
  }),
  watch: {
    displayed() {
      this.$nextTick().then(() => this.$root.$emit('application-cache'));
    },
  },
  created() {
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    this.$userService.isSynchronizedUserAllowedToChangePassword().then(
      (data) => {
        this.allowedToChangePassword = data.isSynchronizedUserAllowedToChangePassword === 'true';
      });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$applicationLoaded());
  },
  methods: {
    openSecurityDetail() {
      document.dispatchEvent(new CustomEvent('hideSettingsApps', {detail: this.id}));
      this.displayDetails = true;
    },
    closeSecurityDetail() {
      document.dispatchEvent(new CustomEvent('showSettingsApps'));
      this.displayDetails = false;
    },
  },
};
</script>

