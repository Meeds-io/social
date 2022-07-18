<template>
  <div class="d-flex flex-column white border-radius">
    <v-flex class="d-flex my-auto border-box-sizing">
      <div class="d-flex flex-column ma-auto py-10 text-center text-sub-title">
        <img
          width="70px"
          height="70px"
          :src="emptyStreamImage"
          :alt="streamFilter"
          class="mx-auto mt-8"
          loading="lazy">
        <span class="text-sub-title my-7">{{ emptyStreamLabel }}</span>
      </div>
    </v-flex>
  </div>
</template>
<script>
export default {
  props: {
    streamFilter: {
      type: String,
      default: null,
    },
  },
  computed: {
    emptyStreamLabel() {
      return this.$t(`activity.filter.empty_${this.streamFilter}.msg`);
    },
    emptyStreamImage() {
      return `/social-portlet/images/empty_${this.streamFilter}.png`;
    },
  }
};
</script>