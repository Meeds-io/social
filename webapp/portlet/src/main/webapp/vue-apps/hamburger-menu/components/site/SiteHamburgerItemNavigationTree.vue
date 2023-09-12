<template>
  <v-list
    dense
    class="pb-0">
    <v-list-item-group>
      <v-treeview
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
            class="d-flex clickable ms-0"
            :class="item.uri === selectedNodeUri && ' v-item--active v-list-item--active ' || ' '">
            <v-icon
              v-if="item.icon"
              size="24"
              class="icon-default-color mt-1 mx-2">
              {{ item.icon }}
            </v-icon>
            <div v-else class="ms-5 me-2"></div>
            <v-list-item-title
              class="body-2 mx-2 mt-1">
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
      return this.navigations?.length && this.navigations.map(nav => nav.name) || [];
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
  }
};
</script>