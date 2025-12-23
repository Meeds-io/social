<template>
  <v-hover v-model="hover">
  <widget-wrapper
    :title="headerTitle"
    ref="parentSpaceListing"
    key="parentSpaceListing"
    extra-class="application-body">
    <template #default>
      <div class="mt-n3">
        <space-avatar
          :space="space"
          link-style
          bold-title />
      </div>
    </template>
  </widget-wrapper>
  </v-hover>
</template>
<script>
export default {
  data: () => ({
    hover: false,
  }),
  computed: {
    space() {
      return this.$root.space;
    },
    headerTitle() {
      return this.$t('parentSpaceListing.header.label');
    }
  },
};
</script>

