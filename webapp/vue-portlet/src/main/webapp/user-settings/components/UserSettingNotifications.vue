<template>
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
</template>

<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    isEmailNotificationEnabled: false,
    isPushNotificationEnabled: false,
    isOnSiteNotificationEnabled: false,
  }),
  computed: {
  },
  watch: {
    value() {
      this.$emit('input', this.value);
    },
  },
  methods: {
    openNotificationSettingDetail() {
      // TODO
    },
  },
};
</script>

