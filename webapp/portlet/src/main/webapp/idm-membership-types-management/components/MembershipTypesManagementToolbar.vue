<template>
  <v-toolbar id="membershipTypesManagementToolbar" flat>
    <v-toolbar-title>
      <v-btn
        class="btn btn-primary addNewMembershipTypeButton"
        @click="$root.$emit('addNewMembershipType')">
        <i class="uiIconSocSimplePlus uiIconWhite me-md-3"></i>
        <span class="d-none d-sm-inline">
          {{ $t('MembershipTypesManagement.addMembershipType') }}
        </span>
      </v-btn>
    </v-toolbar-title>
    <v-spacer />
    <v-scale-transition>
      <v-text-field
        v-model="keyword"
        :placeholder="$t('MembershipTypesManagement.filterBy')"
        prepend-inner-icon="fa-filter"
        class="inputMembershipTypeFilter pa-0 me-3 my-auto" />
    </v-scale-transition>
  </v-toolbar>
</template>

<script>
export default {
  data: () => ({
    keyword: null,
  }),
  watch: {
    keyword() {
      this.$root.$emit('searchMembershipType', this.keyword);
    },
  }
};
</script>
