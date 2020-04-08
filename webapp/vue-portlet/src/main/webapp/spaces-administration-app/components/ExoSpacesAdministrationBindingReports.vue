<template>
  <div id="bindingReports">
    <div v-if="loading">
      <v-skeleton-loader
        class="mx-auto"
        type="table-heading,table-tbody">
      </v-skeleton-loader>
    </div>
    <div v-else>
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
        :items="operations"
        :search="search"
        disable-sort
      >
        <template v-slot:item.space.displayName="{ item }">
          <div>
            <!--            <img v-if="getSpaceAvatar(item) != null" :src="getSpaceAvatar(item)" class="avatar" />-->
            <!--            <img v-else :src="avatar" class="avatar" />-->
            {{ item }}
          </div>          
        </template>
      </v-data-table>
    </div>
  </div>
</template>

<script>
import * as spacesAdministrationServices from '../spacesAdministrationServices';
import {spacesConstants} from '../../js/spacesConstants';  

export default {
  data() {
    return {
      loading: true,
      avatar: spacesConstants.DEFAULT_SPACE_AVATAR,
      search: '',
      operationTypes: [
        `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`, 
        `${this.$t('social.spaces.administration.binding.reports.filter.new.binding')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.remove.binding')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.synchronization')}`
      ],
      operations: [],
      spaces: [],
      headers: [
        { text: `${this.$t('social.spaces.administration.manageSpaces.space')}`, align: 'center',value: 'space.displayName' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.group')}`, align: 'center', value: 'group' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.start.date')}`, align: 'center', value: 'startDate' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.end.date')}`, align: 'center', value: 'endDate' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.operation.type')}`, align: 'center', value: 'operationType' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.added.users')}`, align: 'center', value: 'addedUsers' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.removed.users')}`, align: 'center', value: 'removedUsers' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.File')}`, align: 'center', value: 'file' },
      ],
    };
  },
  created() {
    spacesAdministrationServices.getBindingReportOperations().then(data => {
      this.operations = data.groupSpaceBindingReportOperations;
      this.operations.forEach(operation => {
        this.spaces.push(operation.space);
      });
      console.log('Data', data);
    }).finally(() => this.loading = false);
  }, methods: {
    getSpaceDisplayName(spaceId) {
      console.log('spaceId: ', spaceId);
      const displayName = this.spaces.filter(space => space.id === spaceId)[0].displayName;
      return displayName;
    },
    getSpaceAvatar(spaceId) {
      console.log('spaceId: ', spaceId);
      console.log('Spaces: ', this.spaces);
      const avatar = this.spaces.filter(space => space.id === spaceId)[0].avatar;
      return avatar;
    },
    displayItem(item) {
      console.log('Item: ', item);
      return item;
    }
  }
};
</script>