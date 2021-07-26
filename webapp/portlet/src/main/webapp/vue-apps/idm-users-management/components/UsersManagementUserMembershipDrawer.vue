<template>
  <exo-drawer
    id="userMembershipDrawer"
    ref="userMembershipDrawer"
    right
    @closed="drawer = false">
    <template slot="title">
      <div
        :title="title"
        class="text-truncate">
        {{ title }}
      </div>
    </template>
    <template slot="content">
      <v-data-table
        :headers="headers"
        :items="membershipsArray"
        :loading="loading"
        :loading-text="$t('UsersManagement.loadingResults')"
        :no-results-text="$t('UsersManagement.noResultsFound')"
        :no-data-text="$t('UsersManagement.noData')"
        hide-default-header
        hide-default-footer
        class="data-table-light-border">
        <template slot="item.membershipType" slot-scope="{ item }">
          {{ item.membershipType }}
        </template>
        <template slot="item.groupId" slot-scope="{ item }">
          <v-list-item-content>
            <v-list-item-title>{{ item.groupLabel }}</v-list-item-title>
            <v-list-item-subtitle class="caption text-sub-title">{{ item.groupId }}</v-list-item-subtitle>
          </v-list-item-content>
        </template>
      </v-data-table>
    </template>
    <template v-if="hasPagination" slot="footer">
      <div class="d-flex justify-center">
        <v-pagination
          v-model="page"
          :length="pagesCount"
          circle
          light
          flat
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
    title() {
      return this.$t('UsersManagement.button.membershipsOfUser', {0: this.user.fullname});
    },
    userName() {
      return this.user && this.user.userName;
    },
    membershipsArray() {
      return this.memberships && this.memberships.entities || [];
    },
    hasPagination() {
      return this.memberships.size > this.pageSize;
    },
    pagesCount() {
      return parseInt((this.memberships.size + this.pageSize - 1) / this.pageSize);
    },
    headers() {
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
    loading() {
      if (this.loading) {
        this.$refs.userMembershipDrawer.startLoading();
      } else {
        this.$refs.userMembershipDrawer.endLoading();
      }
    },
    page() {
      this.refresh();
    },
    drawer() {
      if (this.drawer) {
        this.$refs.userMembershipDrawer.open();
      } else {
        this.$refs.userMembershipDrawer.close();
      }
    },
  },
  created() {
    this.$root.$on('openUserMemberships', this.open);
  },
  methods: {
    open(user) {
      this.user = user;
      this.refresh();
      this.drawer = true;
    },
    refresh() {
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
    }
  },
};
</script>