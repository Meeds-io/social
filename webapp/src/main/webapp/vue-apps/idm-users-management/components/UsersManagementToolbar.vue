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
        <div
          v-if="!isDelegatedAdministrator"
          :class="dropdown && 'open' || ''"
          class="btn-group">
          <v-btn
            class="btn btn-primary addNewUserButton"
            @click="$root.$emit('addNewUser')">
            <i class="uiIconAddUser uiIconWhite me-md-3"></i>
            <span class="d-none d-sm-inline">
              {{ $t('UsersManagement.addUser') }}
            </span>
          </v-btn>
          <v-btn
            class="btn btn-primary dropdown-toggle width-auto pa-0"
            @click.prevent.stop="dropdown = true">
            <span class="caret my-0 mx-3"></span>
          </v-btn>
          <div v-if="initialized" class="dropdown-menu">
            <users-management-import-csv-button />
          </div>
        </div>
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
    dropdown: false,
    exportId: null,
    exporting: false,
    isDelegatedAdministrator: true,
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
    dropdown() {
      if (this.dropdown) {
        document.addEventListener('click', this.closeDropdown);
      } else {
        document.removeEventListener('click', this.closeDropdown);
      }
    },
  },
  created() {
    this.$root.$on('applyAdvancedFilter', this.applyAdvancedFilter);
    document.addEventListener('multiSelect', this.updateSelectedUsers);
    this.init();
  },
  updated() {
    // Workaround to hide DropDown Menu on initialization
    // that causes html breaking sometimes
    window.setTimeout(() => this.initialized = true, 1000);
  },
  beforeDestroy() {
    document.removeEventListener('multiSelect', this.updateSelectedUsers);
    this.$root.$off('applyAdvancedFilter', this.applyAdvancedFilter);
  },
  methods: {
    async init() {
      const data = await this.$userService.isDelegatedAdministrator();
      this.isDelegatedAdministrator = data.result === 'true';
    },
    updateSelectedUsers(event) {
      this.usersSelected = event.detail.usersSelected;
    },
    multiSelectAction(action) {
      this.$root.$emit('multiSelectAction', action);
    },
    applyAdvancedFilter(filter) {
      this.filter = filter;
    },
    closeDropdown() {
      if (this.dropdown) {
        this.dropdown = false;
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
