<template>
  <div>
    <exo-space-managers-drawer />
    <exo-space-form-drawer />
  </div>
</template>

<script>
export default {
  mounted() {
    document.addEventListener('editSpaceDetail', this.editSpaceDetail);
    document.addEventListener('openSpaceManagerDetail', this.openSpaceManagerDetail);
  },
  methods: {
    editSpaceDetail(space) {
      space = space.detail || space;
      this.$root.$emit('editSpace', space);
    },
    openSpaceManagerDetail(space) {
      space = space.detail || space;
      this.$root.$emit('displaySpaceManagers', space);
    },
  }
};
</script>
