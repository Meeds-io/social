<template>
  <v-app
    class="transparent"
    flat
    v-if="displayed">
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" class="mb-6" />
    <template v-for="(spaceExternalSetting, index) in spaceExternalSettings">
      <component
        class="mb-6"
        :index="index"
        :space-id="spaceId"
        :is="spaceExternalSetting"
        :key="spaceExternalSetting.name" />
    </template>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    displayed: true,
    spaceApplications: [],
    spaceExternalSettings: []
  }),
  created() {
    if (this.spaceId) {
      this.$spaceService.getSpaceApplications(this.spaceId)
        .then(applications =>
          applications.map(app =>  {
            this.spaceApplications.push(app.id);
          })
        ).then(() => {
          document.addEventListener('component-external-apps-space-settings-space-settings-updated', (event) => {
            if (event && event.detail ) {
              if (this.spaceApplications.find(app => app.id === event.detail.componentOptions.appId) || !event.detail.componentOptions.appId) {
                this.spaceExternalSettings.push(event.detail.componentOptions.componentImpl);
              }
            }
          });
          document.addEventListener('hideSettingsApps', () => this.displayed = false);
          document.addEventListener('showSettingsApps', () => this.displayed = true);
          const externalComponents = extensionRegistry.loadComponents('external-apps-space-settings').map(component => component.componentOptions.componentImpl);
          this.spaceExternalSettings.push(...externalComponents);
        }).finally(() => this.$root.$applicationLoaded());
    }
  }
};
</script>