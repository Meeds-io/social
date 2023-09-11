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
        <template v-if="pluginOption.channelId === emailChannel && digestMailNotificationEnabled">
          <v-row class="ma-0">
            <v-col>
              <label for="EMAIL_DIGEST" class="align-start">{{ $t('UINotification.label.selectBox-mail') }}</label>
            </v-col>
            <v-col class="text-right">
              <select
                v-model="digest"
                :disabled="!pluginOption.channelActive"
                name="EMAIL_DIGEST"
                class="col-auto me-2 my-auto px-3 py-0 subtitle-1 ignore-vuetify-classes">
                <option
                  v-for="digestOption in digestOptions"
                  :key="digestOption.value"
                  :value="digestOption.value">
                  {{ digestOption.text }}
                </option>
              </select>
            </v-col>
          </v-row>
        </template>
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
    digestMailNotificationEnabled: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    drawer: false,
    channels: {},
    digest: null,
    emailChannel: null,
    listChannelOptions: [],
    plugin: null,
    group: null,
  }),
  computed: {
    digestOptions() {
      return this.settings && Object.keys(this.settings.digestLabels).map(digestLabel => ({
        value: digestLabel,
        text: this.settings.digestLabels[digestLabel],
      }));
    },
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
      this.emailChannel = this.settings && this.settings.emailChannel;

      this.listChannelOptions = this.settings?.channelCheckBoxList?.filter(channelChoice => channelChoice.channelActive && channelChoice.allowed && channelChoice.pluginId === this.plugin.type)
        .map(channelChoice => JSON.parse(JSON.stringify(channelChoice))) || [];

      const digestChoice = this.settings && this.settings.emailDigestChoices && this.settings.emailDigestChoices.find(choice => choice && choice.channelActive && choice.channelId === this.emailChannel && choice.pluginId === this.plugin.type);
      this.digest = digestChoice && digestChoice.value;

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
        body: `digest=${this.digest}&channels=${channels}`
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

