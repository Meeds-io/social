<template>
  <exo-drawer
    id="userMembershipDrawer"
    ref="userMembershipDrawer"
    right
    @closed="drawer = false">
    <template #title>
      <div
        class="text-truncate"
        :title="title">
        {{ title }}
      </div>
    </template>
    <template #content>
      <v-data-table
        class="data-table-light-border"
        :headers="headers"
        hide-default-footer
        hide-default-header
        :items="membershipsArray"
        :loading="loading"
        :loading-text="$t('UsersManagement.loadingResults')"
        :no-data-text="$t('UsersManagement.noData')"
        :no-results-text="$t('UsersManagement.noResultsFound')">
        <template
          #item.membershipType="{ item }">
          {{ item.membershipType }}
        </template>
        <template
          #item.groupId="{ item }">
          <v-list-item-content>
            <v-list-item-title>{{ item.groupLabel }}</v-list-item-title>
            <v-list-item-subtitle>{{ item.groupId }}</v-list-item-subtitle>
          </v-list-item-content>
        </template>
      </v-data-table>
    </template>
    <template
      v-if="hasPagination"
      #footer>
      <div class="d-flex justify-center">
        <v-pagination
          v-model="page"
          circle
          flat
          :length="pagesCount"
          light
          @input="changePage" />
      </div>
    </template>
  </exo-drawer>
</template>

<script>
  export default {
    data: () => ({
      drawer: false,
      loading: false,
      saving: false,
      pageSize: 10,
      page: 1,
      offset: 0,
      limit: 5,
      memberships: {},
      user: {},
    }),
    computed: {
      title () {
        return this.$t('UsersManagement.button.membershipsOfUser', { 0: this.user.fullname });
      },
      userName () {
        return this.user && this.user.userName;
      },
      membershipsArray () {
        return this.memberships && this.memberships.entities || [];
      },
      hasPagination () {
        return this.memberships.size > this.pageSize;
      },
      pagesCount () {
        return parseInt((this.memberships.size + this.pageSize - 1) / this.pageSize);
      },
      headers () {
        return [{
          text: this.$t && this.$t('UsersManagement.membershipType'),
          value: 'membershipType',
          align: 'center',
          sortable: false,
        }, {
          text: this.$t && this.$t('UsersManagement.group'),
          value: 'groupId',
          align: 'left',
          sortable: false,
        }];
      },
    },
    watch: {
      loading () {
        if (this.loading) {
          this.$refs.userMembershipDrawer.startLoading();
        } else {
          this.$refs.userMembershipDrawer.endLoading();
        }
      },
      page () {
        this.refresh();
      },
      drawer () {
        if (this.drawer) {
          this.$refs.userMembershipDrawer.open();
        } else {
          this.$refs.userMembershipDrawer.close();
        }
      },
    },
    created () {
      this.$root.$on('openUserMemberships', this.open);
    },
    methods: {
      open (user) {
        this.user = user;
        this.refresh();
        this.drawer = true;
      },
      refresh () {
        const offset = (this.page - 1) * this.pageSize;

        this.loading = true;
        return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${this.userName}/memberships?offset=${offset}&limit=${this.pageSize}&returnSize=true`, {
          method: 'GET',
          credentials: 'include',
        }).then(resp => {
          if (!resp || !resp.ok) {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          } else {
            return resp.json();
          }
        }).then(memberships => this.memberships = memberships)
          .finally(() => this.loading = false);
      },
    },
  };
</script>