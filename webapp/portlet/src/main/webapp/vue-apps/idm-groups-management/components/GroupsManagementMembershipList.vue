<template>
  <div class="ms-4 me-7">
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('GroupsManagement.title.confirmDeleteMembership')"
      :ok-label="$t('GroupsManagement.button.ok')"
      :cancel-label="$t('GroupsManagement.button.cancel')"
      @ok="deleteConfirm()" />
    <v-data-table
      :headers="headers"
      :items="filteredMemberships"
      :loading="loading"
      :options.sync="options"
      :footer-props="{ itemsPerPageOptions }"
      :hide-default-footer="!hasPages"
      :server-items-length="totalSize"
      :loading-text="$t('UsersManagement.loadingResults')"
      :no-results-text="$t('UsersManagement.noResultsFound')"
      :no-data-text="$t('UsersManagement.noData')"
      class="data-table-light-border">
      <template slot="item.actions" slot-scope="{ item }">
        <div class="d-flex flex-no-wrap">
          <v-btn
            :title="$t('GroupsManagement.editMembership')"
            primary
            icon
            text
            @click="$root.$emit('editMembership', item, group)">
            <i class="uiIconEdit"></i>
          </v-btn>
          <v-btn
            :title="$t('GroupsManagement.button.delete')"
            primary
            icon
            text
            @click="deleteMembership(item)">
            <i class="uiIconTrash"></i>
          </v-btn>
        </div>
      </template>
    </v-data-table>
  </div>
</template>

<script>
export default {
  data: () => ({
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    itemsPerPageOptions: [20, 50, 100],
    memberships: [],
    selectedMembership: null,
    deleteConfirmMessage: null,
    keyword: null,
    dateTimeFormat: {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    },
    group: null,
    totalSize: 0,
    options: {
      page: 1,
      itemsPerPage: 20,
    },
    loading: false,
  }),
  computed: {
    hasPages() {
      return this.totalSize > this.options.itemsPerPage;
    },
    filteredMemberships() {
      if (this.keyword) {
        return this.memberships.slice()
          .filter(membership => 
            membership
            && (membership.fullName
               && membership.fullName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0)
            || membership.email
               && membership.email.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      } else {
        return this.memberships.slice();
      }
    },
    headers() {
      return [{
        text: this.$t && this.$t('UsersManagement.userName'),
        value: 'userName',
        align: 'center',
        class: 'text-no-wrap',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.lastName'),
        value: 'lastName',
        align: 'center',
        class: 'text-no-wrap',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.firstName'),
        value: 'firstName',
        align: 'center',
        class: 'text-no-wrap',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.email'),
        value: 'email',
        align: 'center',
        class: 'text-no-wrap',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.membershipType'),
        value: 'membershipType',
        align: 'center',
        class: 'text-no-wrap',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.actions'),
        value: 'actions',
        align: 'center',
        class: 'text-no-wrap',
        sortable: false,
      }];
    },
  },
  watch: {
    options() {
      this.searchMemberships();
    },
    keyword() {
      this.options.page = 1;
      if (!this.keyword) {
        return this.searchMemberships();
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loading) {
        this.loading = true;
        this.waitForEndTyping();
      }
    },
    group() {
      this.options.page = 1;
      return this.searchMemberships();
    },
  },
  created() {
    this.$root.$on('searchGroupMemberships', this.updateSearchTerms);
    this.$root.$on('selectGroup', group => this.group = group);
    this.$root.$on('refreshGroupMemberships', () => this.searchMemberships());
  },
  methods: {
    updateSearchTerms(keyword) {
      this.keyword = keyword;
    },
    deleteMembership(membership) {
      this.selectedMembership = membership;
      this.deleteConfirmMessage = this.$t('GroupsManagement.message.confirmDeleteMembership', {0: this.selectedMembership.fullName, 1: this.selectedMembership.membershipType});
      this.$refs.deleteConfirmDialog.open();
    },
    deleteConfirm() {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?membershipId=${this.selectedMembership.id}`, {
        method: 'DELETE',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        }
        return this.searchMemberships();
      })
        .finally(() => this.loading = false);
    },
    searchMemberships(itemsPerPage) {
      if (!this.group) {
        this.memberships = [];
        this.totalSize = 0;
        return;
      }
      const page = this.options && this.options.page;
      itemsPerPage = itemsPerPage || this.options.itemsPerPage;
      const offset = (page - 1) * itemsPerPage;

      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?groupId=${this.group.id}&offset=${offset}&limit=${itemsPerPage}&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(data => {
        this.memberships = data && data.entities || [];
        this.totalSize = data && data.size || 0;

        if (this.keyword) {
          return this.$nextTick().then(() => {
            const totalSize = this.totalSize;
            this.totalSize = this.filteredMemberships.length;
            if (itemsPerPage < totalSize && this.filteredMemberships.length < this.options.itemsPerPage) {
              const limitToFetch = Math.min(itemsPerPage * 2, totalSize);
              return this.searchMemberships(limitToFetch);
            }
          });
        }
      })
        .finally(() => this.loading = false);
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchMemberships();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  },
};
</script>