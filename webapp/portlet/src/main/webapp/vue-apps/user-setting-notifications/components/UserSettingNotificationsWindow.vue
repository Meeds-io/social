<template>
  <v-card
    v-if="settings"
    class="ma-4"
    flat>
    <v-toolbar
      class="border-box-sizing"
      flat>
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
      <v-toolbar-title class="ps-0">
        {{ $t('UserSettings.manageNotifications') }}
      </v-toolbar-title>
      <v-spacer />
    </v-toolbar>

    <v-flex class="ma-3 white">
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
      :group="selectedGroup"
      :plugin="selectedPlugin"
      :digest-mail-notification-enabled="digestMailNotificationEnabled"
      :settings="settings" />
  </v-card>
</template>

<script>
export default {
  props: {
    settings: {
      type: Object,
      default: null,
    },
    digestMailNotificationEnabled: {
      type: Boolean,
      default: false
    }
  },
  data: () => ({
    selectedPlugin: null,
    selectedGroup: null,
  }),
  methods: {
    openDrawer(plugin, group) {
      this.selectedPlugin = plugin;
      this.selectedGroup = group;
      this.$nextTick(this.$refs.drawer.open);
    },
  },
};
</script>