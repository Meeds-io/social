<template>
  <div>
    <exo-confirm-dialog
      ref="deleteConfirmDialog"
      :message="deleteConfirmMessage"
      :title="$t('UsersManagement.title.confirmDelete')"
      :ok-label="$t('UsersManagement.button.confirm')"
      :cancel-label="$t('UsersManagement.button.cancel')"
      width="500"
      @ok="deleteUserConfirm()" />
    <exo-confirm-dialog
      ref="currentUserWarningDialog"
      :message="$t('UsersManagement.message.deleteCurrentUserWarning')"
      :title="$t('UsersManagement.title.deleteCurrentUserWarning')"
      :ok-label="$t('UsersManagement.button.ok')" />
    <v-data-table
      v-model="selectedUsers"
      :headers="headers"
      :items="filteredUsers"
      :loading="loading"
      :options.sync="options"
      :server-items-length="totalSize"
      :footer-props="{ itemsPerPageOptions }"
      :loading-text="$t('UsersManagement.loadingResults')"
      :no-results-text="$t('UsersManagement.noResultsFound')"
      :no-data-text="$t('UsersManagement.noData')"
      :mobile-breakpoint="0"
      :show-select="!$root.isMobile"
      class="data-table-light-border">
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.lastConnexion="{ item }">
        <div v-if="item.lastLoginTime">
          <date-format
            :value="item.lastLoginTime"
            :format="fullDateFormat"
            class="grey--text me-1" />
        </div>
        <div v-else class="grey--text">
          {{ item.connectionStatus }}
        </div>
      </template>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.isInternal="{ item }">
        <div v-if="item.isInternal" class="displayedIconClass">
          <v-tooltip bottom>
            <template #activator="{on, attrs}">
              <v-icon
                v-on="on"
                v-bind="attrs"
                size="20">
                fa-database
              </v-icon>
            </template>
            <span>{{ createdTitle(item.createdDate) }}</span>
          </v-tooltip>
        </div>
        <div v-else class="displayedIconClass">
          <v-tooltip bottom>
            <template #activator="{on, attrs}">
              <v-icon
                v-on="on"
                v-bind="attrs"
                color="primary"
                size="20">
                fa-network-wired
              </v-icon>
            </template>
            <span>{{ synchronizedTitle(item.synchronizedDate) }}</span>
          </v-tooltip>
        </div>
      </template>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.external="{ item }">
        {{ item && item.external === 'true' ? $t(`UsersManagement.type.external`) : $t(`UsersManagement.type.internal`) }}
      </template>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.actions="{ item }">
        <users-management-item-menu
          :item="item"
          @onboard="$root.$emit('openOnboardUser', item)"
          @enable="saveUserStatus(item, true)"
          @disable="saveUserStatus(item, false)"
          @delete="deleteUser(item)" />
      </template>
    </v-data-table>
  </div>
