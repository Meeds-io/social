<template>
  <div>
    <users-management-import-csv-result class="px-4" />
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('UsersManagement.title.confirmDelete')"
      :ok-label="$t('UsersManagement.button.ok')"
      :cancel-label="$t('UsersManagement.button.cancel')"
      @ok="deleteUserConfirm()" />
    <exo-confirm-dialog
      ref="currentUserWarningDialog"
      :message="$t('UsersManagement.message.deleteCurrentUserWarning')"
      :title="$t('UsersManagement.title.deleteCurrentUserWarning')"
      :ok-label="$t('UsersManagement.button.ok')" />
    <v-card-text v-if="error" class="errorMessage">
      <v-alert type="error">
        {{ error }}
      </v-alert>
    </v-card-text>
    <v-data-table
      :headers="isSuperUser ? superUserHeaders : headers"
      :items="filteredUsers"
      :loading="loading"
      :options.sync="options"
      :server-items-length="totalSize"
      :footer-props="{ itemsPerPageOptions }"
      :loading-text="$t('UsersManagement.loadingResults')"
      :no-results-text="$t('UsersManagement.noResultsFound')"
      :no-data-text="$t('UsersManagement.noData')"
      class="data-table-light-border">
      <template slot="item.lastConnexion" slot-scope="{ item }">
        <div v-if="typeof item.lastConnexion == 'number'">
          <date-format
            :value="item.lastConnexion"
            :format="fullDateFormat"
            class="grey--text mr-1" />
        </div>
        <div v-else class="grey--text">
          {{ item.lastConnexion }}
        </div>
      </template>
      <template slot="item.enabled" slot-scope="{ item }">
        <div>
          <label class="switch">
            <input
              v-model="item.enabled"
              type="checkbox"
              @click ="saveUserStatus(item)">
            <div class="slider round"><span class="absolute-activate">{{ $t(`UsersManagement.button.activated`) }}</span></div>
            <span class="absolute-deactivated">{{ $t(`UsersManagement.button.deactivated`) }}</span>
          </label>
        </div>
      </template>
      <template slot="item.role" slot-scope="{ item }">
        <v-btn
          :title="$t('UsersManagement.button.membership')"
          primary
          icon
          text
          @click="$root.$emit('openUserMemberships', item)">
          <i class="uiIconGroup"></i>
        </v-btn>
      </template>
      <template slot="item.edit" slot-scope="{ item }">
        <v-btn
          :title="$t('UsersManagement.button.editUser')"
          primary
          icon
          text
          @click="$root.$emit('editUser', item)">
          <i class="uiIconEdit"></i>
        </v-btn>
      </template>
      <template slot="item.delete" slot-scope="{ item }">
        <v-btn
          :title="$t('UsersManagement.button.deleteUser')"
          primary
          icon
          text
          @click="deleteUser(item)">
          <i class="uiIconTrash trashIconColor"></i>
        </v-btn>
      </template>
    </v-data-table>
  </div>
</template>

