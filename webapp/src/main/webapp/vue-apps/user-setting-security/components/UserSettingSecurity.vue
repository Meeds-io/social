<template>
  <v-app>
    <v-card
      class="application-body"
      flat>
      <v-card-title class="text-title pb-0">
        {{ $t('UserSettings.security.title') }}
      </v-card-title>
      <v-list>
        <v-list-item dense>
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('UserSettings.security.emailChange.title') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-tooltip bottom>
              <template #activator="{on, attrs}">
                <div
                  v-on="on"
                  v-bind="attrs">
                  <v-btn
                    :aria-label="emailChangeTooltip"
                    small
                    icon
                    @click="$refs.emailDrawer.open()">
                    <v-icon size="24" class="icon-default-color">fa-edit</v-icon>
                  </v-btn>
                </div>
              </template>
              <span>{{ emailChangeTooltip }}</span>
            </v-tooltip>  
          </v-list-item-action>
        </v-list-item>
        <v-list-item dense>
          <v-list-item-content>
            <v-list-item-title>
              {{ $t('UserSettings.security.passwordChange.title') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-tooltip bottom>
              <template #activator="{on, attrs}">
                <div
                  v-on="on"
                  v-bind="attrs">
                  <v-btn
                    :aria-label="passwordChangeTooltip"
                    :disabled="!allowedToChangePassword"
                    small
                    icon
                    @click="$refs.passwordDrawer.open()">
                    <v-icon size="24" class="icon-default-color">fa-edit</v-icon>
                  </v-btn>
                </div>
              </template>
              <span>{{ passwordChangeTooltip }}</span>
            </v-tooltip>  
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
    <user-setting-security-email-drawer
      ref="emailDrawer" />
    <user-setting-security-password-drawer
      ref="passwordDrawer" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    displayed: true,
    allowedToChangePassword: false,
  }),
  computed: {
    passwordChangeTooltip() {
      return this.allowedToChangePassword ? this.$t('UserSettings.button.tooltip.enabled') : this.$t('UserSettings.button.tooltip.disabled');
    },
    emailChangeTooltip() {
      return this.$t('UserSettings.security.emailChange.tooltip');
    },
  },
  watch: {
    displayed() {
      if (this.displayed) {
        this.$nextTick().then(() => this.$root.$emit('application-cache'));
      }
      this.$root.$updateApplicationVisibility(this.displayed);
    },
  },
  created() {
    document.addEventListener('showSettingsApps', this.showApp);
    document.addEventListener('hideSettingsApps', this.hideApp);
    this.init();
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$applicationLoaded());
    this.$root.$updateApplicationVisibility(this.displayed);
  },
  methods: {
    async init() {
      const data = await this.$userService.isSynchronizedUserAllowedToChangePassword();
      this.allowedToChangePassword = data?.isSynchronizedUserAllowedToChangePassword === 'true';
    },
    showApp() {
      this.displayed = true;
    },
    hideApp() {
      this.displayed = false;
    },
  }
};
</script>

