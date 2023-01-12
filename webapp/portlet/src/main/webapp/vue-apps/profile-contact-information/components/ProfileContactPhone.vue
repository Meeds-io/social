<template>
  <div v-if="user.phones && user.phones.length" id="profileContactPhones">
    <v-divider class="my-4" />
    <v-flex class="d-flex">
      <div class="align-start text-no-wrap font-weight-bold me-3">
        {{ $t('profileContactInformation.phones') }}
      </div>
      <div v-if="user && user.phones && user.phones.length" class="align-end flex-grow-1 text-truncate text-end">
        <div
          v-for="(phone, i) in user.phones"
          :key="i"
          :title="phone.phoneNumber"
          class="text-no-wrap text-truncate profileContactPhone">
          <span class="pe-1 text-capitalize">
            {{ getLabel(`profileContactInformation.phones.${phone.phoneType}`, phone.phoneType) }}:
          </span>
          <span v-autolinker="phone.phoneNumber"></span>
        </div>
      </div>
    </v-flex>
  </div>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
  },
  methods: {
    getLabel(label, defaultValue) {
      const translation = this.$t(label);
      return translation === label && defaultValue || translation;
    }
  },
};
</script>