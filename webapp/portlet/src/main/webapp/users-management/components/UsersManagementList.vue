<template>
  <v-data-table
    :headers="headers"
    :items="users"
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
    users: [],
    query: null,
    options: {
      page: 1,
      itemsPerPage: 10,
    },
    totalSize: 0,
    loading: true,
  }),
  computed: {
    headers() {
      return [{
        text: this.$t && this.$t('UsersManagement.userName'),
        value: 'userName',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.firstName'),
        value: 'firstName',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.lastName'),
        value: 'lastName',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.email'),
        value: 'email',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.status'),
        value: 'enabled',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.actions'),
        value: '',
        sortable: false,
      }];
    },
  },
  watch: {
    options() {
      this.refreshUsers();
    },
  },
  created() {
    this.refreshUsers();
  },
  methods: {
    refreshUsers() {
      this.loading = true;
      const { page, itemsPerPage } = this.options;
      const offset = (page - 1) * itemsPerPage;
      const limit = offset + itemsPerPage;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users?q=${this.query || ''}&offset=${offset || 0}&limit=${limit|| 0}&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error('Response code indicates a server error', resp);
        } else {
          return resp.json();
        }
      }).then(data => {
        this.users = data && data.entities || [];
        this.totalSize = data && data.size || 0;
      })
        .finally(() => this.loading = false);
    },
  },
};
</script>