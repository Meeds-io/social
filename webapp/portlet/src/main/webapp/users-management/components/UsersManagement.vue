<template>
  <v-app class="white">
    <users-management-toolbar />
    <users-management-list />
    <users-management-user-form-drawer />
  </v-app>
</template>

<script>
export default {
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  }
};
</script>