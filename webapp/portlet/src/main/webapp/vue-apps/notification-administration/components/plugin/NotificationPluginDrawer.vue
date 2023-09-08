<template>
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    class="userNotificationDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template slot="title">
      <span class="text-wrap">
        {{ pluginLabel }}
      </span>
    </template>
    <template slot="content">
      <div v-for="pluginOption in listChannelOptions" :key="pluginOption.channelId">
        <v-row v-if="pluginOption.allowed" class="ma-0 d-flex">
          <v-col class="flex-grow-1">
            <!-- eslint-disable-next-line -->
            <label :for="pluginOption.channelId" class="my-auto">{{ channelLabels[pluginOption.channelId] }}</label>
          </v-col>
          <v-col class="text-right flex-grow-0">
            <v-switch
              v-model="channels[pluginOption.channelId]"
              :name="pluginOption.channelId"
              :id="pluginOption.channelId"
              hide-details
              dense
              class="mt-0" />
          </v-col>
        </v-row>
      </div>
    </template>
    <template slot="footer">
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="cancel">
          {{ $t('UserSettings.button.cancel') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          @click="save">
          {{ $t('UserSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
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
    drawer: false,
    channels: {},
    listChannelOptions: [],
    plugin: null,
    group: null,
  }),
  computed: {
    channelLabels() {
      return this.settings?.channelLabels;
    },
    groupLabel() {
      return this.group && this.settings?.groupsLabels[this.group.groupId];
    },
    pluginLabel() {
      const pluginId = this.plugin?.type;
      return this.$te(`NotificationAdmin.${pluginId}`) && this.$t(`NotificationAdmin.${pluginId}`) || this.settings?.pluginLabels[pluginId];
    },
  },
  methods: {
    open(plugin, group) {
      this.plugin = plugin;
      this.group = group;
      this.listChannelOptions = this.settings?.channelCheckBoxList?.filter(channelChoice => channelChoice.pluginId === this.plugin.type)
        .map(channelChoice => JSON.parse(JSON.stringify(channelChoice))) || [];

      this.channels = {};
      this.listChannelOptions.forEach(option => {
        this.channels[option.channelId] = option.channelActive;
      });
      this.listChannelOptions.sort((a, b) => a.channelId.localeCompare(b.channelId));

      this.$refs.drawer.open();
    },
    save() {
      this.$refs.drawer.startLoading();
      const channels = Object.keys(this.channels).map(channelId => `${channelId}=${this.channels[channelId]}`).join(',');
      return this.$notificationAdministration.savePluginSettings(this.plugin.type, channels)
        .then(() => {
          this.$root.$emit('refresh');
          this.$refs.drawer.close();
        })
        .finally(() => {
          this.$refs.drawer.endLoading();
        });
    },
    cancel() {
      this.$refs.drawer.close();
    },
  },
};
</script>