</template>
<script>
export default {
  data: (vm) => ({
    itemsPerPageOptions: [20, 50, 100],
    users: [],
    user: null,
    currentUser: eXo.env.portal.userName,
    selectedUser: null,
    selectedUsers: [],
    deleteConfirmMessage: null,
    tableMenus: {},
    keyword: null,
    filter: vm.$root.presetStatus === 'DISABLED' ? { status: 'DISABLED' } : null,
    lang: eXo.env.portal.language,
    options: {
      page: 1,
      itemsPerPage: 20,
    },
    synchronizedDate: 0,
    createdDate: 0,
    totalSize: 0,
    initialized: false,
    loading: true,
    fullDateFormat: {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    },
  }),
  computed: {
    status() {
      return this.filter?.status || 'ENABLED';
    },
    type() {
      return this.filter?.type;
    },
    connectionStatus() {
      return this.filter?.connectionStatus;
    },
    enrollmentStatus() {
      return this.filter?.enrollmentStatus;
    },
    retrieveListLink() {
      const form = new FormData();
      form.append('isDisabled', this.status === 'ENABLED' ? 'false':'true');
      form.append('searchEmail', 'true');
      form.append('searchUserName', 'true');
      form.append('userType', this.type || '');
      form.append('status', this.status || 'ENABLED');
      form.append('q', this.status === 'DISABLED' || !this.keyword ? '' : this.keyword);
      if (this.connectionStatus) {
        form.append('isConnected', this.connectionStatus);
      }
      if (this.enrollmentStatus) {
        form.append('enrollmentStatus', this.enrollmentStatus);
      }
      if (this.selectedUsers) {
        this.selectedUsers.forEach(u => form.append('includeUser', u.userName));
      }
      const params = new URLSearchParams(form).toString();
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users?${params}`;
    },
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
        text: this.$t && this.$t('UsersManagement.fullName'),
        value: 'fullname',
        align: 'center',
        class: 'px-2',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.email'),
        value: 'email',
        align: 'center',
        class: 'px-2',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.lastConnection'),
        value: 'lastConnexion',
        align: 'center',
        class: 'px-2',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.source'),
        value: 'isInternal',
        align: 'center',
        width: '80px',
        class: 'px-2',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.type'),
        value: 'external',
        align: 'center',
        width: '80px',
        class: 'px-2',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.actions'),
        value: 'actions',
        align: 'center',
        class: 'px-2',
        sortable: false,
      }].filter(x => x.show == null || x.show === true);
    },
  },
  watch: {
    options() {
      this.searchUsers();
    },
    totalSize() {
      this.$emit('total-size-updated', this.totalSize);
    },
    filter() {
      this.$emit('filter-updated', this.status);
      this.options.page = 1;
      this.searchUsers();
    },
    selectedUsers(selectedUsers) {
      document.dispatchEvent( new CustomEvent('multiSelect', {detail: {usersSelected: selectedUsers.length > 0}}));
    },
    initialized(_newVal, oldVal) {
      if (!oldVal) {
        this.$root.$applicationLoaded();
      }
    },
    keyword() {
      this.options.page = 1;
      this.searchUsers();
    },
    retrieveListLink: {
      immediate: true,
      handler() {
        this.$emit('list-link-updated', this.retrieveListLink);
      },
    },
  },
  created() {
    this.$root.$on('searchUser', this.updateSearchTerms);
    this.$root.$on('refreshUsers', this.searchUsers);
    this.$root.$on('multiSelectAction', this.multiSelectAction);
    this.$root.$on('apply-users-filter', this.applyAdvancedFilter);
  },
  methods: {
    updateSearchTerms(keyword, filter) {
      this.keyword = keyword;
      this.filter = filter;
    },
    multiSelectAction(action) {
      const selectedUsers = [];
      if (this.selectedUsers.length > 0) {
        for (let i = 0; i < this.selectedUsers.length; i++) {
          selectedUsers.push(this.selectedUsers[i].userName);
        }
        const selection = this.selectedUsers;
        this.selectedUsers = [];
        this.loading = true;
        this.$userService.multiSelectAction(action, selectedUsers)
          .then(data => {
            // The server lists the users it updated, each read back
            if (data.length === selectedUsers.length) {
              this.$root.$emit('alert-message', `UsersManagement.selection.success.${action}`, 'success');
            } else {
              const partialKey = action === 'onboard' ? 'UsersManagement.selection.partial.onboard' : 'UsersManagement.selection.partial';
              this.$root.$emit('alert-message', this.$t(partialKey, {0: data.length, 1: selectedUsers.length}), 'warning');
            }
            if (data.length > 0) {
              this.$root.$emit('pending-users-changed');
            }
          })
          .then(this.searchUsers)
          .catch(error => {
            if (this.$userAccountConfirmationService.isCancelled(error)) {
              // Nothing was applied: the administrator keeps the selection
              this.selectedUsers = selection;
            } else {
              throw error;
            }
          })
          .finally(() => this.loading = false);
      }
    },
    synchronizedTitle(synchronizedDate) {
      if (synchronizedDate) {
        return this.$t('UsersManagement.source.synchronized', {0: this.formatDate(synchronizedDate)});
      } else {
        return this.$t('UsersManagement.source.synchronized.notProvided');
      }
    },
    createdTitle(createdDate) {
      return this.$t('UsersManagement.source.createdUser', {0: this.formatDate(createdDate)});
    },
    deleteUser(user) {
      if (this.currentUser === user.userName) {
        this.$refs.currentUserWarningDialog.open();
        return;
      }
      this.selectedUser = user;
      this.deleteConfirmMessage = `
        <p>${this.$t('UsersManagement.message.confirmDelete', {0: `<strong>${this.selectedUser.fullname}</strong>`})}</p>
        <p class="mb-0">${this.$t('UsersManagement.message.confirmDelete.onceDone')}</p>
        <ul class="mb-2">
          <li>${this.$t('UsersManagement.message.confirmDelete.deactivated')}</li>
          <li>${this.$t('UsersManagement.message.confirmDelete.noAccess')}</li>
          <li>${this.$t('UsersManagement.message.confirmDelete.contentKept')}</li>
          <li>${this.$t('UsersManagement.message.confirmDelete.anonymization')}</li>
        </ul>
        <p>${this.$t('UsersManagement.message.confirmDelete.usernameReminder', {0: `<strong>${this.selectedUser.userName}</strong>`})}</p>
        <p class="mb-0">${this.$t('UsersManagement.message.confirmDelete.question')}</p>`;
      this.$refs.deleteConfirmDialog.open();
    },
    deleteUserConfirm() {
      this.loading = true;
      const self = this;
      const userNameDeleted = this.selectedUser.userName ;
      return this.$userAccountConfirmationService.confirmedFetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${userNameDeleted}`, {
        method: 'DELETE',
        credentials: 'include',
      })
        .then(resp => {
          if (!resp || !resp.ok) {
            if (resp && resp.status === 400) {
              return resp.text().then(error => {
                throw new Error(error);
              });
            } else {
              throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
            }
          } else {
            self.users = self.users.filter(u => u.userName !== userNameDeleted);
            this.$root.$emit('pending-users-changed');
          }
        })
        .catch(error => {
          if (this.$userAccountConfirmationService.isCancelled(error)) {
            return;
          }
          error = error.message || String(error);
          const errorI18NKey = `UsersManagement.error.${error}`;
          const errorI18N = this.$t(errorI18NKey, {0: this.selectedUser.fullname});
          if (errorI18N !== errorI18NKey) {
            error = errorI18N;
          }
          this.$root.$emit('alert-message', error, 'error');
        })
        .finally(() => this.loading = false);
    },
    searchUsers() {
      this.loading = true;
      const page = this.options && this.options.page;
      let itemsPerPage = this.options && this.options.itemsPerPage;
      if (itemsPerPage <= 0) {
        itemsPerPage = this.totalSize || 20;
      }
      const offset = (page - 1) * itemsPerPage;
      return fetch(`${this.retrieveListLink}&offset=${offset || 0}&limit=${itemsPerPage}&returnSize=true`, {
        method: 'GET',
        credentials: 'include',
      })
        .then(resp => {
          if (!resp || !resp.ok) {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          } else {
            return resp.json();
          }
        })
        .then(data => {
          const entities = data.entities || data.users;
          entities.forEach(user => {
            user.enabled = user.enabled || false;
            user.userName = user.userName || user.username || '';
            user.firstName = user.firstName || user.firstname || '';
            user.lastName = user.lastName || user.lastname || '';
            user.email = user.email || '';
            if (user.synchronizedDate) {
              user.synchronizedDate = Number(user.synchronizedDate);
            }
            if (user.createdDate) {
              user.createdDate = Number(user.createdDate);
            }
            if (user.lastLoginTime) {
              user.lastLoginTime = Number(user.lastLoginTime);
            } else {
              user.connectionStatus = this.$t('UsersManagement.lastConnection.neverConnected');
            }
            // Onboarding follows the same "never signed in" source as the
            // Pending Users tabs, whatever the account's type
            if (!user.neverConnected) {
              if (user.enrollmentDate != null) {
                user.enrollmentStatus = 'invitationAccepted';
                user.enrollmentDetails= this.$t('UsersManagement.enrollment.invitationAccepted', {0: this.formatDate(Number(user.enrollmentDate))});
              } else {
                user.enrollmentStatus = 'alreadyConnected';
                user.enrollmentDetails= this.$t('UsersManagement.enrollment.alreadyConnected');
              }
            } else if (user.enrollmentDate != null) {
              user.enrollmentStatus = 'reInviteToJoin';
              user.enrollmentDetails = this.$t('UsersManagement.enrollment.reInviteToJoin', {0: this.formatDate(Number(user.enrollmentDate))});
            } else {
              user.enrollmentStatus = 'inviteToJoin';
              user.enrollmentDetails = this.$t('UsersManagement.enrollment.inviteToJoin');
            }
          });
          this.users = entities;
          this.totalSize = data && data.size || 0;
          return this.$nextTick();
        })
        .finally(() => this.loading = false);
    },
    saveUserStatus(user, enabled) {
      this.error = null;
      this.user = {
        userName: user.userName,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        enabled: enabled,
      };
      this.loading = true;
      return this.$userAccountConfirmationService.confirmedFetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users`, {
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
          } else if (resp.status === 500) {
            // A status the server could not prove: the table shows the
            // stored one once refreshed
            return resp.text().then(code => {
              const key = `UsersManagement.edit.error.${(code || '').replace(/:/g, '.')}`;
              const label = this.$t(key);
              this.$root.$emit('alert-message', label === key ? this.$t('IDMManagement.error.UnknownServerError') : label, 'error');
            });
          } else {
            throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
          }
        } else {
          this.$root.$emit('pending-users-changed');
        }
      })
        .then(this.searchUsers)
        .catch(error => {
          if (!this.$userAccountConfirmationService.isCancelled(error)) {
            throw error;
          }
        })
        .finally(() => this.loading = false);
    },
    formatDate(time) {
      return this.$dateUtil.formatDateObjectToDisplay(new Date(time),this.fullDateFormat, this.lang);
    },
    applyAdvancedFilter(filter) {
      this.filter = filter;
      this.searchUsers();
    },
  },
};
</script>
