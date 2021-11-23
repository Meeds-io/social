<template>
  <v-app
    class="transparent"
    flat
    v-if="displayed">
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
  props: {
    space: {
      type: Object,
      default: () => null
    },
  },
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    displayed: true,
    spaceExternalSettings: []
  }),
  created() {
    // add external components
    const externalComponents = extensionRegistry.loadComponents('external-space').map(component => component.componentOptions.componentImpl);
    this.spaceExternalSettings.push(...externalComponents);
    document.addEventListener('hideSettingsApps', () => this.displayed = true);
    document.addEventListener('showSettingsApps', () => this.displayed = true);
  },
  mounted() {
    document.addEventListener('addSpaceSettingsExternalComponents', (event) => {
      if (event && event.detail && this.isApplicationInstalled(event.detail.appId)) {
        this.spaceExternalSettings.push(event.detail.componentImpl);
      }
    });
    this.$nextTick().then(() => this.$root.$applicationLoaded());
  },
  methods: {
    isApplicationInstalled(appId) {
      const allApplication = this.space && this.space.app.split(',');
      if (allApplication) {
        let applicationDetails = [];
        for (let  i =0;i< allApplication.length;i++) {
          applicationDetails =  allApplication[i].split(':');
          if (applicationDetails[1] === appId) {
            return true;
          }
        }
        return false;
      }
    }
  }
};
</script>