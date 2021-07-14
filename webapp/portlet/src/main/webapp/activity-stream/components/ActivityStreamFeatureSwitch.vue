<template>
  <div class="white d-flex flex-column text-center mb-4">
    <v-img
      src="/social-portlet/images/comingSoonV2.png"
      width="190px"
      height="50px"
      max-width="100%"
      class="mx-auto mt-6" />
    <div
      class="v-label font-weight-bold mt-0 mb-6 text-color"
      v-sanitized-html="comingSoonText">
    </div>
  </div>
</template>

<script>
export default {
  computed: {
    comingSoonText() {
      return this.$t('activityStream.comingSoonV2', {
        0: `<a href="${eXo.env.portal.context}/${eXo.env.portal.portalName}/tribe-feedback">`,
        1: '</a>',
      });
    },
  },
};
</script>