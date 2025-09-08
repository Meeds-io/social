<template>
  <div>
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
      show-select
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
      <template #item.enabled="{ item }">
        <div
          :title="item.enabled && $t(`UsersManagement.button.enabled`) || $t(`UsersManagement.button.disabled`)"
          class="d-flex">
          <v-switch
            v-model="item.enabled"
            class="my-0 mx-auto"
            @change="saveUserStatus(item)" />
        </div>
      </template>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.isInternal="{ item }">
        <div v-if="item.isInternal" class="displayedIconClass">
          <div :title="createdTitle(item.createdDate)">
            <v-icon size="20">fa-database</v-icon>
          </div>
        </div>
        <div v-else class="displayedIconClass">
          <v-btn
            :title="synchronizedTitle(item.synchronizedDate)"
            primary
            icon
            text>
            <i class="uiIconManageApplication"><span class="synchronizedIconClass">SYNC</span></i>
          </v-btn>
        </div>
      </template>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.external="{ item }">
        {{ item && item.external === 'true' ? $t(`UsersManagement.type.external`) : $t(`UsersManagement.type.internal`) }}
      </template>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.role="{ item }">
        <v-btn
          :title="$t('UsersManagement.button.membership')"
          primary
          icon
          text
          @click="$root.$emit('openUserMemberships', item)">
          <i class="uiIconGroup"></i>
        </v-btn>
      </template>
      <!-- eslint-disable vue/valid-v-slot -->
      <template #item.actions="{ item }">
        <v-menu offset-x offset-y>
          <template #activator="{on, attrs}">
            <v-btn
              v-bind="attrs"
              v-on="on"
              :title="$t('UsersManagement.userActions')"
              :aria-label="$t('UsersManagement.userActions')"
              icon>
              <v-icon size="20">fas fa-ellipsis-v</v-icon>
            </v-btn>
          </template>
          <v-list dense>
            <v-tooltip :disabled="item.isInternal" bottom>
              <template #activator="{on, attrs}">
                <v-list-item
                  v-on="on"
                  v-bind="attrs"
                  :disabled="!item.isInternal"
                  class="px-2"
                  dense
                  @click="$root.$emit('editUser', item)">
                  <v-list-item-icon class="mx-1 justify-center">
                    <v-icon size="14">fa-edit</v-icon>
                  </v-list-item-icon>
                  <v-list-item-title class="ps-0">{{ $t('UsersManagement.edit') }}</v-list-item-title>
                </v-list-item>
              </template>
              <span>{{ $t('UsersManagement.tooltip.editSynchronzedUser') }}</span>
            </v-tooltip>
            <v-list-item
              v-if="isSuperUser"
              :disabled="!item.isInternal"
              class="px-2"
              dense
              @click="deleteUser(item)">
              <v-list-item-icon class="mx-1 justify-center">
                <v-icon size="14" color="error">fa-trash</v-icon>
              </v-list-item-icon>
              <v-list-item-title class="ps-0">
                <div class="error--text">{{ $t('UsersManagement.button.deleteUser') }}</div>
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>
    </v-data-table>
  </div>
