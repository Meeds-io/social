<template>
  <exo-drawer
    v-model="hamburgerMenu"
    :drawer-width="drawerWidth"
    :style="hamburgerMenuStyle"
    left
    eager>
    <template #content>
      <slot></slot>
    </template>
  </exo-drawer>
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
    hamburgerMenuStyle: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    hamburgerMenu: false,
  }),
  watch: {
    hamburgerMenu() {
      if (this.value !== this.hamburgerMenu) {
        this.$emit('input', this.hamburgerMenu);
      }
    },
    value() {
      if (this.value !== this.hamburgerMenu) {
        this.hamburgerMenu = this.value;
      }
    },
  }
};
</script>
