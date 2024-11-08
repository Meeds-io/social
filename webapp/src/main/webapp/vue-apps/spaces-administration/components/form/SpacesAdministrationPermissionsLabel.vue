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
  <div class="d-flex flex-column">
    {{ permissionLabels }}
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    },
    spaceAdminMembershipType: {
      type: String,
      default: () => 'spaceAdmin',
    },
  },
  data: () => ({
    isUsersPermissions: false,
    isExternalsPermissions: false,
    isSpaceAdminPermissions: false,
    isCustomPermissions: false,
    specificGroupEntries: null,
  }),
  computed: {
    permissionLabels() {
      const permissionLabels = [];
      permissionLabels.push(this.$t('social.spaces.administration.manageSpaces.permissionsStepAdministrators'));
      if (this.isUsersPermissions) {
        permissionLabels.push(this.$t('social.spaces.administration.manageSpaces.permissionsStepUsers'));
      }
      if (this.isExternalsPermissions) {
        permissionLabels.push(this.$t('social.spaces.administration.manageSpaces.permissionsStepExternals'));
      }
      if (this.isSpaceAdminPermissions) {
        permissionLabels.push(this.$t('social.spaces.administration.manageSpaces.permissionsStepSpaceAdmins'));
      }
      if (this.specificGroupEntries?.length) {
        const specificGroupEntries = this.specificGroupEntries?.map?.(g => g.name)?.filter?.(g => g) || [];
        permissionLabels.push(...specificGroupEntries);
      }
      return permissionLabels.join(', ');
    },
  },
  created() {
    this.refreshSelection();
  },
  methods: {
    refreshSelection() {
      const permissions = this.value?.slice?.() || [];

      this.isUsersPermissions = permissions.find(p => p === this.$root.usersPermission) ? true : false;
      this.isExternalsPermissions = permissions.find(p => p === this.$root.externalsPermission) ? true : false;
      this.isSpaceAdminPermissions = permissions.find(p => p === this.spaceAdminMembershipType) && true || false;
      this.specificGroupEntries = [];

      const specificGroupEntries = permissions?.filter?.(p =>
        p !== this.$root.administratorsPermission
        && (!p.includes(':') || p.split(':')[1] !== this.$root.administratorsPermission)
        && p !== this.$root.usersPermission
        && p !== this.$root.externalsPermission
        && p !== this.spaceAdminMembershipType
      ) || null;
      this.isCustomPermissions = !!specificGroupEntries?.length;
      if (specificGroupEntries?.length) {
        specificGroupEntries.forEach(this.retrieveObject);
      }
    },
    async retrieveObject(groupId) {
      groupId = groupId.includes(':') ? groupId.split(':')[1] : groupId;
      if (groupId.indexOf('/spaces/') === 0) {
        const space = await this.$spaceService.getSpaceByGroupId(groupId);
        if (space) {
          this.specificGroupEntries.push({
            groupId: space.groupId,
            name: space.displayName,
          });
        }
      } else {
        const group = await this.$identityService.getIdentityByProviderIdAndRemoteId('group', groupId);
        if (group) {
          this.specificGroupEntries.push({
            groupId: groupId,
            name: group.profile?.fullname,
          });
        }
      }
    },
  },
};
</script>