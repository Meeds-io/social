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
      <v-list-item
        link
        :href="navigationUri(item)"
        :target="navigationTarget(item)"
        class="d-flex px-0"
        :class="isGroupNode(item) && ' ' || 'clickable'">
        <v-icon
          v-if="item.icon"
          size="20"
          class="icon-default-color mt-1 mx-2">
          {{ item.icon }}
        </v-icon>
        <v-list-item-title class="body-2 mt-1">
          {{ item.label }}
        </v-list-item-title>
      </v-list-item>
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
  methods: {
    navigationUri(navigation) {
      if (this.isGroupNode(navigation)) {
        return '';
      }
      let url = navigation.pageLink || `/portal/${navigation.siteKey.name}/${navigation.uri}`;
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return url;
    },
    navigationTarget(navigation) {
      return navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    isGroupNode(navigation) {
      return !navigation.pageKey;
    },
  }
};
</script>