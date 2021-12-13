<template>
  <v-app class="transparent" flat v-if="displayed">
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" class="mb-6" />
    <template>
      <template v-for="(spaceExternalSetting, index) in spaceExternalSettings">
        <component
          class="mb-6"
          :index="index"
          :space-id="spaceId"
          :is="spaceExternalSetting"
          :key="spaceExternalSetting.name" />
      </template>
    </template>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    displayed: true,
    spaceExternalSettings: []
  }),
  created() {
    // add external components
    const externalComponents = extensionRegistry.loadComponents('external-apps-space-settings').map(component => component.componentOptions.componentImpl);
    this.spaceExternalSettings.push(...externalComponents);

    document.addEventListener('addSpaceSettingsExternalComponents', (event) => {
      if (event && event.detail) {
        this.spaceExternalSettings.push(event.detail.componentImpl);
      }
    });

    document.addEventListener('hideSettingsApps', () => this.displayed = true);
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$applicationLoaded());
  },
};
</script>