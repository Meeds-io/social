<template>
  <v-flex class="d-flex">
    <div class="align-start text-no-wrap font-weight-bold mr-3">
      {{ $t('profileContactInformation.ims') }}
    </div>
    <div v-if="user && user.ims && user.ims.length" class="align-end flex-grow-1 text-truncate text-end">
      <div
        v-for="(iM, i) in user.ims"
        :key="i"
        :title="iM.imId"
        class="text-no-wrap text-truncate">
        <span class="pr-1 text-capitalize">
          {{ getLabel(`profileContactInformation.phone.${iM.imType}`, iM.imType) }}:
        </span>
        {{ iM.imId }}
      </div>
    </div>
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
  methods: {
    getLabel(label, defaultValue) {
      const translation = this.$t(label);
      return translation === label && defaultValue || translation;
    }
  },
};
</script>