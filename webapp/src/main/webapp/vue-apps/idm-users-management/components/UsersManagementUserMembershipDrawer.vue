<template>
  <exo-drawer
    id="userMembershipDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    allow-expand
    right>
    <template #title>{{ $t('UsersManagement.editMembershipsOfUser', {0: user?.fullname}) }}</template>
    <template #content>
      <application-toolbar
        v-if="!$root.isDelegatedAdministrator"
        class="px-1 mt-4"
        compact>
        <template #left>
          <v-btn
            :disabled="displayAddGroup"
            color="primary"
            elevation="0"
            @click="displayAddGroup = true">
            {{ $t('UsersManagement.add') }}
          </v-btn>
        </template>
      </application-toolbar>
      <v-expand-y-transition v-if="!$root.isDelegatedAdministrator">
        <identity-suggester
          v-if="displayAddGroup"
          ref="groupInput"
          v-model="selectedGroup"
          :labels="suggesterLabels"
          :search-options="{filterType: 'all'}"
          class="mx-5"
          include-spaces
          include-groups
          all-groups-for-admin />
      </v-expand-y-transition>
      <div
        v-if="initialized && !membershipsArray.length"
        class="d-flex align-center justify-center ma-5">
        <v-icon size="20" class="me-5">fa-users</v-icon>
        {{ $t('UsersManagement.noMemberships') }}
      </div>
      <v-list
        :loading="loading"
        class="px-4">
        <v-list-item
          v-for="g in membershipsArray"
          :key="g.id"
          class="pa-1 pb-1"
          dense>
          <v-list-item-action class="pa-0 ma-0">
            <select
              v-model="g.membershipType"
              :disabled="$root.isDelegatedAdministrator"
              aria-label="hidden"
              class="ignore-vuetify-classes width-auto pa-0 ma-0"
              @change="setAsModified">
              <option
                v-for="role in rolesToDisplay"
                :key="role.value"
                :value="role.name">
                {{ role.label }}
              </option>
            </select>
          </v-list-item-action>
          <v-list-item-content class="d-flex align-center pa-0">
            <v-list-item-title class="d-flex align-center text-truncate">
              <div class="px-2">
                {{ $t('UsersManagement.of') }}
              </div>
              <space-avatar
                v-if="g.isSpace"
                :space-group-id="g.groupId"
                class="text-truncate" />
              <template v-else>
                <v-icon size="28" class="me-2">
                  fa-users
                </v-icon>
                <span class="text-truncate">
                  {{ g.groupLabel }}
                </span>
              </template>
            </v-list-item-title>
          </v-list-item-content>
          <v-list-item-action v-if="!$root.isDelegatedAdministrator" class="pa-0 my-auto">
            <v-btn
              :title="$t('siteNavigation.label.deleteCustomGroup')"
              icon
              @click.stop.prevent="removeMembership(g)">
              <v-icon color="error" small>fa-trash</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>
        <v-list-item v-if="hasMore" dense>
          <v-list-item-content class="pa-0 mt-4">
            <v-list-item-title class="d-flex align-center justify-center">
              <v-btn
                :loading="loading"
                class="btn"
                elevation="0"
                outlined
                @click="loadMore">
                {{ $t('UsersManagement.loadMore') }}
              </v-btn>
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </template>
    <template v-if="!$root.isDelegatedAdministrator" #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :disabled="loading"
          class="btn me-2"
          @click="close">
          {{ $t('SpaceSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="saving"
          class="btn btn-primary"
          @click.prevent.stop="apply">
          {{ $t('SpaceSettings.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    loading: false,
    saving: false,
    modified: false,
    displayAddGroup: false,
    selectedGroup: null,
    defaultRole: '*',
    pageSize: Math.max(2, Math.round((window.innerHeight - 250) / 48)),
    page: 1,
    size: -1,
    offset: 0,
    user: {},
    roles: [],
    memberships: [],
  }),
  computed: {
    userName() {
      return this.user?.userName;
    },
    rolesToDisplay() {
      return this.roles.map(r => ({
        name: r.name,
        label: this.$te(`UsersManagement.role.${r.name}`) ? this.$t(`UsersManagement.role.${r.name}`) : r.name,
      }));
    },
    membershipsArray() {
      return this.memberships?.map?.(m => {
        m.isSpace = m.groupId?.startsWith?.('/spaces/');
        return m;
      }) || [];
    },
    initialized() {
      return this.size !== -1;
    },
    hasMore() {
      return this.size > this.memberships.length;
    },
    suggesterLabels() {
      return {
        placeholder: this.$t('UsersManagement.groupSuggester.placeholder'),
        noDataLabel: this.$t('UsersManagement.groupSuggester.noData')
      };
    },
  },
  watch: {
    selectedGroup() {
      if (this.selectedGroup) {
        this.memberships.unshift({
          groupId: this.selectedGroup.groupId,
          groupLabel: this.selectedGroup.displayName,
          id: `${this.defaultRole}:${this.user?.userName}:${this.selectedGroup.groupId}`,
          membershipType: this.defaultRole,
          userName: this.user?.userName,
        });
        this.selectedGroup.profile = null;
        window.setTimeout(() => this.selectedGroup = null, 10);
      }
    },
    memberships: {
      deep: true,
      handler() {
        this.modified = true;
      },
    },
  },
  created() {
    this.$root.$on('openUserMemberships', this.open);
    this.init();
  },
  methods: {
    setAsModified() {
      this.modified = true;
    },
    async init() {
      this.loading = true;
      try {
        const resp = await fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/membershipTypes`, {
          method: 'GET',
          credentials: 'include',
        });
        if (resp?.ok) {
          const data = await resp.json();
          this.roles = data || [];
        } else {
          this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error');
        }
      } finally {
        this.loading = false;
      }
    },
    open(user) {
      this.offset = 0;
      this.page = 1;
      this.user = user;
      this.size = -1;
      this.memberships = [];
      this.originalMemberships = [];
      this.displayAddGroup = false;
      this.retrieveList()
        .then(() => this.$nextTick())
        .then(() => this.modified = false);
      this.$refs.drawer.open();
    },
    close() {
      this.$refs.drawer.close();
    },
    loadMore() {
      this.page++;
      this.retrieveList();
    },
    removeMembership(membership) {
      this.memberships = this.memberships.filter(m => m.groupId !== membership.groupId || m.membershipType !== membership.membershipType);
      this.size--;
    },
    retrieveList() {
      const offset = (this.page - 1) * this.pageSize;
      this.loading = true;
      return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/users/${this.userName}/memberships?offset=${offset}&limit=${this.pageSize}&returnSize=${!this.initialized}`, {
        method: 'GET',
        credentials: 'include',
      }).then(resp => {
        if (!resp || !resp.ok) {
          throw new Error(this.$t('IDMManagement.error.UnknownServerError'));
        } else {
          return resp.json();
        }
      }).then(memberships => {
        const sortedMemberships = memberships?.entities || [];
        sortedMemberships?.sort((m1, m2) => m1.groupLabel?.localeCompare(m2.groupLabel));
        this.memberships.push(...sortedMemberships);
        this.originalMemberships = JSON.parse(JSON.stringify(this.memberships));
        if (!this.initialized) {
          this.size = memberships?.size || 0;
        }
      })
        .finally(() => this.loading = false);
    },
    async apply() {
      this.saving = true;
      try {
        const newMemberships = this.memberships.filter(m => !this.originalMemberships.find(om => om.groupId === m.groupId && om.membershipType === m.membershipType));
        if (newMemberships?.length) {
          await Promise.all(newMemberships.map(this.createMembership));
        }
        const toDeleteMemberships = this.originalMemberships.filter(m => !this.memberships.find(om => om.groupId === m.groupId && om.membershipType === m.membershipType));
        if (toDeleteMemberships?.length) {
          await Promise.all(toDeleteMemberships.map(this.deleteMembership));
        }
        this.close();
      } finally {
        this.saving = false;
      }
    },
    async createMembership(membership) {
      const resp = await fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(membership),
      });
      if (!resp?.ok) {
        if (resp.status === 400) {
          return resp.text()
            .then(error => this.$root.$emit('alert-message', error, 'error'));
        } else {
          this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error');
        }
      }
    },
    async deleteMembership(membership) {
      const resp = await fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/groups/memberships?membershipId=${membership.membershipType}:${this.user.userName}:${membership.groupId}`, {
        method: 'DELETE',
        credentials: 'include',
      });
      if (!resp?.ok) {
        if (resp.status === 400) {
          return resp.text()
            .then(error => this.$root.$emit('alert-message', error, 'error'));
        } else {
          this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error');
        }
      }
    },
  },
};
</script>