<template>
  <div class="bindingReports">
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
        :items="operations"
        :search="search"
        disable-sort
      >
        <template slot="header" slot-scope="props">
          <tr>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.manageSpaces.space') }}
            </th>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.binding.reports.table.title.group') }}
            </th>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.binding.reports.table.title.start.date') }}
            </th>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.binding.reports.table.title.end.date') }}
            </th>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.binding.reports.table.title.operation.type') }}
            </th>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.binding.reports.table.title.added.users') }}
            </th>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.binding.reports.table.title.removed.users') }}
            </th>
            <th colspan="1" class="text-md-center">
              {{ $t('social.spaces.administration.binding.reports.table.title.File') }}
            </th>
          </tr>
        </template>
        <template slot="item" slot-scope="props">
          <tr>
            <td>
              <img v-if="props.item.space.avatarUrl != null" :src="props.item.space.avatarUrl" class="avatar" />
              <img v-else :src="avatar" class="avatar" />
              {{ props.item.space.displayName }}
            </td>
            <td>{{ props.item.group }}</td>
            <td>{{ props.item.startDate }}</td>
            <td>
              <div v-if="props.item.endDate !== 'null'"> {{ props.item.endDate }} </div>
              <div v-else class="inProgress">
                <v-progress-circular
                  indeterminate
                  color="primary">
                </v-progress-circular> <span>In progress</span>
              </div>
            </td>
            <td>{{ props.item.operationType }}</td>
            <td>{{ props.item.addedUsers }}</td>
            <td>{{ props.item.removedUsers }}</td>
            <td>{{ props.item.file }}</td>
          </tr>
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