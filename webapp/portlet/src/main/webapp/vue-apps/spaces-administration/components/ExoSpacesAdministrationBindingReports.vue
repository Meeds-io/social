<template>
  <div class="bindingReports">
    <div v-if="loading">
      <v-skeleton-loader
        class="mx-auto"
        type="table-heading,table-tbody" />
    </div>
    <div v-else>
      <div class="reportsFilterSearch">
        <v-layout justify-end row>
          <v-flex xs2>
            <v-text-field
              v-model="search"
              :placeholder="$t('social.spaces.administration.binding.reports.search')"
              prepend-inner-icon="search"
              single-line
              flat
              hide-details />
          </v-flex>
          <v-flex
            class="filter"
            ms-2
            me-4
            xs2>
            <select
              v-model="action"
              class="selectSpacesFilter my-auto width-auto me-2 subtitle-1 ignore-vuetify-classes d-none d-sm-inline">
              <option
                v-for="operationType in operationTypes"
                :key="operationType"
                :value="operationType">
                {{ operationType }}
              </option>
            </select>
          </v-flex>
        </v-layout>
      </div>
      <v-data-table
        :headers="headers"
        :items="operations"
        :search="search"
        :footer-props="{
          itemsPerPageText: `${$t('social.spaces.administration.binding.reports.table.footer.rows.per.page')}:`,        
        }"
        disable-sort>
        <template slot="item" slot-scope="props">
          <tr>
            <td class="text-md-start">
              <img
                v-if="props.item.space.avatarUrl != null"
                :src="props.item.space.avatarUrl"
                class="avatar">
              <img
                v-else
                :src="avatar"
                class="avatar">
              {{ props.item.space.displayName }}
            </td>
            <td
              :title="props.item.group.name"
              class="text-md-center nameGroupBinding"
              rel="tooltip"
              data-placement="bottom">
              {{ props.item.group.name }}
            </td>
            <td class="text-md-center">
              <date-format :value="props.item.startDate" :format="dateFormat" />
            </td>
            <td class="text-md-center">
              <div v-if="props.item.endDate !== 'null'">
                <date-format :value="props.item.endDate" :format="dateFormat" />
              </div>
              <div v-else class="inProgress">
                <v-progress-circular
                  indeterminate
                  color="primary" /> <span>In progress</span>
              </div>
            </td>
            <td class="text-md-center">{{ getOperationType(props.item.operationType) }}</td>
            <td class="text-md-center">{{ props.item.addedUsers }}</td>
            <td class="text-md-center">{{ props.item.removedUsers }}</td>
            <td class="text-md-center"> 
              <v-btn
                icon
                class="rightIcon"
                @click="uploadCSVFile(props.item.space.id, props.item.operationType, props.item.group.id, props.item.bindingId)">
                <v-icon
                  medium
                  class="uploadFile">
                  mdi-download
                </v-icon>
              </v-btn>
            </td>
          </tr>
        </template>
      </v-data-table>
    </div>
  </div>
</template>

<script>
import * as spacesAdministrationServices from '../spacesAdministrationServices';

export default {
  data() {
    return {
      loading: true,
      avatar: this.$spacesConstants && this.$spacesConstants.DEFAULT_SPACE_AVATAR,
      search: '',
      action: `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`,
      operationTypes: [
        `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`, 
        `${this.$t('social.spaces.administration.binding.reports.filter.add')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.remove')}`,
        `${this.$t('social.spaces.administration.binding.reports.filter.synchronize')}`
      ],
      operations: [],
      dateFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: 'numeric',
        minute: 'numeric',
        second: 'numeric'
      }
    };
  },
  computed: {
    headers() {
      return [
        { text: `${this.$t('social.spaces.administration.manageSpaces.space')}`, align: 'center', value: 'space.displayName' },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.group')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.start.date')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.end.date')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.operation.type')}`,
          align: 'center',
          filterable: true,
          value: 'operationType',
          filter: value => {
            if (this.action === `${this.$t('social.spaces.administration.binding.reports.filter.all.bindings')}`) {
              return true;
            }
            return this.getOperationType(value).toLowerCase() === this.action.toLowerCase();
          },
        },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.added.users')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.removed.users')}`, align: 'center', filterable: false },
        { text: `${this.$t('social.spaces.administration.binding.reports.table.title.File')}`, align: 'center', filterable: false },
      ];
    }
  },
  created() {
    spacesAdministrationServices.getBindingReportOperations().then(data => {
      this.operations = data.groupSpaceBindingReportOperations;
    }).finally(() => this.loading = false);
  }, methods: {
    uploadCSVFile(spaceId, action, groupId, groupBindingId) {
      spacesAdministrationServices.getReport(spaceId, action, groupId, groupBindingId);
    },
    getOperationType(type) {
      const action = type.toLowerCase();
      return `${this.$t(`social.spaces.administration.binding.reports.filter.${action}`)}`;
    }
  },
};
</script>
