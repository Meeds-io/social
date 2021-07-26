<template>
  <v-flex id="groupsManagementToolbar" class="d-flex flex-row">
    <v-flex class="mx-3 d-flex flex-grow-0 groupsManagementToolbarButton">
      <v-btn
        class="btn btn-primary my-auto addNewGroupButton"
        @click="$root.$emit('addNewGroup')">
        <i class="uiIconGroup uiIconWhite me-md-3"></i>
        <span class="d-none d-sm-inline">
          {{ $t('GroupsManagement.addGroup') }}
        </span>
      </v-btn>
    </v-flex>
    <v-flex class="mx-3 d-flex flex-column flex-grow-1 groupsManagementToolbarFilter">
      <v-text-field
        v-model="keyword"
        :placeholder="$t('GroupsManagement.filterByGroup')"
        prepend-inner-icon="fa-filter"
        class="inputMembershipTypeFilter pa-0 my-auto" />
      <v-progress-linear
        v-if="searching"
        color="primary"
        class="groupSearchingProgressBar"
        indeterminate
        size="32" />
    </v-flex>
  </v-flex>
</template>

<script>
export default {
  data: () => ({
    keyword: null,
    searching: false,
  }),
  watch: {
    keyword() {
      this.$root.$emit('searchGroup', this.keyword);
    },
  },
  created() {
    this.$root.$on('searchingGroup', searching => this.searching = searching);
  },
};
</script>
