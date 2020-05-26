<template>
  <v-data-table
    :headers="headers"
    :items="filteredUsers"
    :items-per-page="10"
    :loading="loading"
    :options.sync="options"
    :server-items-length="totalSize"
    class="elevation-1">
  </v-data-table>
</template>

<script>
export default {
  data: () => ({
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    users: [],
    keyword: null,
    filter: 'ANY',
    options: {
      page: 1,
      itemsPerPage: 10,
    },
    totalSize: 0,
    loading: true,
  }),
  computed: {
    filteredUsers() {
      if (!this.keyword || !this.loading) {
        return this.users.slice();
      } else {
        return this.users.slice()
          .filter(user => 
            user.userName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || user.firstName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || user.lastName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || user.email.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    },
    headers() {
      return [{
        text: this.$t && this.$t('UsersManagement.userName'),
        value: 'userName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.firstName'),
        value: 'firstName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.lastName'),
        value: 'lastName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.email'),
        value: 'email',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.status'),
        value: 'statusLabel',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.actions'),
        value: '',
        align: 'center',
        sortable: false,
      }];
    },
  },
  watch: {
    options() {
      this.searchUsers();
    },
    filter() {
      this.searchUsers();
    },
    keyword() {
      if (!this.keyword) {
        this.searchUsers();
        return;
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loading) {
        this.loading = true;
        this.waitForEndTyping();
      }
    },
  },
  created() {
    this.$root.$on('searchUser', this.updateSearchTerms);

    this.searchUsers();
  },
  methods: {
    updateSearchTerms(keyword, filter) {
      this.keyword = keyword;
      this.filter = filter;
    },
    searchUsers() {
      this.loading = true;
      const page = this.options && this.options.page;
      let itemsPerPage = this.options && this.options.itemsPerPage;
      if (itemsPerPage <= 0) {
        itemsPerPage = this.totalSize || 10;
      }
      const offset = (page - 1) * itemsPerPage;
      const limit = offset + itemsPerPage;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users?q=${this.keyword || ''}&status=${this.filter || 'ANY'}&offset=${offset || 0}&limit=${limit|| 0}&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error('Response code indicates a server error', resp);
        } else {
          return resp.json();
        }
      }).then(data => {
        const entities = data && data.entities || [];
        entities.forEach(user => {
          user.statusLabel = user.enabled ? this.$t('UsersManagement.status.enabled') : this.$t('UsersManagement.status.disabled');
        });
        this.users = entities;
        this.totalSize = data && data.size || 0;
      })
        .finally(() => this.loading = false);
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchUsers();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
  },
};
</script>