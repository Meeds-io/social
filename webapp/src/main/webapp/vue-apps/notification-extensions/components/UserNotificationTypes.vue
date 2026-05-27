<template>
  <v-list class="px-4 pt-0 pb-4" dense>
    <div class="text-header my-4">{{ $t('Notification.label.types') }}</div>
    <v-list-item-group
      v-model="selectedGroupIndex"
      color="primary"
      dense
      mandatory>
      <user-notification-type
        v-for="(group, index) in groups"
        :key="group.name"
        :group="group"
        :selected="index === selectedGroupIndex"
        :unread-only="index === unreadIndex"
        @select="selectType(index, $event)" />
    </v-list-item-group>
  </v-list>
</template>
<script>
export default {
  props: {
    badgeByPlugin: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    loading: false,
    unreadIndex: false,
    selectedGroupIndex: null,
    groups: [],
    settings: null,
  }),
  watch: {
    badgeByPlugin() {
      this.refreshSettings();
    },
  },
  created() {
    this.refreshSettings();
  },
  methods: {
    async refreshSettings() {
      this.loading = true;
      try {
        const resp = await fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/${eXo.env.portal.userName}`, {
          method: 'GET',
          credentials: 'include',
        });
        if (resp && resp.ok) {
          this.settings = await resp.json();
          this.refreshExtensions();
        }
      } catch (error) {
        console.error('Failed to fetch notification settings:', error);
      } finally {
        this.loading = false;
      }
    },
    refreshExtensions() {
      const groups = [];
      const selectedGroupName = this.groups[this.selectedGroupIndex] && this.groups[this.selectedGroupIndex].name;
      extensionRegistry.loadExtensions('WebNotification', 'notification-group-extension')
        .forEach(group => {
          if (this.settings && group?.plugins?.length) {
            const listPlugins = group.plugins.filter(pluginId => {
              return this.settings.channelCheckBoxList && this.settings.channelCheckBoxList.some(choice => choice.channelActive && choice.pluginId === pluginId);
            });
            if (!listPlugins.length) {
              return;
            }
          }
          const badge = this.badgeByPlugin && group.plugins && group.plugins.reduce((sum, p) => sum += this.badgeByPlugin[p] || 0, 0) || 0;
          groups.push({
            ...group,
            badge,
            label: this.$te(`Notification.label.types.${group.name}`)
              ? this.$t(`Notification.label.types.${group.name}`)
              : group.name
          });
        });
      const badge = this.badgeByPlugin && Object.keys(this.badgeByPlugin).reduce((sum, p) => {
        const isEnabled = !this.settings || (this.settings.channelCheckBoxList && this.settings.channelCheckBoxList.some(choice => choice.channelActive && choice.pluginId === p));
        return isEnabled ? sum + (this.badgeByPlugin[p] || 0) : sum;
      }, 0) || 0;
      groups.splice(0, 0, {
        rank: -1,
        name: 'all',
        label: this.$t('Notification.label.types.all'),
        icon: 'fa-bell',
        badge,
        plugins: null,
      });
      groups.sort((g1, g2) => (g1.rank || 100) - (g2.rank || 100));
      this.groups = groups;
      if (selectedGroupName) {
        const newIndex = this.groups.findIndex(g => g.name === selectedGroupName);
        this.selectedGroupIndex = newIndex !== -1 ? newIndex : (this.groups.length ? 0 : null);
      }
    },
    selectType(index, unreadOnly) {
      this.selectedGroupIndex = index;
      if (this.unreadIndex === index || !unreadOnly) {
        this.unreadIndex = false;
      } else {
        this.unreadIndex = index;
      }
      const selectedGroup = this.groups[this.selectedGroupIndex];
      this.$emit('change', selectedGroup?.name, selectedGroup?.plugins, this.unreadIndex === index);
    },
  },
};
</script>