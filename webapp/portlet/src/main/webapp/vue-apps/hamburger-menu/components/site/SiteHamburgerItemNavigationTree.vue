<template>
  <v-list
    dense
    class="pb-0">
    <v-list-item-group>
      <v-treeview
        id="siteHamburgerItemNavigationTree"
        :open.sync="openLevel"
        :items="navigations"
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
            class="d-flex clickable px-0"
            :class="isCurrentNode(item) && ' v-item--active v-list-item--active' || ' '">
            <v-icon
              v-if="item.icon"
              size="20"
              class="icon-default-color mt-1 ms-4"
             :class="isCurrentNode(item) && 'me-2' || ''">
              {{ item.icon }}
            </v-icon>
            <v-list-item-title
              :class="!isCurrentNode(item) && 'ms-2'"
              class="body-2 mt-1">
              {{ item.label }}
            </v-list-item-title>
          </v-list-item>
        </template>
      </v-treeview>
    </v-list-item-group>
  </v-list>
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
  },
  methods: {
    navigationUri(navigation) {
      let url = navigation.pageLink || `/portal/${navigation.siteKey.name}/${navigation.uri}`;
      if (!url.match(/^(https?:\/\/|javascript:|\/portal\/)/)) {
        url = `//${url}`;
      }
      return  url;
    },
    navigationTarget(navigation) {
      return navigation?.target === 'SAME_TAB' && '_self' || '_blank';
    },
    isCurrentNode(navigation) {
      return navigation.uri === this.selectedNodeUri;
    },
  }
};
</script>