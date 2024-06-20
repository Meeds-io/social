<template>
  <v-app
    v-if="displayed"
    id="SpaceSettings"
    class="transparent"
    flat>
    <div class="application-body">
      <space-setting-general :space-id="spaceId" />
      <template>
        <extension-registry-components
          :key="spaceApplications"
          :params="extensionParams"
          name="SpaceSettings"
          type="space-settings-components"
          parent-element="div"
          element="div"
          class="my-6"
          element-class="mb-6" />
      </template>
    </div>
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
