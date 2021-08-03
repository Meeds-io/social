<template>
  <v-flex>
    <v-card-title class="headline white--text">
      {{ user && user.fullname }}{{ external }}{{ disabled }}
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
  computed: {
    external(){
      if (this.user && this.user.external === 'true') {
        const external = this.$t('profileHeader.label.external') ;
        return ` (${external}) `;
      } else {
        return '';
      }
    },
    disabled(){
      if (this.user && !this.user.enabled) {
        const disabled = this.$t('profileHeader.label.disabled') ;
        return ` (${disabled}) `;
      } else {
        return '';
      }
    },
  }
};
</script>
