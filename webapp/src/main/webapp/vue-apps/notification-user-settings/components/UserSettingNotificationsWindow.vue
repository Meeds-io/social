<template>
  <div
    v-if="settings"
    class="application-body">
    <v-toolbar
      class="border-box-sizing"
      flat>
      <v-btn
        class="mx-1"
        height="36"
        icon
        width="36"
        @click="$emit('back')">
        <v-icon size="20">
          {{ $vuetify.rtl && 'mdi-arrow-right' || 'mdi-arrow-left' }}
        </v-icon>
      </v-btn>
      <v-toolbar-title class="ps-0 text-title">
        {{ $t('UserSettings.manageNotifications') }}
      </v-toolbar-title>
      <v-spacer />
    </v-toolbar>

    <v-flex class="ma-3">
      <user-setting-notification-group
        v-for="group in settings.groups"
        :key="group.groupId"
        :digest-mail-notification-enabled="digestMailNotificationEnabled"
        :group="group"
        :settings="settings"
        @edit="openDrawer" />
    </v-flex>
    <user-setting-notification-drawer
      ref="drawer"
      :digest-mail-notification-enabled="digestMailNotificationEnabled"
      :settings="settings" />
  </div>
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
    created () {
      this.$featureService.isFeatureEnabled('digestMailNotification')
        .then(enabled => this.digestMailNotificationEnabled = enabled);
    },
    methods: {
      openDrawer (plugin, group) {
        this.$nextTick(() => this.$refs.drawer.open(plugin, group));
      },
    },
  };
</script>