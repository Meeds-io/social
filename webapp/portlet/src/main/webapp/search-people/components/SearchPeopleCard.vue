<template>
  <div>
  </div>
</template>

<script>
export default {
  props: {
    connectors: {
      type: Object,
      default: () => ({}),
    },
  },
  data: () => ({
    dialog: false,
  }),
};
</script>
