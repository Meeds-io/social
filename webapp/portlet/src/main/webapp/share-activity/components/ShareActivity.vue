<template>
  <v-app>
    <share-activity-drawer v-if="!newActivityStreamDisplayed" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    newActivityStreamDisplayed: false,
  }),
  created() {
    this.newActivityStreamDisplayed = $('#ActivityStream').length;
  },
};
</script>