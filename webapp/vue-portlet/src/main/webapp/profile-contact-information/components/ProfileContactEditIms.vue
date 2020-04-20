<template>
  <profile-contact-edit-multi-field
    :values="value"
    :title="$t('profileContactInformation.ims')"
    :items="items"
    type="im"
    item-name="imType"
    item-value="imId" />
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
    items: ['skype', 'msn', 'facebook', 'github', 'other'],
  }),
  created() {
    const extensions = extensionRegistry.loadExtensions('profile-contact', 'edit-ims');
    if (extensions && extensions.length) {
      this.items.push(extensions);
    }
  },
};
</script>