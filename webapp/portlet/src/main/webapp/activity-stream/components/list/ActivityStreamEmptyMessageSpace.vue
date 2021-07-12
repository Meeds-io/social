<template>
  <div class="d-flex flex-column white border-radius">
    <v-img
      src="/social-portlet/activity-stream/emptySpaceStream.png"
      height="380"
      max-height="380"
      min-height="380"
      contain
      eager
      class="mx-auto" />
    <div class="mx-4 mb-4">
      <p v-sanitized-html="welcomeTitle"></p>
      <div v-sanitized-html="welcomeSpaceParagraph1" class="mb-4"></div>
      <div v-sanitized-html="welcomeSpaceParagraph2" class="mb-4"></div>
      <div v-sanitized-html="conclusionParagraph"></div>
    </div>
  </div>
</template>
<script>
export default {
  computed: {
    welcomeTitle() {
      return this.$t && this.$t('UIActivity.label.Welcome_Space', {
        'space name': `<strong>${eXo.env.portal.spaceDisplayName}</strong>`,
      });
    },
    welcomeSpaceParagraph1() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_one');
    },
    welcomeSpaceParagraph2() {
      return this.$t && this.$t('UIActivity.label.empty_space_stream_paragraph_two');
    },
    conclusionParagraph() {
      return this.$t && this.$t('UIActivity.label.empty_stream_lets_begin');
    },
  },
};
</script>