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
  computed: {
    items() {
      const items = this.$root.imTypes.slice().filter(item => item !== 'other').sort();
      items.push('other');
      return items;
    },
  },
  created() {
    const extensions = extensionRegistry.loadExtensions('profile-contact', 'edit-ims');
    if (extensions && extensions.length) {
      this.items.push(extensions);
    }
  },
};
</script>