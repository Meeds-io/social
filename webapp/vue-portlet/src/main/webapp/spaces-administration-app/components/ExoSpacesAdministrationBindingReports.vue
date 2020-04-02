<template>
  <div id="bindingReports">
    <div v-show="loading">
      <v-skeleton-loader
        class="mx-auto"
        type="table-heading,table-tbody">
      </v-skeleton-loader>
    </div>
    <div v-show="!loading">
      <v-card-title>
        <v-layout justify-space-between>
          <v-flex xs7></v-flex>
          <v-flex xs2>
            <v-text-field
              v-model="search"
              append-icon="search"
              single-line
              solo
              flat
              hide-details>
            </v-text-field>
          </v-flex>
          <v-flex xs2>
            <v-select
              :items="operationTypes"
              label="All bindings"
              solo
              flat
            ></v-select>
          </v-flex>          
        </v-layout>
      </v-card-title>
      <v-data-table
        :headers="headers"
        :items="actions"
        :search="search"
        disable-sort
      ></v-data-table>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      search: '',
      operationTypes: [
        `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`, 
        `${this.$t('social.spaces.administration.binding.reports.filter.new.binding')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.remove.binding')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.synchronization')}`
      ],
      actions: [],
      headers: [
        { text: `${this.$t('social.spaces.administration.manageSpaces.space')}`,value: 'space' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.group')}`, value: 'group' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.start.date')}`, value: 'startDate' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.end.date')}`, value: 'endDate' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.operation.type')}`, value: 'operationType' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.added.users')}`, value: 'addedUsers' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.removed.users')}`, value: 'removedUsers' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.File')}`, value: 'file' },
      ],
    };
  }
};
</script>