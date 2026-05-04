<template>
  <v-hover v-slot="{ hover }">
    <div>
      <v-list-item
        class="ps-0"
        :class="selectedClass"
        selectable
        dense
        @click="$root.$emit('selectGroup', group)">
        <v-list-item-action class="ps-1 my-1 me-0">
          <v-btn
            v-if="displayIcon"
            icon
            @click="toogleOpenTree">
            <v-progress-circular
              v-if="loadingChildren"
              indeterminate
              size="18" />
            <v-icon v-else size="32">{{ icon }}</v-icon>
          </v-btn>
        </v-list-item-action>
        <v-list-item-content>
          <v-list-item-title
            :title="group.label"
            :class="{ 'font-weight-bold': loading >=0 && selected, 'ms-3': !displayIcon }"
            class="text-body text-truncate">
            {{ group.label }}
          </v-list-item-title>
        </v-list-item-content>
        <v-list-item-action class="my-1">
          <v-sheet :min-height="36">
            <groups-management-tree-group-menu v-show="hover" :group="group" />
          </v-sheet>
        </v-list-item-action>
      </v-list-item>
      <div v-if="displayChildren">
        <div class="ms-3 d-flex flex-nowrap">
          <v-pagination
            v-if="pagesCount > 1"
            v-model="page"
            :length="pagesCount"
            :total-visible="5" />
        </div>
        <groups-management-tree-item
          v-for="child in children"
          :key="child.id"
          :group="child"
          :page-size="pageSize"
          :loading="loading"
          :open-all="openAll"
          class="ms-3" />
      </div>
    </div>
  </v-hover>
</template>

<script>
export default {
  props: {
    group: {
      type: Object,
      default: null,
    },
    selectedGroups: {
      type: Array,
      default: null,
    },
    pageSize: {
      type: Number,
      default: 10,
    },
    loading: {
      type: Boolean,
      default: false,
    },
    openAll: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    selected: false,
    open: false,
    page: 1,
  }),
  computed: {
    selectedClass() {
      return this.loading >=0 && this.selected ? 'mdi-menu-right' : 'mdi-menu-down';
    },
    pagesCount() {
      return parseInt((this.childrenSize + this.pageSize - 1) / this.pageSize);
    },
    icon() {
      return this.loading >=0 && this.open ? 'mdi-menu-down' : 'mdi-menu-right';
    },
    loadingChildren() {
      return this.loading && this.group.loading;
    },
    displayChildren() {
      return this.loading >=0 && this.group.children && this.group.children.length && (this.open || this.openAll);
    },
    childrenSize() {
      return this.loading >=0 && this.group.childrenSize;
    },
    children() {
      return this.loading >=0 && this.group.children;
    },
    displayIcon() {
      return this.loading >=0 && !this.openAll && (!this.group.childrenRetrieved || this.group.children);
    },
  },
  watch: {
    open() {
      if (this.open && !this.group.childrenRetrieved) {
        const offset = (this.page - 1) * this.pageSize;
        this.$root.$emit('retrieveGroupChildren', this.group, offset);
      }
    },
    page() {
      const offset = (this.page - 1) * this.pageSize;
      this.$root.$emit('retrieveGroupChildren', this.group, offset);
    },
  },
  created() {
    this.$root.$on('selectGroup', group => {
      this.selected = group && this.group.id === group.id;
    });
    this.$root.$on('searchingGroup', () => {
      this.open = false;
      this.selected = false;
    });
  },
  methods: {
    toogleOpenTree(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.open = !this.open;
    },
  },
};
</script>
