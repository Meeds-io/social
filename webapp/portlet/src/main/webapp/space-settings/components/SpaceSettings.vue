<template>
  <v-app class="transparent" flat>
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" class="mb-6" />
    <v-card v-if="displaySpaceChatSetting" class="border-radius" flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title class="title text-color">
              {{ $t('SpaceSettings.Chat') }}
            </v-list-item-title>
            <v-list-item-subtitle>
              {{ 'Enable space chat' }}
            </v-list-item-subtitle>
          </v-list-item-content>
          <v-list-item-action>
            <v-switch v-model="spaceChatEnabled" @change="enableDisableChat"></v-switch>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    spaceChatEnabled: false,
    displaySpaceChatSetting: true,
  }),
  created() {
    this.$spaceService.getSpaceApplicationsChoices()
      .then(data => {
        this.displaySpaceChatSetting = data.some(app => app.applicationName === 'ChatApplication');
      });
    document.addEventListener('hideSettingsApps', () => this.displaySpaceChatSetting = false);
    document.addEventListener('showSettingsApps', () => this.displaySpaceChatSetting = true);
    console.log('Check if spaceChatEnabled');
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    enableDisableChat() {
      //Todo
      console.log(`${this.spaceChatEnabled ? 'Enable' : 'Disable'} space chat !`);
    }
  }
};
</script>