<template>
  <v-container>
    <v-card elevation="0">
      <row
        class="px-4"
        v-if="!displaySequentially"
        @click="$emit('close')">
        <v-icon
          v-if="$root.ltr"
          class="fas fa-arrow-left mt-4"
          small />
        <v-icon
          v-else
          class="fas fa-arrow-right mt-4"
          small />
      </row>
      <v-card-title :title="site?.name" class="text-capitalize font-weight-bold text-subtitle-1">
        {{ site?.displayName }}
      </v-card-title>
      <v-card-subtitle v-sanitized-html="site?.description" class="text-subtitle-2 py-2 text-color rich-editor-content" />
      <site-hamburger-item-navigation-tree v-if="site?.siteNavigations?.length" :navigations="site.siteNavigations" />
    </v-card>
  </v-container>
</template>

<script>
export default {
  props: {
    site: {
      type: Object,
      default: null
    },
    displaySequentially: {
      type: Boolean,
      default: false,
    },
  },
};
</script>