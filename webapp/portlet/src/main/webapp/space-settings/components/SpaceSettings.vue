<template>
  <v-app class="transparent" flat>
    <space-setting-general :space-id="spaceId" class="mb-6" />
    <space-setting-applications :space-id="spaceId" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    spaceId: eXo.env.portal.spaceId,
  }),
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
};
</script>