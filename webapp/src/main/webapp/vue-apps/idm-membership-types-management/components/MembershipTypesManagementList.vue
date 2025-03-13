<template>
  <div>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :cancel-label="$t('MembershipTypesManagement.button.cancel')"
      :message="deleteConfirmMessage"
      :ok-label="$t('MembershipTypesManagement.button.ok')"
      :title="$t('MembershipTypesManagement.title.confirmDelete')"
      @ok="deleteConfirm()" />
    <v-data-table
      v-model:options="options"
      class="data-table-light-border"
      :footer-props="{ itemsPerPageOptions }"
      :headers="headers"
      :hide-default-footer="!hasPages"
      :items="filteredMembershipTypes"
      :loading="loading"
      :loading-text="$t('UsersManagement.loadingResults')"
      :no-data-text="$t('UsersManagement.noData')"
      :no-results-text="$t('UsersManagement.noResultsFound')">
      <template
        #item.createdDate="{ item }">
        <date-format
          :format="dateTimeFormat"
          :value="item.createdDate && item.createdDate.time" />
      </template>
      <template
        #item.modifiedDate="{ item }">
        <date-format
          :format="dateTimeFormat"
          :value="item.modifiedDate && item.modifiedDate.time" />
      </template>
      <template
        #item.actions="{ item }">
        <v-btn
          icon
          primary
          text
          :title="$t('MembershipTypesManagement.editMembershipType')"
          @click="$root.$emit('editMembershipType', item)">
          <i class="uiIconEdit"></i>
        </v-btn>
        <v-btn
          icon
          primary
          text
          :title="$t('MembershipTypesManagement.button.delete')"
          @click="deleteMembershipType(item)">
          <i class="uiIconTrash"></i>
        </v-btn>
      </template>
    </v-data-table>
  </div>
</template>

<script>
  export default {
    data: () => ({
      startTypingKeywordTimeout: 0,
      itemsPerPageOptions: [20, 50, 100],
      membershipTypes: [],
      selectedMembershipType: null,
      deleteConfirmMessage: null,
      keyword: null,
      dateTimeFormat: {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      },
      pageSize: 20,
      options: {
        page: 1,
        itemsPerPage: 20,
      },
      loading: true,
    }),
    computed: {
      hasPages () {
        return this.membershipTypes.length > this.pageSize;
      },
      filteredMembershipTypes () {
        if (this.keyword) {
          return this.membershipTypes.slice()
            .filter(membershipType => 
              membershipType
              && (membershipType.name
                && membershipType.name.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0)
              || membershipType.description
              && membershipType.description.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
        } else {
          return this.membershipTypes.slice();
        }
      },
      headers () {
        return [{
          text: this.$t && this.$t('MembershipTypesManagement.name'),
          value: 'name',
          align: 'center',
          sortable: false,
        }, {
          text: this.$t && this.$t('MembershipTypesManagement.description'),
          value: 'description',
          align: 'center',
          sortable: false,
        }, {
          text: this.$t && this.$t('MembershipTypesManagement.createdDate'),
          value: 'createdDate',
          align: 'center',
          sortable: false,
        }, {
          text: this.$t && this.$t('MembershipTypesManagement.modifiedDate'),
          value: 'modifiedDate',
          align: 'center',
          sortable: false,
        }, {
          text: this.$t && this.$t('MembershipTypesManagement.actions'),
          value: 'actions',
          align: 'center',
          sortable: false,
        }];
      },
    },
    watch: {
      keyword () {
        this.options.page = 1;
        this.searchMembershipTypes();
      },
    },
    created () {
      this.$root.$on('searchMembershipType', this.updateSearchTerms);
      this.$root.$on('refreshMembershipTypes', this.searchMembershipTypes);

      this.searchMembershipTypes()
        .finally(() => this.$root.$applicationLoaded());
    },
    methods: {
      updateSearchTerms (keyword) {
        this.keyword = keyword;
      },
      deleteMembershipType (membershipType) {
        this.selectedMembershipType = membershipType;
        this.deleteConfirmMessage = this.$t('MembershipTypesManagement.message.confirmDelete', { 0: this.selectedMembershipType.name });
        this.$refs.deleteConfirmDialog.open();
      },
      deleteConfirm () {
        this.loading = true;
        return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/membershipTypes/${this.selectedMembershipType.name}`, {
          method: 'DELETE',
          credentials: 'include',
        }).then(resp => {
          if (!resp || !resp.ok) {
            if (resp && resp.status === 400) {
              return resp.text().then(error => {
                throw new Error(error);
              });
            } else {
              throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
            }
          }
          return this.searchMembershipTypes();
        }).catch(error => {
          error = error.message || String(error);
          const errorI18NKey = `MembershipTypesManagement.error.${error}`;
          const errorI18N = this.$t(errorI18NKey, { 0: this.selectedMembershipType.name });
          if (errorI18N !== errorI18NKey) {
            error = errorI18N;
          }
          this.$root.$emit('alert-message', error, 'error');
        })
          .finally(() => this.loading = false);
      },
      searchMembershipTypes () {
        this.loading = true;
        return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/membershipTypes`, {
          method: 'GET',
          credentials: 'include',
        }).then(resp => {
          if (!resp || !resp.ok) {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          } else {
            return resp.json();
          }
        }).then(data => {
          this.membershipTypes = data || [];
        })
          .finally(() => this.loading = false);
      },
    },
  };
</script>