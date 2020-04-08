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
        :headers="headers"
        :items="operations"
        :search="search"
        disable-sort
      >
        <template slot="item" slot-scope="props">
          <tr>
            <td class="text-md-center">
              <img v-if="props.item.space.avatarUrl != null" :src="props.item.space.avatarUrl" class="avatar" />
              <img v-else :src="avatar" class="avatar" />
              {{ props.item.space.displayName }}
            </td>
            <td class="text-md-center">{{ props.item.group }}</td>
            <td class="text-md-center">{{ props.item.startDate }}</td>
            <td class="text-md-center">
              <div v-if="props.item.endDate !== 'null'"> {{ props.item.endDate }} </div>
              <div v-else class="inProgress">
                <v-progress-circular
                  indeterminate
                  color="primary">
                </v-progress-circular> <span>In progress</span>
              </div>
            </td>
            <td class="text-md-center">{{ props.item.operationType }}</td>
            <td class="text-md-center">{{ props.item.addedUsers }}</td>
            <td class="text-md-center">{{ props.item.removedUsers }}</td>
            <td class="text-md-center">{{ props.item.file }}</td>
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
        { text: `${this.$t('social.spaces.administration.manageSpaces.space')}`, align: 'center' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.group')}`, align: 'center' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.start.date')}`, align: 'center' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.end.date')}`, align: 'center' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.operation.type')}`, align: 'center' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.added.users')}`, align: 'center' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.removed.users')}`, align: 'center' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.File')}`, align: 'center' },
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