<template>
  <v-list
    v-if="menuItem && menuItem.label && menuItem.key && menuItem.children && menuItem.children.length"
    dense
    rounded
    class="adminMenu contentMenuWrapper py-0">
    <v-subheader class="text-uppercase subtitle-2 font-weight-bold">{{ menuItem.label }}</v-subheader>
    <v-treeview
      :items="menuItem.children"
      item-children="children"
      item-key="name"
      item-text="label"
      dense
      rounded
      hoverable
      open-all
      class="subItemTitle">
      <template slot="label" slot-scope="{ item }">
        <a v-if="item && item.link" :href="item.link">{{ item.label }}</a>
        <span v-else-if="item">{{ item.label }}</span>
        <span v-else>{{ item }}</span>
      </template>
    </v-treeview>
  </v-list>
</template>

<script>
export default {
  name: 'AdministrationMenuItemApp',
  props: {
    menuItem: {
      type: Object,
      default: null
    },
  },
};
</script>