<template>
  <v-data-table
    :headers="headers"
    :items="filteredNavigations"
    :loading="loading"
    :options.sync="options"
    :server-items-length="totalSize"
    :footer-props="{ itemsPerPageOptions }"
    :loading-text="$t('authentication.multifactor.protected.loadingResults')"
    :no-results-text="$t('authentication.multifactor.protected.noResultsFound')"
    :no-data-text="$t('authentication.multifactor.protected.noData')"
    class="data-table-light-border">
    <template slot="item.delete" slot-scope="{ item }">
      <v-btn
        :title="$t('authentication.multifactor.manage.datatable.delete')"
        primary
        icon
        text
        @click="deleteNavigation(item.name)">
        <i class="uiIconTrash trashIconColor"></i>
      </v-btn>
    </template>
  </v-data-table>
</template>

<script>
export default {
  data: () => ({
    itemsPerPageOptions: [20, 50, 100],
    options: {
      page: 1,
      itemsPerPage: 20,
    },
    totalSize: 0,
    loading: false,
    navigationsGroup: [],
  }),
  watch: {
    options() {
      this.protectedNavigationsList();
    },
  },
  computed: {
    filteredNavigations() {
      return this.navigationsGroup.slice();
    },
    headers() {
      return [{
        text: this.$t && this.$t('authentication.multifactor.manage.datatable.navigation'),
        value: 'name',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }, {
        text: this.$t && this.$t('authentication.multifactor.manage.datatable.delete'),
        value: 'delete',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }];
    }
  },
  created() {
    this.protectedNavigationsList();
    this.$root.$on('protectedNavigationsList', this.protectedNavigationsList);
  },
  methods: {
    protectedNavigationsList() {
      let itemsPerPage = this.options && this.options.itemsPerPage;
      if (itemsPerPage <= 0) {
        itemsPerPage = this.totalSize || 20;
      }
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/getProtectedNavigations`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('authentication.multifactor.protected.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(data => {
        this.navigationsGroup = data;
        this.totalSize =  data.length|| 0;
        return this.$nextTick();
      })
        .finally(() => {
          if (!this.initialized) {
            this.$root.$emit('application-loaded');
          }
          this.loading = false;
        });
    },
    deleteNavigation(deletedNavigation) {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/deleteNavigation/${deletedNavigation}`, {
        method: 'DELETE',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp && resp.status === 400) {
            return resp.text().then(error => {
              throw new Error(error);
            });
          } else {
            throw new Error(this.$t('authentication.multifactor.protected.error.UnknownServerError'));
          }
        }
        return this.protectedNavigationsList();
      }).catch(error => {
        this.error = error.message || String(error);
        window.setTimeout(() => {
          this.error = null;
        }, 5000);
      }).finally(() => this.loading = false);
    },
  }
};
</script>
