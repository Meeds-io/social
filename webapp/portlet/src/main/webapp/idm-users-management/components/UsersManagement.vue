<template>
  <v-app class="white">
    <v-card flat>
      <users-management-toolbar />
      <users-management-list />
      <users-management-user-form-drawer />
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