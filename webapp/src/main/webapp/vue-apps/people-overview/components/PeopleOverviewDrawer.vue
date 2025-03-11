<template>
  <exo-drawer
    ref="overviewDrawer"
    body-classes="hide-scroll decrease-z-index-more"
    right>
    <template #title>
      {{ title }}
    </template>
    <template #content>
      <v-layout
        class="ma-3"
        column>
        <people-overview-people-list
          v-if="users && users.length"
          class="ma-0 border-box-sizing"
          :filter="filter"
          :users="users"
          @refresh="refresh()" />
        <template v-else-if="!loadingUsers">
          <span class="ma-auto">{{ $t('peopleOverview.label.noResults') }}</span>
        </template>
        <v-card-actions class="flex-grow-1 justify-center my-2">
          <v-spacer />
          <v-btn
            v-if="canShowMore"
            class="loadMoreButton ma-auto btn"
            :disabled="loadingUsers"
            :loading="loadingUsers"
            @click="loadNextPage">
            {{ $t('peopleOverview.label.showMore') }}
          </v-btn>
          <v-spacer />
        </v-card-actions>
      </v-layout>
    </template>
  </exo-drawer>
</template>

<script>
  export default {
    data: () => ({
      title: null,
      filter: null,
      fieldsToRetrieve: 'all,connectionsInCommonCount',
      loadingUsers: false,
      offset: 0,
      pageSize: 20,
      users: [],
    }),
    computed: {
      canShowMore () {
        return this.loadingUsers || this.users.length >= this.limit;
      },
      searchUsersMethod () {
        return this.filter === 'invitations' ? this.$userService.getInvitations : this.$userService.getPending;
      },
    },
    methods: {
      refresh () {
        this.$emit('refresh');
        this.searchUsers();
      },
      searchUsers () {
        this.users = [];
        this.loadingUsers = true;
        return this.searchUsersMethod(this.offset, this.limit, this.fieldsToRetrieve)
          .then(data => {
            this.users = data && data.users || [];
            return this.$nextTick();
          })
          .finally(() => this.loadingUsers = false);
      },
      loadNextPage () {
        this.limit += this.pageSize;
        this.searchUsers();
      },
      open (filter, title) {
        this.title = title;
        this.limit = this.pageSize;
        this.filter = filter;
        this.$nextTick().then(() => {
          this.searchUsers();
          this.$refs.overviewDrawer.open();
        });
      },
    },
  };
</script>