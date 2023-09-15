<template>
  <v-list class="px-4 pt-0 pb-4" dense>
    <h4>{{ $t('Notification.label.types') }}</h4>
    <v-list-item-group
      v-model="selectedGroupIndex"
      color="primary"
      dense
      mandatory>
      <v-list-item
        v-for="group in groups"
        :key="group.name"
        class="px-4 mx-n4 rounded-lg"
        dense>
        <v-list-item-icon class="me-2 align-center justify-center">
          <v-icon size="18" class="icon-default-color">
            {{ group.icon }}
          </v-icon>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title class="subtitle-2">
            {{ group.label }}
          </v-list-item-title>
        </v-list-item-content>
        <v-scale-transition>
          <v-list-item-icon v-if="group.badge" class="me-2 full-height align-center justify-center position-relative">
            <v-tooltip bottom>
              <template #activator="{on, bind}">
                <v-card
                  :value="group.badge > 0"
                  min-height="30"
                  max-height="30"
                  min-width="30"
                  class="pa-1 rounded-circle caption d-flex align-center justify-center ms-auto my-auto error-color-background"
                  flat
                  dark
                  v-on="on"
                  v-bind="bind">
                  {{ group.badge > 99 && '99+' || group.badge }}
                </v-card>
              </template>
              <span>{{ $t('Notification.label.types.unread', {0: group.badge}) }}</span>
            </v-tooltip>
          </v-list-item-icon>
        </v-scale-transition>
      </v-list-item>
    </v-list-item-group>
  </v-list>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    badge: {
      type: Object,
      default: null,
    },
    badgeByPlugin: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    loading: false,
    selectedGroupIndex: null,
    groups: [],
  }),
  computed: {
    selectedGroup() {
      return this.groups[this.selectedGroupIndex];
    },
  },
  watch: {
    selectedGroup() {
      this.$emit('input', this.selectedGroup?.plugins);
      this.$emit('group', this.selectedGroup?.name);
    },
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
  },
};
</script>