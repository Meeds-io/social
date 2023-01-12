<template>
  <profile-contact-edit-multi-field
    :values="value"
    :title="$t('profileContactInformation.urls')"
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