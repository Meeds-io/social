<template>
  <div>
    <exo-confirm-dialog
      ref="deleteNavigationWarningDialog"
      :message="deleteConfirmMessage"
      :title="$t('authentication.multifactor.protected.confirmDelete')"
      :ok-label="$t('authentication.multifactor.protected.button.ok')"
      :cancel-label="$t('UsersManagement.button.cancel')"
      @ok="deleteNavigation()" />
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
          @click="deleteNavigationConfirm(item.id)">
          <i class="uiIconTrash trashIconColor"></i>
        </v-btn>
      </template>
    </v-data-table>
  </div>
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
    selectedNavigationsGroup: null,
    deleteConfirmMessage: null,
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
        value: 'id',
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
        const navs = data;
        this.navigationsGroup = navs;
        this.totalSize =  navs.length|| 0;
        return this.$nextTick();
      })
        .finally(() => {
          if (!this.initialized) {
            this.$root.$emit('application-loaded');
          }
          this.loading = false;
        });
    },
    deleteNavigationConfirm(deletedNavigation){
      this.selectedNavigationsGroup = deletedNavigation;
      this.deleteConfirmMessage = this.$t('authentication.multifactor.protected.title.confirmDelete', {0: deletedNavigation});
      this.$refs.deleteNavigationWarningDialog.open();
    },
    deleteNavigation() {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/mfa/deleteNavigation`, {
        method: 'DELETE',
        credentials: 'include',
        body: this.selectedNavigationsGroup,
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
