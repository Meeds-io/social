<template>
  <v-data-table
    :headers="headers"
    :loading="loading"
    :options.sync="options"
    :server-items-length="totalSize"
    :footer-props="{ itemsPerPageOptions }"
    :loading-text="$t('authentication.multifactor.protected.loadingResults')"
    :no-results-text="$t('authentication.multifactor.protected.noResultsFound')"
    :no-data-text="$t('authentication.multifactor.protected.noData')"
    class="data-table-light-border">
    <template slot="item.edit" slot-scope="{ item }">
      <span v-exo-tooltip.bottom.body="$t('authentication.multifactor.manage.datatable.edit')">
        <v-btn
          icon
          text
          @click="$root.$emit('edit', item)">
          <i class="uiIconEdit"></i>
        </v-btn>
      </span>
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
  }),
  computed: {
    headers() {
      return [{
        text: this.$t && this.$t('authentication.multifactor.manage.datatable.navigation'),
        value: 'navigation',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }, {
        text: this.$t && this.$t('authentication.multifactor.manage.datatable.details'),
        value: 'details',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }, {
        text: this.$t && this.$t('authentication.multifactor.manage.datatable.security'),
        value: 'security',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }, {
        text: this.$t && this.$t('authentication.multifactor.manage.datatable.edit'),
        value: 'edit',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }];
    }
  },
};
</script>
