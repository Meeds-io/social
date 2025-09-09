<template>
  <application-toolbar
    id="usersManagementToolbar"
    :right-text-filter="status === 'ENABLED' && {
      minCharacters: 1,
      placeholder: $t('UsersManagement.filterBy'),
      tooltip: $t('UsersManagement.filterBy'),
    }"
    :right-filter-button="{
      hide: false,
      text: $t('UsersManagement.filterBy')
    }"
    :filters-count="filtersCount"
    class="px-1"
    compact
    @filter-button-click="$root.$emit('advancedFilter')"
    @filter-text-input-end-typing="keyword = $event">
    <template #left>
      <div class="d-flex position-absolute zindex-1 mt-n1 t-0">
        <v-menu v-model="menu" offset-y>
          <!-- eslint-disable vue/valid-v-slot -->
          <template #activator="{on, attrs}">
            <div class="d-flex border-radius overflow-hidden">
              <v-btn
                class="btn btn-primary addNewUserButton"
                tile
                @click="$root.$emit('addNewUser')">
                <i class="uiIconAddUser uiIconWhite me-md-3"></i>
                <span class="d-none d-sm-inline">
                  {{ $t('UsersManagement.addUser') }}
                </span>
              </v-btn>
              <v-divider vertical />
              <v-btn
                v-bind="attrs"
                v-on="on"
                class="btn btn-primary overflow-hidden pa-0"
                min-width="30"
                tile
                @click.prevent.stop="menu = !menu">
                <v-icon size="20">{{ menu ? 'fa-caret-up' : 'fa-caret-down' }}</v-icon>
              </v-btn>
            </div>
          </template>
          <users-management-import-csv-button
            class="full-width border-box-sizing" />
        </v-menu>
        <v-btn
          :loading="exporting"
          color="primary"
          elevation="0"
          class="ms-2"
          outlined
          @click="exportUsers">
          <v-icon size="14" class="me-2">fa-file-excel</v-icon>
          {{ $t('UsersManagement.selection.export') }}
        </v-btn>
        <template v-if="usersSelected">
          <v-btn
            outlined
            color="primary"
            class="ms-2 multiSelect"
            @click="multiSelectAction('onboard')">
            <i class="uiIconInviteUser me-2"></i>
            {{ $t('UsersManagement.selection.onboard') }}
          </v-btn>
          <v-btn
            v-if="disabledUsers"
            outlined
            color="primary"
            class="ms-2 multiSelect"
            @click="multiSelectAction('enable')">
            <i class="uiIconValidateUser me-2"></i>
            {{ $t('UsersManagement.selection.enable') }}
          </v-btn>
          <v-btn
            v-else
            outlined
            color="primary"
            class="ms-2 multiSelect"
            @click="multiSelectAction('disable')">
            <i class="uiIconRejectUser me-2"></i>
            {{ $t('UsersManagement.selection.disable') }}
          </v-btn>
        </template>
      </div>
    </template>
  </application-toolbar>
</template>
<script>
export default {
  props: {
    exportUsersUrl: {
      type: String,
      default: null,
    },
    disabledUsers: {
      type: Boolean,
      default: false,
    },
    totalSize: {
      type: Number,
      default: () => 0,
    },
  },
  data: () => ({
    initialized: false,
    keyword: null,
    usersSelected: false,
    filter: null,
    menu: false,
    exportId: null,
    exporting: false,
  }),
  computed: {
    exportLink() {
      return `${this.exportUsersUrl}&export=true`;
    },
    status() {
      return this.filter?.status || 'ENABLED';
    },
    filtersCount() {
      return (this.status !== 'ENABLED' ? 1 : 0)
        + (this.filter?.type ? 1 : 0)
        + (this.filter?.connectionStatus ? 1 : 0)
        + (this.filter?.enrollmentStatus ? 1 : 0);
    },
  },
  watch: {
    keyword() {
      this.$root.$emit('searchUser', this.keyword, this.filter);
    },
    filter() {
      this.$root.$emit('searchUser', this.keyword, this.filter);
    },
    menu() {
      if (this.menu) {
        document.addEventListener('click', this.closeMenu);
      } else {
        document.removeEventListener('click', this.closeMenu);
      }
    },
  },
  created() {
    this.$root.$on('applyAdvancedFilter', this.applyAdvancedFilter);
    document.addEventListener('multiSelect', this.updateSelectedUsers);
  },
  beforeDestroy() {
    document.removeEventListener('multiSelect', this.updateSelectedUsers);
    this.$root.$off('applyAdvancedFilter', this.applyAdvancedFilter);
  },
  methods: {
    updateSelectedUsers(event) {
      this.usersSelected = event.detail.usersSelected;
    },
    multiSelectAction(action) {
      this.$root.$emit('multiSelectAction', action);
    },
    applyAdvancedFilter(filter) {
      this.filter = filter;
    },
    closeMenu() {
      if (this.menu) {
        this.menu = false;
      }
    },
    exportUsers() {
      this.exporting = true;
      return fetch(this.exportLink, {
        credentials: 'include'
      }).then(resp => resp?.ok && resp.json())
        .then(exportResult => this.exportId = exportResult.exportId)
        .then(() => document.dispatchEvent(new CustomEvent('alert-message', {detail: {
          alertComponent: 'users-management-export-csv-result',
          alertComponentParams: {
            options: {
              exportLink: this.exportLink,
              exportId: this.exportId,
              totalUsers: this.totalSize,
            },
          },
          alertDismissible: false,
          alertTimeout: 864000000,
        }})))
        .catch(error => this.$root.$emit('alert-message', error, 'error'))
        .finally(() => this.exporting = false);
    },
  }
};
</script>
