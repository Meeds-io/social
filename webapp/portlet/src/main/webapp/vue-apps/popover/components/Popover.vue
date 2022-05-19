<template>
  <div>
    <popover-user />
  </div>
</template>
<script>
export default {
  mounted() {
    this.$root.$applicationLoaded();
  },
};
</script>