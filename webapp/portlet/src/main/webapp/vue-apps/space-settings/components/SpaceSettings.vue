<template>
  <v-app
    class="transparent"
    flat
    v-if="displayed">
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" class="mb-6" />
    <template>
      <extension-registry-components
        :params="spaceSettingParam"
        name="SpaceSettings"
        type="space-settings-components"
        parent-element="div"
        element="div"
        class="mb-6"
        elementClass="mb-6"/>
    </template>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    displayed: true,
  }),
  mounted() {
    document.addEventListener('hideSettingsApps', () => this.displayed = true);
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
};
</script>
