<template>
  <v-card
    class="d-flex flex-column"
    color="transparent"
    min-height="70"
    flat
    tile>
    <v-card-title class="ps-2 py-0 my-auto">
      {{ userFullname }}
    </v-card-title>
    <v-card-subtitle v-if="userPosition" class="pb-0 pt-2 ps-2 mb-auto">
      {{ userPosition || '' }}
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
    userFullname() {
      return this.user?.fullname && `${this.user.fullname}${this.external}${this.disabled}`;
    },
    userPosition() {
      return this.user?.position;
    },
    external() {
      if (this.user && this.user.external === 'true') {
        const external = this.$t('profileHeader.label.external') ;
        return ` (${external}) `;
      } else {
        return '';
      }
    },
    disabled() {
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
