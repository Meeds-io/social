<template>
  <v-flex>
    <v-card-title class="headline white--text">
      {{ user && user.fullname }}{{ user && user.external && (' (' + $t('profileHeader.label.external') + ')') || '' }}{{ user && !user.enabled && (' (' + $t('profileHeader.label.disabled') + ')') || '' }}
    </v-card-title>
    <v-card-subtitle class="subtitle white--text" dark>
      {{ user && user.position || '' }}
    </v-card-subtitle>
  </v-flex>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
  },
};
</script>
