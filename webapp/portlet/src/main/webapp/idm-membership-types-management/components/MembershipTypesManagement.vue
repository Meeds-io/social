<template>
  <v-app class="white">
    <v-card flat>
      <membership-types-management-toolbar />
      <membership-types-management-list />
      <membership-types-management-form-drawer />
    </v-card>
  </v-app>
</template>

<script>
export default {
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  }
};
</script>