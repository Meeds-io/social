<template>
  <v-card
    v-if="settings"
    flat>
    <div class="d-flex align-center">
      <v-btn
        class="mx-1"
        icon
        height="36"
        width="36"
        @click="$emit('back')">
        <v-icon size="20">
          {{ $vuetify.rtl && 'mdi-arrow-right' || 'mdi-arrow-left' }}
        </v-icon>
      </v-btn>
      <span class="text-header text-sub-title ps-0">
        {{ $t('UserSettings.manageNotifications') }}
      </span>
    </div>
    <v-flex class="white">
      <user-setting-notification-group
        v-for="group in settings.groups"
        :settings="settings"
        :key="group.groupId"
        :group="group"
        :digest-mail-notification-enabled="digestMailNotificationEnabled"
        @edit="openDrawer" />
    </v-flex>
    <user-setting-notification-drawer
      ref="drawer"
      :settings="settings"
      :digest-mail-notification-enabled="digestMailNotificationEnabled" />
  </v-card>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    digestMailNotificationEnabled: false,
  }),
  created() {
    this.$featureService.isFeatureEnabled('digestMailNotification')
      .then(enabled => this.digestMailNotificationEnabled = enabled);
  },
  methods: {
    openDrawer(plugin, group) {
      this.$nextTick(() => this.$refs.drawer.open(plugin, group));
    },
  },
};
</script>