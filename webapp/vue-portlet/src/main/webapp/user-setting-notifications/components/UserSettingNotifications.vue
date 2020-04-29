<template>
  <v-app v-if="displayed">
    <v-card class="ma-4 border-radius" flat>
      <v-list @click="openNotificationSettingDetail">
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('UserSettings.notifications') }}
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item two-line>
          <v-list-item-content>
            <v-list-item-title class="text-color">
              {{ $t('UserSettings.notifyByEmail') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title">
              {{ $t('UserSettings.notifyByEmailDescription') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-switch v-model="isEmailNotificationEnabled" />
          </v-list-item-action>
        </v-list-item>
        <v-divider class="mx-4" />
        <v-list-item two-line>
          <v-list-item-content>
            <v-list-item-title class="text-color">
              {{ $t('UserSettings.notifyOnMobile') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title">
              {{ $t('UserSettings.notifyOnMobileDescription') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn icon>
              <v-switch v-model="isPushNotificationEnabled" />
            </v-btn>
          </v-list-item-action>
        </v-list-item>
        <v-divider class="mx-4" />
        <v-list-item two-line>
          <v-list-item-content>
            <v-list-item-title class="text-color">
              {{ $t('UserSettings.notifyOnSite') }}
            </v-list-item-title>
            <v-list-item-subtitle class="text-sub-title">
              {{ $t('UserSettings.notifyOnSiteDescription') }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn icon>
              <v-switch v-model="isOnSiteNotificationEnabled" />
            </v-btn>
          </v-list-item-action>
        </v-list-item>
        <v-divider class="mx-4" />
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="subtitle-1 text-color">
              {{ $t('UserSettings.manageNotifications') }}
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action>
            <v-btn icon>
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
    isEmailNotificationEnabled: false,
    isPushNotificationEnabled: false,
    isOnSiteNotificationEnabled: false,
    displayed: true,
    skeleton: true,
  }),
  created() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    document.addEventListener('hideSettingsApps', (event) => {
      if (event && event.detail && this.id !== event.detail) {
        this.displayed = false;
      }
    });
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
  methods: {
    openNotificationSettingDetail() {
      // TODO
    },
  },
};
</script>

