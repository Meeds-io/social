<template>
  <div>
    <v-divider v-if="user.ims.length>0" class="my-4"/>
    <v-flex v-if="user.ims.length>0" class="d-flex">
      <div
        :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius'"
        class="align-start text-no-wrap font-weight-bold mr-3">
        {{ $t('profileContactInformation.ims') }}
      </div>
      <div
        v-if="skeleton"
        class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
        &nbsp;&nbsp;&nbsp;&nbsp;
      </div>
      <div v-else-if="user && user.ims && user.ims.length" class="align-end flex-grow-1 text-truncate text-end">
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
  </div>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    skeleton: {
      type: Boolean,
      default: false,
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