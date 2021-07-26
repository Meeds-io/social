<template>
  <v-app class="transparent" flat>
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" class="mb-6" />
    <template v-if="displaySpaceExternalSettings">
      <template v-for="(spaceExternalSetting, index) in spaceExternalSettings">
        <component
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
    displaySpaceExternalSettings: true,
    spaceExternalSettings: []
  }),
  created() {
    // add external components
    const externalComponents = extensionRegistry.loadComponents('external-space').map(component => component.componentOptions.componentImpl);
    this.spaceExternalSettings.push(...externalComponents);
    
    document.addEventListener('hideSettingsApps', () => this.displaySpaceExternalSettings = false);
    document.addEventListener('showSettingsApps', () => this.displaySpaceExternalSettings = true);
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
};
</script>