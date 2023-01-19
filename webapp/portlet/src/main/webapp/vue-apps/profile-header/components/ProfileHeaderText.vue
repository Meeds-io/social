<template>
  <v-card
    class="d-flex flex-column"
    color="transparent"
    min-height="70"
    flat
    tile>
    <v-card-title class="py-2 ps-2">
      {{ user && user.fullname }}{{ external }}{{ disabled }}
    </v-card-title>
    <v-card-subtitle class="py-2 ps-2">
      {{ user && user.position || '' }}
    </v-card-subtitle>
  </v-card>
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
