<template>
  <v-card
    id="profileHeaderText"
    class="d-flex"
    color="transparent"
    flat
    tile>
    <div
      v-if="!isWelcomeDisplayOption"
      class="d-flex flex-column">
      <div id="profileHeaderUserFullname" class="line-height-normal text-title text-break text-wrap">
        {{ userFullname }}
      </div>
      <div
        id="profileHeaderUserPrimaryProperty"
        v-if="primaryProperty"
        class="subtitle text-subtitle text-break text-wrap">
        {{ primaryProperty || '' }}
      </div>
    </div>
    <div
      v-else
      class="d-flex text-title text-break text-wrap">
      {{ $t('profileHeader.welcome.label', {0: userFirstName}) }}
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
    displayOption: {
      type: String,
      default: null
    }
  },
  computed: {
    isWelcomeDisplayOption() {
      return this.displayOption === 'welcome';
    },
    userFullname() {
      return this.user?.fullname && `${this.user.fullname}${this.external}${this.disabled}`;
    },
    userFirstName() {
      return `${this.user?.firstname}${this.external}${this.disabled}`;
    },
    primaryProperty() {
      return this.user?.primaryProperty;
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
