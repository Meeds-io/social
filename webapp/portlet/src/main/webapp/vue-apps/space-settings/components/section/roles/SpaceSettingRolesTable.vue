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
    :headers="headers"
    :items="items"
    disable-pagination
    hide-default-footer
    must-sort
    class="table-layout-auto">
    <template slot="item" slot-scope="props">
      <space-setting-role-item
        :key="props.item.role"
        :name="props.item.name"
        :description="props.item.description"
        :users="props.item.users"
        :size="props.item.size" />
    </template>
  </v-data-table>
</template>
<script>
export default {
  data: () => ({
    isRedactionalSpace: false,
    members: null,
    managers: null,
    publishers: null,
    redactors: null,
  }),
  computed: {
    items() {
      const items = [{
        name: this.$t('SpaceSettings.roles.manager'),
        description: this.$t('SpaceSettings.roles.manager.description'),
        users: this.managers?.users,
        size: this.managers?.size,
        role: 'manager',
      },{
        name: this.$t('SpaceSettings.roles.publisher'),
        description: this.$t('SpaceSettings.roles.publisher.description'),
        users: this.publishers?.users,
        size: this.publishers?.size,
        role: 'manager',
      },{
        name: this.$t('SpaceSettings.roles.member'),
        description: this.$t('SpaceSettings.roles.member.description'),
        users: this.members?.users,
        size: this.members?.size,
        role: 'member',
      }];
      if (this.isRedactionalSpace) {
        items.splice(1, 0, {
          name: this.$t('SpaceSettings.roles.redactor'),
          description: this.$t('SpaceSettings.roles.redactor.description'),
          users: this.redactors?.users,
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
        class: 'actionHeader',
        width: '150'
      }, {
        text: this.$t('SpaceSettings.roles.table.users'),
        value: 'size',
        sortable: false,
        align: 'center',
        class: 'actionHeader',
        width: '150'
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
    this.init();
  },
  methods: {
    async init() {
      this.space = this.$root.space;
      if (!this.$root.spaceMembers) {
        this.$root.spaceMembers = await this.$spaceService.getSpaceMembers(null, 0, 3, '', 'member', this.space.id);
      }
      this.members = this.$root.spaceMembers;

      if (!this.$root.spaceManagers) {
        this.$root.spaceManagers = await this.$spaceService.getSpaceMembers(null, 0, 3, '', 'manager', this.space.id);
      }
      this.managers = this.$root.spaceManagers;

      if (!this.$root.spacePublishers) {
        this.$root.spacePublishers = await this.$spaceService.getSpaceMembers(null, 0, 3, '', 'publisher', this.space.id);
      }
      this.publishers = this.$root.spacePublishers;

      if (!this.$root.spaceRedactors) {
        this.$root.spaceRedactors = await this.$spaceService.getSpaceMembers(null, 0, 3, '', 'redactor', this.space.id);
      }
      this.redactors = this.$root.spacePublishers;
      this.isRedactionalSpace = !!this.redactors?.length;
    },
  },
};
</script>