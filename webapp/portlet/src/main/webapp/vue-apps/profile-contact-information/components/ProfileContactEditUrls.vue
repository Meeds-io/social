<template>
  <profile-contact-edit-multi-field
    :values="value"
    :title="$t('profileContactInformation.url')"
    item-value="url" />
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      default: () => null,
    },
  },
};
</script>