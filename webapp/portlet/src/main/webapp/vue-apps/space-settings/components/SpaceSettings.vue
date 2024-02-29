<template>
  <v-app
    id="SpaceSettings"
    class="transparent"
    flat
    v-if="displayed">
    <space-setting-general :space-id="spaceId" class="mb-5" />
    <space-setting-applications :space-id="spaceId" class="mb-5" />
    <template>
      <extension-registry-components
        :key="spaceApplications"
        :params="extensionParams"
        name="SpaceSettings"
        type="space-settings-components"
        parent-element="div"
        element="div"
        class="mb-6"
        element-class="mb-6" />
    </template>
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    spaceApplications: null,
    displayed: true,
  }),
  created() {
    this.$spaceService.getSpaceApplications(this.spaceId).then(applications => {
      this.spaceApplications=applications;
    });
  },
  computed: {
    extensionParams() {
      return {
        spaceId: this.spaceId,
        spaceApplications: this.spaceApplications
      };
    },
  },
};
</script>
