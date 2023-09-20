<template>
  <v-treeview
    id="siteHamburgerItemNavigationTree"
    :open.sync="openLevel"
    :items="navigations"
    :active="active"
    active-class="v-item--active v-list-item--active"
    class="treeView-item my-2"
    item-key="name"
    hoverable
    activatable
    open-on-click
    transition>
    <template #label="{ item }">
      <site-hamburger-item-navigation :navigation="item" />
    </template>
  </v-treeview>
</template>

<script>
export default {
  props: {
    navigations: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    selectedNodeUri: eXo.env.portal.selectedNodeUri
  }),
  computed: {
    openLevel() {
      const ids = [];
      if (this.navigations?.length) {
        this.navigations.forEach(nav => {
          ids.push(nav.name);
          ids.push(...nav.children.length && nav.children.map(nav => nav.name) || []);
        });
      }
      return ids;
    },
    active() {
      const splittedCurrentUri = this.selectedNodeUri.split('/');
      return [splittedCurrentUri[splittedCurrentUri.length -1]];
    },
  },
};
</script>