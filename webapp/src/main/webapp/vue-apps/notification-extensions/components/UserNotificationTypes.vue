<template>
  <v-list class="px-4 pt-0 pb-4" dense>
    <h4>{{ $t('Notification.label.types') }}</h4>
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
  }),
  watch: {
    badgeByPlugin() {
      this.refreshExtensions();
    },
  },
  created() {
    this.refreshExtensions();
  },
  methods: {
    refreshExtensions() {
      const groups = [];
      extensionRegistry.loadExtensions('WebNotification', 'notification-group-extension')
        .forEach(group => {
          const badge = this.badgeByPlugin && group.plugins && group.plugins.reduce((sum, p) => sum += this.badgeByPlugin[p] || 0, 0) || 0;
          group = {...group, badge};
          groups.push({
            ...group,
            badge,
            label: this.$te(`Notification.label.types.${group.name}`)
              ? this.$t(`Notification.label.types.${group.name}`)
              : group.name
          });
        });
      const badge = this.badgeByPlugin && Object.keys(this.badgeByPlugin).reduce((sum, p) => sum += this.badgeByPlugin[p] || 0, 0) || 0;
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