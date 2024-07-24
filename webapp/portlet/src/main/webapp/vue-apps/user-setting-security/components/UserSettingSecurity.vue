<template>
  <v-app>
    <user-setting-security-window
      v-if="displayDetails"
      @back="closeSecurityDetail" />
    <v-card
      v-else
      class="application-body"
      flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="text-title">
              {{ $t('UserSettings.security') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <span
              :title="allowedToChangePassword ? $t('UserSettings.button.tooltip.enabled') : $t('UserSettings.button.tooltip.disabled')">
              <v-btn
                :disabled="!allowedToChangePassword"
                small
                icon
                @click="openSecurityDetail">
                <v-icon size="24" class="icon-default-color">
                  {{ $vuetify.rtl && 'fa-caret-left' || 'fa-caret-right' }}
                </v-icon>
              </v-btn>
            </span>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
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
      if (this.displayed) {
        this.$nextTick().then(() => this.$root.$emit('application-cache'));
      }
      this.$root.$updateApplicationVisibility(this.displayed);
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

