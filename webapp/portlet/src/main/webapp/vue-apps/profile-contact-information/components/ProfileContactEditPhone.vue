<template>
  <profile-contact-edit-multi-field
    :values="value"
    :title="$t('profileContactInformation.phones')"
    :items="items"
    type="phone"
    item-name="phoneType"
    item-value="phoneNumber" />
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      default: () => null,
    },
  },
  data: () => ({
    items: ['work', 'home', 'other'],
  }),
  created() {
    const extensions = extensionRegistry.loadExtensions('profile-contact', 'edit-phone');
    if (extensions && extensions.length) {
      this.items.push(extensions);
    }
  },
};
</script>