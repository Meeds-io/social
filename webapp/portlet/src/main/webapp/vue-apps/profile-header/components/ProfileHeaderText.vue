<template>
  <v-card
    id="profileHeaderText"
    class="d-flex"
    color="transparent"
    min-height="70"
    flat
    tile>
    <div class="d-flex flex-column my-auto">
      <div id="profileHeaderUserFullname" class="title text-break text-wrap">
        {{ userFullname }}
      </div>
      <div
        id="profileHeaderUserPosition"
        v-if="userPosition"
        class="subtitle text-sub-title text-break text-wrap">
        {{ userPosition || '' }}
      </div>
    </div>
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