</template>
<script>
export default {
  data: () => ({
    itemsPerPageOptions: [20, 50, 100],
    users: [],
    user: null,
    currentUser: eXo.env.portal.userName,
    selectedUser: null,
    selectedUsers: [],
    deleteConfirmMessage: null,
    keyword: null,
    filter: null,
    lang: eXo.env.portal.language,
    options: {
      page: 1,
      itemsPerPage: 20,
    },
    synchronizedDate: 0,
    createdDate: 0,
    totalSize: 0,
    initialized: false,
    isSuperUser: false,
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
      form.append('q', this.keyword || '');
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
        text: this.$t && this.$t('UsersManagement.userName'),
        value: 'userName',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.firstName'),
        value: 'firstName',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.lastName'),
        value: 'lastName',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
      }, {
        text: this.$t && this.$t('UsersManagement.email'),
        value: 'email',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.lastConnection'),
        value: 'lastConnexion',
        align: 'center',
        class: 'headerPadding',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.source'),
        value: 'isInternal',
        align: 'center',
        width: '80px',
        class: 'headerPadding',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.type'),
        value: 'external',
        align: 'center',
        width: '80px',
        class: 'headerPadding',
        sortable: false,
        show: !this.$root.isMobile
      }, {
        text: this.$t && this.$t('UsersManagement.actions'),
        value: 'actions',
        align: 'center',
        class: 'headerPadding',
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
    this.$userService.isSuperUser().then(
      (data) => {
        this.isSuperUser = data.isSuperUser === 'true';
      });
    this.$root.$on('searchUser', this.updateSearchTerms);
    this.$root.$on('refreshUsers', this.searchUsers);
    this.$root.$on('multiSelectAction', this.multiSelectAction);
    this.$root.$on('applyAdvancedFilter', this.applyAdvancedFilter);
  },
  methods: {
    updateSearchTerms(keyword, filter) {
      this.keyword = keyword;
      this.filter = filter;
    },
    multiSelectAction(action) {
      const selectedUsers = [];
      let msg ='';
      if (this.selectedUsers.length > 0) {
        for (let i = 0; i < this.selectedUsers.length; i++) {
          selectedUsers.push(this.selectedUsers[i].userName);
        }
        this.selectedUsers = [];
        this.loading = true;
        this.$userService.multiSelectAction(action, selectedUsers).then(data => {
          if (data.length > 0) {
            msg = this.$t(`UsersManagement.selection.success.${  action}`);
          }
        }).finally(() => {
          if (!this.initialized) {
            this.$root.$applicationLoaded();
          }
          this.searchUsers();
          this.initialized = true;
          this.$root.$emit('alert-message', msg, 'success');
        });
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
      this.deleteConfirmMessage = this.$t('UsersManagement.message.confirmDelete', {0: this.selectedUser.fullName});
      this.$refs.deleteConfirmDialog.open();
    },
    deleteUserConfirm() {
      this.loading = true;
      const self = this;
      const userNameDeleted = this.selectedUser.userName ;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${userNameDeleted}`, {
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
        } else {
          self.users = self.users.filter(u => u.userName !== userNameDeleted);
        }
      }).catch(error => {
        error = error.message || String(error);
        const errorI18NKey = `UsersManagement.error.${error}`;
        const errorI18N = this.$t(errorI18NKey, {0: this.selectedUser.fullname});
        if (errorI18N !== errorI18NKey) {
          error = errorI18N;
        }
        this.$root.$emit('alert-message', error, 'error');
      }).finally(() => this.loading = false);
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
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(data => {
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
            if (user.enrollmentDate != null) {
              user.enrollmentStatus = 'invitationAccepted';
              user.enrollmentDetails= this.$t('UsersManagement.enrollment.invitationAccepted', {0: this.formatDate(Number(user.enrollmentDate))});
            } else {
              user.enrollmentStatus = 'alreadyConnected';
              user.enrollmentDetails= this.$t('UsersManagement.enrollment.alreadyConnected');
            }
          } else {
            user.connectionStatus = this.$t('UsersManagement.lastConnection.neverConnected');
            if (user.external === 'true') {
              user.enrollmentStatus = 'cannotBeEnrolled';
              user.enrollmentDetails = this.$t('UsersManagement.enrollment.cannotBeEnrolled');
            } else if (user.enrollmentDate != null) {
              user.enrollmentStatus = 'reInviteToJoin';
              user.enrollmentDetails = this.$t('UsersManagement.enrollment.reInviteToJoin', {0: this.formatDate(Number(user.enrollmentDate))});
            } else {
              user.enrollmentStatus = 'inviteToJoin';
              user.enrollmentDetails = this.$t('UsersManagement.enrollment.inviteToJoin');
            }
          }
        });
        this.users = entities;
        this.totalSize = data && data.size || 0;
        return this.$nextTick();
      })
        .finally(() => {
          if (!this.initialized) {
            this.$root.$applicationLoaded();
          }
          this.loading = false;
          this.initialized = true;
        });
    },
    saveUserStatus(user) {
      this.error = null;
      this.user = {
        userName: user.userName,
        firstName: user.firstName,
        lastName: user.lastName,
        email: user.email,
        enabled: user.enabled,
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
          this.$root.$applicationLoaded();
        }
        this.searchUsers();
        this.loading = false;
        this.initialized = true;
      });
    },
    formatDate(time) {
      return this.$dateUtil.formatDateObjectToDisplay(new Date(time),this.fullDateFormat, this.lang);
    },
    sendOnBoardingEmail(username) {
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/onboard/${username}`, {
        method: 'PATCH',
        credentials: 'include',
      }).then((resp) => {
        if (resp && resp.ok) {
          return resp.json();
        } else {
          throw new Error('Error sending onBoarding email');
        }
      }).finally(() => {
        if (!this.initialized) {
          this.$root.$applicationLoaded();
        }
        this.searchUsers();
        this.loading = false;
        this.initialized = true;
      });
    },
    applyAdvancedFilter(filter) {
      this.filter = filter;
      this.searchUsers();
    }
  },
};
</script>
