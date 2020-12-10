<template>
  <v-app class="transparent" flat>
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" class="mb-6" />
    <v-card v-if="loading" class="border-radius" flat>
      <v-list>
        <v-list-item>
          <v-list-item-content>
            <v-skeleton-loader type="list-item-two-line"/>
          </v-list-item-content>
          <v-list-item-action>
            <v-skeleton-loader type="avatar" />
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-card>
    <v-card v-else-if="displaySpaceChatSetting && !loading" class="border-radius" flat>
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
    displaySpaceChatSetting: false,
    loading: true,
    userSettings: {},
  }),
  created() {
    this.$spaceService.getSpaceApplicationsChoices()
      .then(data => {
        this.displaySpaceChatSetting = data.some(app => app.displayName.includes('Chat Application'));        
        //check if chat enabled
        this.$spaceService.getUserSettings()
          .then(userSettings => {
            this.userSettings = userSettings;
            this.$spaceService.isRoomEnabled(this.userSettings, this.spaceId)
              .then(value => {
                this.spaceChatEnabled = value === 'true';
              })
              .finally(() => {
                this.loading = false;
              });
          });
      });
    document.addEventListener('hideSettingsApps', () => this.displaySpaceChatSetting = false);
    document.addEventListener('showSettingsApps', () => this.displaySpaceChatSetting = true);
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  methods: {
    enableDisableChat() {
      this.$spaceService.updateRoomEnabled(this.userSettings, this.spaceId, this.spaceChatEnabled);
    }
  }
};
</script>