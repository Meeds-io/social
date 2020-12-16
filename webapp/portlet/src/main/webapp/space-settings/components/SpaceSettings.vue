<template>
  <v-app class="transparent" flat>
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" class="mb-6" />
    <space-chat-setting v-if="displaySpaceChatSetting" :space-id="spaceId"></space-chat-setting>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    displaySpaceChatSetting: true,
  }),
  created() {
    document.addEventListener('hideSettingsApps', () => this.displaySpaceChatSetting = false);
    document.addEventListener('showSettingsApps', () => this.displaySpaceChatSetting = true);
  },
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
};
</script>