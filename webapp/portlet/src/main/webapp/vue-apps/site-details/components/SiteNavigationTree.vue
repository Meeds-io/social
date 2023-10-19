<template>
  <v-treeview
    id="siteNavigationTree"
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
      <site-navigation-item :navigation="item" :enable-change-home="enableChangeHome" />
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
    siteName: {
      type: String,
      default: null,
    },
    enableChangeHome: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    selectedNodeUri: eXo.env.portal.selectedNodeUri,
    currentSite: eXo.env.portal.portalName,
  }),
  computed: {
    openLevel() {
      const ids = [];
      if (this.navigations?.length) {
        this.navigations.forEach(nav => {
          ids.push(nav.name);
          ids.push(...nav.children?.length && nav.children?.map(nav => nav.name) || []);
        });
      }
      return ids;
    },
    active() {
      if (this.siteName !== this.currentSite) {
        return [];
      }
      const splittedCurrentUri = this.selectedNodeUri.split('/');
      return [splittedCurrentUri[splittedCurrentUri.length -1]];
    },
  },
};
</script>