<template>
  <v-app>
    <users-visibility/>
    <document-sharing-permissions/>
  </v-app>
</template>

<script>
export default {
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  }
};
</script>
