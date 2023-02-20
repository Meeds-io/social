<template>
  <v-menu
    :value="true"
    :absolute="false"
    :close-on-click="false"
    :close-on-content-click="false"
    :content-class="`position-relative overflow-hidden elevation-0 fill-height ${extraClass}`"
    :min-width="drawerWidth"
    max-width="none"
    attach="#ParentSiteLeftContainer"
    eager>
    <slot></slot>
  </v-menu>
</template>
<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    drawerWidth: {
      type: String,
      default: null,
    },
    levelsOpened: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    open: false,
    extraClass: '',
  }),
  watch: {
    levelsOpened() {
      if (this.levelsOpened) {
        this.extraClass = 'z-index-drawer';
      } else {
        window.setTimeout(() => {
          this.extraClass = '';
        }, 300);
      }
    },
    open() {
      if (this.value !== this.open) {
        this.$emit('input', this.open);
      }
    },
    value() {
      if (this.value !== this.open) {
        this.open = this.value;
      }
    },
  }
};
</script>
