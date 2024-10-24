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
  <v-app class="application-body">
    <v-main v-if="$root.space" class="px-5 py-1 border-box-sizing">
      <space-setting-overview id="SpaceSettingsOverview" />
      <space-setting-roles id="SpaceSettingsRoles" />
      <space-setting-access id="SpaceSettingsAccess" />
      <space-setting-public-site id="SpaceSettingsPublicAccess" />
      <template>
        <extension-registry-components
          :params="extensionParams"
          name="SpaceSettings"
          type="space-settings-components"
          parent-element="div"
          element="div" />
      </template>
    </v-main>
  </v-app>
</template>
<script>
export default {
  computed: {
    extensionParams() {
      return {
        spaceId: this.$root.spaceId,
      };
    },
  },
};
</script>
