<template>
  <div class="d-flex flex-nowrap align-center">
    <v-avatar
      :size="iconSize"
      class="flex-grow-0 flex-shrink-0 me-2"
      tile>
      <img
        v-if="iconUrl"
        :src="iconUrl"
        :alt="name"
        width="auto"
        height="auto">
      <v-icon v-else :size="iconSize">fa-globe</v-icon>
    </v-avatar>
    <div class="d-flex flex-grow-1 flex-shrink-1 text-truncate-2 text-color me-2">
      {{ name || url }}
    </div>
    <div class="d-flex flex-grow-0 flex-shrink-0 align-center">
      <div class="d-flex flex-column me-2">
        <v-btn
          v-if="!first"
          icon
          x-small
          @click="moveTop">
          <v-icon size="16">fa-arrow-up</v-icon>
        </v-btn>
        <v-btn
          v-if="!last"
          icon
          x-small
          @click="moveDown">
          <v-icon size="16">fa-arrow-down</v-icon>
        </v-btn>
      </div>
      <v-btn
        icon
        small
        @click="edit">
        <v-icon size="16">fa-edit</v-icon>
      </v-btn>
      <v-btn
        small
        icon
        @click="remove">
        <v-icon size="16">fa-trash</v-icon>
      </v-btn>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    link: {
      type: Object,
      default: null,
    },
    first: {
      type: Boolean,
      default: false,
    },
    last: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    iconSize: 36,
  }),
  computed: {
    name() {
      return this.link?.name
          && (this.link?.name[this.$root.language] || this.link?.name[this.$root.defaultLanguage]);
    },
    url() {
      return this.link?.url;
    },
    iconUrl() {
      if (this.link?.iconSrc) {
        return this.$utils.convertImageDataAsSrc(this.link.iconSrc);
      } else {
        return this.link?.iconUrl;
      }
    },
  },
  methods: {
    moveTop() {
      this.$emit('move-top');
    },
    moveDown() {
      this.$emit('move-down');
    },
    edit() {
      this.$emit('edit');
    },
    remove() {
      this.$emit('remove');
    },
  },
};
</script>