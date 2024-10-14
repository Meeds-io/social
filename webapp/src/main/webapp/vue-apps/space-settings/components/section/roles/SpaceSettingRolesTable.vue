<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-data-table
    :loading="!!loading"
    :headers="headers"
    :items="items"
    :headers-length="headers.length"
    class="table-layout-auto"
    hide-default-footer
    hide-default-header
    disable-pagination
    fixed-header
    must-sort>
    <template #header>
      <thead class="v-data-table-header">
        <tr>
          <th
            :aria-label="$t('SpaceSettings.roles.table.role')"
            class="text-start actionHeader ps-0"
            style="min-width: 150px;">
            <span>{{ $t('SpaceSettings.roles.table.role') }}</span>
          </th>
          <th
            v-if="!$root.isMobile"
            :aria-label="$t('SpaceSettings.roles.table.description')"
            class="text-start actionHeader">
            <span>{{ $t('SpaceSettings.roles.table.description') }}</span>
          </th>
          <th
            :aria-label="$t('SpaceSettings.roles.table.users')"
            class="text-start actionHeader pe-0"
            style="min-width: 135px;">
            <span>{{ $t('SpaceSettings.roles.table.users') }}</span>
          </th>
        </tr>
      </thead>
    </template>
    <template slot="item" slot-scope="props">
      <space-setting-roles-table-item
        :key="props.item.role"
        :name="props.item.name"
        :description="props.item.description"
        :role="props.item.role"
        :users="props.item.users"
        :size="props.item.size" />
    </template>
  </v-data-table>
</template>
<script>
export default {
  data: () => ({
    loading: 0,
    isContentCreationRestricted: false,
    members: null,
    managers: null,
    publishers: null,
    redactors: null,
    expand: 'users,settings',
  }),
  computed: {
    items() {
      const items = [{
        name: this.$t('SpaceSettings.roles.manager'),
        description: this.$t('SpaceSettings.roles.manager.description'),
        users: this.managers?.spacesMemberships?.map?.(m => m.user),
        size: this.managers?.size,
        role: 'manager',
      },{
        name: this.$t('SpaceSettings.roles.publisher'),
        description: this.$t('SpaceSettings.roles.publisher.description'),
        users: this.publishers?.spacesMemberships?.map?.(m => m.user),
        size: this.publishers?.size,
        role: 'publisher',
      },{
        name: this.$t('SpaceSettings.roles.member'),
        description: this.$t('SpaceSettings.roles.member.description'),
        users: this.members?.spacesMemberships?.map?.(m => m.user),
        size: this.members?.size,
        role: 'member',
      }];
      if (this.isContentCreationRestricted) {
        items.splice(2, 0, {
          name: this.$t('SpaceSettings.roles.redactor'),
          description: this.$t('SpaceSettings.roles.redactor.description'),
          users: this.redactors?.spacesMemberships?.map?.(m => m.user),
          size: this.redactors?.size,
          role: 'redactor',
        });
      }
      return items;
    },
    headers() {
      const headers = [{
        text: this.$t('SpaceSettings.roles.table.role'),
        sortable: false,
        value: 'name',
        class: 'actionHeader ps-0',
        width: '150'
      }, {
        text: this.$t('SpaceSettings.roles.table.users'),
        value: 'size',
        sortable: false,
        align: 'center',
        class: 'actionHeader',
        width: '135'
      }];
      if (!this.$root.isMobile) {
        headers.splice(1, 0, {
          text: this.$t('SpaceSettings.roles.table.description'),
          value: 'description',
          sortable: false,
          align: 'center',
          class: 'actionHeader',
          width: 'auto'
        });
      }
      return headers;
    },
  },
  created() {
    this.$root.$on('space-settings-refresh-managers', this.refreshManagers);
    this.$root.$on('space-settings-refresh-publishers', this.refreshPublishers);
    this.$root.$on('space-settings-refresh-redactors', this.refreshRedactors);
    this.$root.$on('space-settings-refresh-members', this.refreshMembers);

    this.$root.$on('space-settings-managers-updated', this.refreshManagers);
    this.$root.$on('space-settings-publishers-updated', this.refreshPublishers);
    this.$root.$on('space-settings-redactors-updated', this.refreshRedactors);
    this.$root.$on('space-settings-members-updated', this.refreshMembers);

    this.init();
  },
  beforeDestroy() {
    this.$root.$off('space-settings-refresh-managers', this.refreshManagers);
    this.$root.$off('space-settings-refresh-publishers', this.refreshPublishers);
    this.$root.$off('space-settings-refresh-redactors', this.refreshRedactors);
    this.$root.$off('space-settings-refresh-members', this.refreshMembers);

    this.$root.$off('space-settings-managers-updated', this.refreshManagers);
    this.$root.$off('space-settings-publishers-updated', this.refreshPublishers);
    this.$root.$off('space-settings-redactors-updated', this.refreshRedactors);
    this.$root.$off('space-settings-members-updated', this.refreshMembers);
  },
  methods: {
    init() {
      this.space = this.$root.space;
      this.refreshManagers();
      this.refreshPublishers();
      this.refreshRedactors();
      this.refreshMembers();
    },
    async refreshManagers() {
      this.loading++;
      try {
        // query, offset, limit, expand, role, spaceId
        // null, 0, 3, this.expand, 'manager', this.space.id
        this.managers = await this.getSpaceMemberships('manager');
        this.$emit('managers-loaded', this.managers);
      } finally {
        this.loading--;
      }
    },
    async refreshPublishers() {
      this.loading++;
      try {
        this.publishers = await this.getSpaceMemberships('publisher');
        this.$emit('publishers-loaded', this.publishers);
      } finally {
        this.loading--;
      }
    },
    async refreshRedactors() {
      this.loading++;
      try {
        this.redactors = await this.getSpaceMemberships('redactor');
        this.$emit('redactors-loaded', this.redactors);

        this.isContentCreationRestricted = !!this.redactors?.size;
        this.$emit('restriction-loaded', this.isContentCreationRestricted);
      } finally {
        this.loading--;
      }
    },
    async refreshMembers() {
      this.loading++;
      try {
        this.members = await this.getSpaceMemberships('member');
        this.$emit('members-loaded', this.members);
      } finally {
        this.loading--;
      }
    },
    getSpaceMemberships(role) {
      return this.$spaceService.getSpaceMemberships({
        offset: 0,
        limit: 3,
        status: role,
        expand: this.expand,
        space: this.space.id,
        returnSize: true,
      });
    },
  },
};
</script>