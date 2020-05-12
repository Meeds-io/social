<template>
  <v-app
    class="transparent"
    flat>
    <space-setting-general
      :space-id="spaceId"
      :skeleton="skeleton"
      class="mb-6" />
    <space-setting-applications
      :space-id="spaceId"
      :skeleton="skeleton" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
    skeleton: true,
  }),
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
    this.skeleton = false;
  },
};
</script>