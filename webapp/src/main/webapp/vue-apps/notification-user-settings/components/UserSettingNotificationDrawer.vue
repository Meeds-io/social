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
            <label :for="pluginOption.channelId" class="my-auto">{{ channelLabels[pluginOption.channelId] }}</label>
          </v-col>
          <v-col class="text-right flex-grow-0">
            <v-switch
              v-model="channels[pluginOption.channelId]"
              :name="pluginOption.channelId"
              :disabled="!pluginOption.channelActive"
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
      return this.settings && this.settings.channelLabels;
    },
    groupLabel() {
      return this.settings && this.group && this.settings.groupsLabels[this.group.groupId];
    },
    pluginLabel() {
      return this.settings && this.plugin && this.settings.pluginLabels[this.plugin.type];
    },
  },
  methods: {
    open(plugin, group) {
      this.plugin = plugin;
      this.group = group;

      this.listChannelOptions = this.settings?.channelCheckBoxList?.filter(channelChoice => channelChoice.channelActive && channelChoice.allowed && channelChoice.pluginId === this.plugin.type)
        .map(channelChoice => JSON.parse(JSON.stringify(channelChoice))) || [];

      this.channels = {};
      this.listChannelOptions.forEach(option => {
        this.channels[option.channelId] = option.allowed && option.active && option.channelActive;
      });
      this.listChannelOptions.sort((a, b) => a.channelId.localeCompare(b.channelId));

      this.$refs.drawer.open();
    },
    save() {
      this.$refs.drawer.startLoading();
      const channels = Object.keys(this.channels).map(channelId => `${channelId}=${this.channels[channelId]}`).join(',');
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/notifications/settings/${eXo.env.portal.userName}/plugin/${this.plugin.type}`, {
        method: 'PATCH',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `channels=${channels}`
      }).then(resp => {
        if (resp && resp.ok) {
          this.$root.$emit('refresh');
          this.$refs.drawer.close();
        }
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

