<template>
  <v-card
    v-if="settings"
    class="ma-4"
    flat>
    <v-toolbar
      class="border-box-sizing"
      flat>
      <v-btn
        icon
        @click="$emit('back')">
        <v-icon>mdi-arrow-left</v-icon>
      </v-btn>
      <v-toolbar-title>
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
        @edit="openDrawer" />
    </v-flex>
    <user-setting-notification-drawer
      ref="drawer"
      :group="selectedGroup"
      :plugin="selectedPlugin"
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