<script>
export default {
  data: () => ({
    startSearchAfterInMilliseconds: 600,
    endTypingKeywordTimeout: 50,
    startTypingKeywordTimeout: 0,
    itemsPerPageOptions: [20, 50, 100],
    users: [],
    user: null,
    currentUser: eXo.env.portal.userName,
    selectedUser: null,
    deleteConfirmMessage: null,
    keyword: null,
    filter: 'ENABLED',
    options: {
      page: 1,
      itemsPerPage: 20,
    },
    totalSize: 0,
    initialized: false,
    isSuperUser: false,
    loading: true,
    error: null,
    fullDateFormat: {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }
  }),
  computed: {
    filteredUsers() {
      if (!this.keyword || !this.loading) {
        return this.users.slice();
      } else {
        return this.users.slice()
          .filter(user => 
            user.userName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || user.firstName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || user.lastName.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0
            || user.email.toLowerCase().indexOf(this.keyword.toLowerCase()) >= 0);
      }
    },
    headers() {
      return [{
        text: this.$t && this.$t('UsersManagement.userName'),
        value: 'userName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.firstName'),
        value: 'firstName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.lastName'),
        value: 'lastName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.email'),
        value: 'email',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.lastConnexion'),
        value: 'lastConnexion',
        align: 'center',
        width: '20%',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.status'),
        value: 'enabled',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.role'),
        value: 'role',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.edit'),
        value: 'edit',
        align: 'center',
        sortable: false,
      }];
    },
    superUserHeaders() {
      return [{
        text: this.$t && this.$t('UsersManagement.userName'),
        value: 'userName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.firstName'),
        value: 'firstName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.lastName'),
        value: 'lastName',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.email'),
        value: 'email',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.status'),
        value: 'enabled',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.role'),
        value: 'role',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.edit'),
        value: 'edit',
        align: 'center',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.delete'),
        value: 'delete',
        align: 'center',
        sortable: false,
      }];
    },
  },
  watch: {
    options() {
      this.searchUsers();
    },
    filter() {
      this.options.page = 1;
      this.searchUsers();
    },
    keyword() {
      this.options.page = 1;
      if (!this.keyword) {
        this.searchUsers();
        return;
      }
      this.startTypingKeywordTimeout = Date.now();
      if (!this.loading) {
        this.loading = true;
        this.waitForEndTyping();
      }
    },
  },
  created() {
    this.$userService.isSuperUser().then(
      (data) => {
        this.isSuperUser = data.isSuperUser === 'true';
      });
    this.$root.$on('searchUser', this.updateSearchTerms);
    this.$root.$on('refreshUsers', this.searchUsers);
  },
  methods: {
    updateSearchTerms(keyword, filter) {
      this.keyword = keyword;
      this.filter = filter;
    },
    deleteUser(user) {
      if (this.currentUser === user.userName) {
        this.$refs.currentUserWarningDialog.open();
        return;
      }
      this.selectedUser = user;
      this.deleteConfirmMessage = this.$t('UsersManagement.message.confirmDelete', {0: this.selectedUser.fullName});
      this.$refs.deleteConfirmDialog.open();
    },
    deleteUserConfirm() {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${this.selectedUser.userName}`, {
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
        return this.searchUsers();
      }).catch(error => {
        error = error.message || String(error);
        const errorI18NKey = `UsersManagement.error.${error}`;
        const errorI18N = this.$t(errorI18NKey, {0: this.selectedUser.fullname});
        if (errorI18N !== errorI18NKey) {
          error = errorI18N;
        }
        this.error = error;
        window.setTimeout(() => {
          this.error = null;
        }, 5000);
      }).finally(() => this.loading = false);
    },
    searchUsers() {
      const page = this.options && this.options.page;
      let itemsPerPage = this.options && this.options.itemsPerPage;
      if (itemsPerPage <= 0) {
        itemsPerPage = this.totalSize || 20;
      }
      const offset = (page - 1) * itemsPerPage;
      this.loading = true;
      const uri = this.filter === 'ENABLED' ? 'social/users':'users';
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/${uri}?q=${this.keyword || ''}&status=${this.filter || 'ENABLED'}&offset=${offset || 0}&limit=${itemsPerPage}&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(data => {
        const entities = data.entities || data.users;
        entities.forEach(user => {
          user.enabled = user.enabled ;
          user.userName = user.userName || user.username || '';
          user.firstName = user.firstName || user.firstname || '';
          user.lastName = user.lastName || user.lastname || '';
          if (user.lastConnexion) {
            user.lastConnexion = Number(user.lastConnexion);
          } else if (user.external === 'true') {
            user.lastConnexion = this.$t('UsersManagement.lastConnexion.neverConnected');
          } else {
            user.lastConnexion = this.$t('UsersManagement.lastConnexion.neverEnrolled');
          }
        });
        this.users = entities;
        this.totalSize = data && data.size || 0;
        return this.$nextTick();
      })
        .finally(() => {
          if (!this.initialized) {
            this.$root.$emit('application-loaded');
          }
          this.loading = false;
          this.initialized = true;
        });
    },
    waitForEndTyping() {
      window.setTimeout(() => {
        if (Date.now() - this.startTypingKeywordTimeout > this.startSearchAfterInMilliseconds) {
          this.searchUsers();
        } else {
          this.waitForEndTyping();
        }
      }, this.endTypingKeywordTimeout);
    },
    saveUserStatus(user) {
      this.error = null;
      this.user = {
        userName: user.userName,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        enabled: !user.enabled,
      };
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users`, {
        method: 'PUT',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(this.user),
      }).then(resp => {
        if (!resp || !resp.ok) {
          if (resp.status === 400) {
            return resp.text().then(error => {
              error = error.message || String(error);
              const errorI18NKey = `UsersManagement.error.${error}`;
              const errorI18N = this.$t(errorI18NKey, {0: user.fullname});
              if (errorI18N !== errorI18NKey) {
                error = errorI18N;
              }
              this.error = error;
              window.setTimeout(() => {
                this.error = null;
              }, 5000);
            });
          } else {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          }
        }
      })        .finally(() => {
        if (!this.initialized) {
          this.$root.$emit('application-loaded');
        }
        this.searchUsers();
        this.loading = false;
        this.initialized = true;
      });
    },
  },
};
</script>
