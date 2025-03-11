<template>
  <v-toolbar 
    id="groupsManagementToolbar" 
    class="transparent"
    flat>
    <v-toolbar-title>
      <v-btn
        class="btn addNewMembershipButton"
        :disabled="!group"
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
        class="inputMembershipTypeFilter pa-0 me-3 my-auto"
        :disabled="!group"
        :placeholder="$t('GroupsManagement.filterBy')"
        prepend-inner-icon="fa-filter" />
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
      keyword () {
        this.$root.$emit('searchGroupMemberships', this.keyword);
      },
    },
    created () {
      this.$root.$on('selectGroup', group => this.group = group);
    },
  };
</script>
