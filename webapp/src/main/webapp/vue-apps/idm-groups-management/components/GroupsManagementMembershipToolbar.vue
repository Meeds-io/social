<template>
  <v-toolbar 
    id="groupsManagementToolbar" 
    flat
    class="transparent">
    <v-toolbar-title>
      <v-btn
        :disabled="!group"
        class="btn addNewMembershipButton"
        @click="$root.$emit('addNewMembership', group)">
        <i class="uiIconSocConnectUser me-md-3"></i>
        <span class="d-none d-sm-inline">
          {{ $t('GroupsManagement.addMemberInGroup') }}
        </span>
      </v-btn>
    </v-toolbar-title>
    <v-spacer />
    <v-scale-transition>
      <v-text-field
        v-model="keyword"
        :placeholder="$t('GroupsManagement.filterBy')"
        :disabled="!group"
        prepend-inner-icon="fa-filter"
        class="inputMembershipTypeFilter pa-0 me-3 my-auto" />
    </v-scale-transition>
  </v-toolbar>
</template>

<script>
export default {
  data: () => ({
    group: null,
    keyword: null,
  }),
  watch: {
    keyword() {
      this.$root.$emit('searchGroupMemberships', this.keyword);
    },
  },
  created() {
    this.$root.$on('selectGroup', group => this.group = group);
  },
};
</script>
