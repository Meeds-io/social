<template>
  <v-app v-if="displayed">
    <user-setting-security-window
      v-if="displayDetails"
      @back="closeSecurityDetail" />
    <v-card v-else class="ma-4 border-radius" flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('UserSettings.security') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn
              small
              icon
              @click="openSecurityDetail">
              <v-icon size="24" class="text-sub-title">
                fa-caret-right
              </v-icon>
            </v-btn>
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
    displayDetails: false,
  }),
  created() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
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

