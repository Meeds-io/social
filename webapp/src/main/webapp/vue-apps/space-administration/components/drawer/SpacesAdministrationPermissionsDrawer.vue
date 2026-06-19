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
  <exo-drawer
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    class="spacePermissionsDrawer"
    right
    allow-expand>
    <template v-if="space" #title>
      {{ $t('social.spaces.administration.manageSpaces.permissionsDrawerTitle', {
        0: space.displayName
      }) }}
    </template>
    <template v-else-if="selectionCount" #title>
      {{ $t('social.spaces.administration.manageSpaces.spacesPermissionsDrawerTitle', {
        0: selectionCount
      }) }}
    </template>
    <template v-if="drawer && (space || spaces)" #content>
      <div class="pa-4 full-wdith">
        <div class="mb-4">
          {{ $t('social.spaces.administration.manageSpaces.permissionsDrawerDescription') }}
        </div>
        <template v-if="!loading">
          <div class="font-weight-bold">
            {{ $t('social.spaces.administration.manageSpaces.permissionsStepEditSpaceLayoutPermissionLabel') }}
          </div>
          <spaces-administration-permissions
            v-model="layoutPermissions"
            :space="space"
            class="mb-4"
            space-admin />
          <div class="font-weight-bold">
            {{ $t('social.spaces.administration.manageSpaces.permissionsStepPublicSitePermissionLabel') }}
          </div>
          <spaces-administration-permissions
            v-model="publicSitePermissions"
            :space="space"
            class="mb-4"
            space-admin />
          <div class="font-weight-bold">
            {{ $t('social.spaces.administration.manageSpaces.permissionsStepDeleteSpacePermissionLabel') }}
          </div>
          <spaces-administration-permissions
            v-model="deletePermissions"
            :space="space"
            class="mb-4"
            space-admin />
          <extension-registry-components
            :params="{
              'space' : space,
              'spaces': spaces
            }"
            name="SpacesAdministrationPermissions"
            type="form-permission-management"
            parent-element="div"
            element="div" />
        </template>
      </div>
    </template>
    <template v-if="!loading" #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          :title="$t('links.label.cancel')"
          class="btn ms-auto me-2"
          @click="close()">
          {{ $t('social.spaces.administration.manageSpaces.cancel') }}
        </v-btn>
        <v-btn
          :disabled="!modified"
          :loading="saving"
          class="btn-primary"
          elevation="0"
          @click="save">
          {{ $t('social.spaces.administration.delete.spaces.button.update') }}
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
    space: null,
    layoutPermissions: null,
    publicSitePermissions: null,
    deletePermissions: null,
    originalLayoutPermissions: null,
    originalPublicSitePermissions: null,
    originalDeletePermissions: null,
    spaces: null,
    selectionCount: null,
    callback: null,
    extendedFieldsModified: [],
    extendedPermissions: null,
    updatedExtendedPermissions: null,
    updatedPermissionCount: 0,
  }),
  computed: {
    modified() {
      return this.selectionCount
        || JSON.stringify(this.layoutPermissions) !== JSON.stringify(this.originalLayoutPermissions)
        || JSON.stringify(this.publicSitePermissions) !== JSON.stringify(this.originalPublicSitePermissions)
        || JSON.stringify(this.deletePermissions) !== JSON.stringify(this.originalDeletePermissions)
        || this.updatedPermissionCount > 0;
    },
  },
  created() {
    this.updatedExtendedPermissions = new Map();
    this.$root.$on('space-administration-permissions-drawer-open', this.open);
    this.$root.$on('space-administration-permissions-drawer-extended-field-updated', this.addUpdatedExtendedPermission);
    this.$root.$on('space-administration-permissions-drawer-extended-field-restored', this.removeUpdatedExtendedPermission);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-permissions-drawer-open', this.open);
    this.$root.$off('space-administration-permissions-drawer-extended-field-updated', this.addUpdatedExtendedPermission);
    this.$root.$off('space-administration-permissions-drawer-extended-field-restored', this.removeUpdatedExtendedPermission);

  },
  methods: {
    async open(obj, selectionCount, callback) {
      window.setTimeout(() => this.$refs.drawer.open(), 50);
      this.loading = true;
      try {
        if (obj?.id) {
          this.space = obj;
          this.spaces = null;
          this.selectionCount = 0;
          const permissions = await this.$spaceAdministrationService.getSpacePermission(this.space.id);
          this.originalLayoutPermissions = permissions.layoutPermissions;
          this.originalPublicSitePermissions = permissions.publicSitePermissions;
          this.originalDeletePermissions = permissions.deletePermissions;
          this.originalExtendedPermissions = permissions.extendedPermissions;
          this.layoutPermissions = JSON.parse(JSON.stringify(this.originalLayoutPermissions));
          this.publicSitePermissions = JSON.parse(JSON.stringify(this.originalPublicSitePermissions));
          this.deletePermissions = JSON.parse(JSON.stringify(this.originalDeletePermissions));
          this.extendedPermissions = JSON.parse(JSON.stringify(this.originalExtendedPermissions));
        } else {
          this.space = null;
          this.spaces = obj;
          this.selectionCount = selectionCount;
          this.callback = callback;
          this.originalLayoutPermissions = [];
          this.originalPublicSitePermissions = [];
          this.originalDeletePermissions = [];
          this.layoutPermissions = [];
          this.publicSitePermissions = [];
          this.deletePermissions = [];
          this.extendedPermissions = {};
        }
      } finally {
        this.loading = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
    async save() {
      this.saving = true;
      try {
        if (!this.extendedPermissions) {
          this.extendedPermissions = {};
        }
        if (this.updatedExtendedPermissions.size) {
          this.updatedExtendedPermissions.forEach((v, k) => {
            this.extendedPermissions[k] = v[k];
          });
        }
        if (this.callback) {
          this.callback({
            layoutPermissions: this.layoutPermissions,
            publicSitePermissions: this.publicSitePermissions,
            deletePermissions: this.deletePermissions,
            extendedPermissions: this.extendedPermissions,
          });
        } else {
          await this.$spaceAdministrationService.updateSpacePermissions(this.space.id, {
            layoutPermissions: this.layoutPermissions,
            publicSitePermissions: this.publicSitePermissions,
            deletePermissions: this.deletePermissions,
            extendedPermissions: this.extendedPermissions,
          });
          this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spacePermissionsUpdateSuccess'), 'success');
        }
        this.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spacePermissionsUpdateError'), 'error');
      } finally {
        this.saving = false;
      }
    },
    addUpdatedExtendedPermission(permission) {
      if (!this.updatedExtendedPermissions.has(permission.key) || this.updatedExtendedPermissions.get(permission.key)[permission.key] !== permission.value[permission.key]) {
        this.updatedExtendedPermissions.set(permission.key, permission.value);
        this.updatedPermissionCount ++;
      }
    },
    removeUpdatedExtendedPermission(permission) {
      if (this.updatedExtendedPermissions.has(permission.key)) {
        this.updatedExtendedPermissions.delete(permission.key);
        this.updatedPermissionCount --;
      }
    }
  },
};
</script>