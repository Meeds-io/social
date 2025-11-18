<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-app>
    <v-main class="application-body">
      <spaces-administration-toolbar
        :selected-template-id="$root.selectedTemplateId"
        @keyword-changed="$root.keyword = $event"
        @template-changed="$root.selectedTemplateId = $event"
        @loading="$root.loadingDisplay = $root.loadingDisplay || $event" />
      <spaces-administration-list
        ref="spacesList"
        class="px-5" />
    </v-main>
    <div>
      <component
        v-for="extension in $root.mainExtensions"
        :key="extension.name"
        :is="extension.componentName" />
    </div>
    <spaces-administration-filter-drawer />
    <spaces-administration-managers-drawer />
    <spaces-administration-sync-reports-drawer />
    <spaces-administration-sync-members-drawer />
    <spaces-administration-edit-categories-drawer />
    <spaces-administration-manage-relationships-drawer />
    <spaces-administration-permissions-drawer />
    <spaces-administration-apply-template-drawer />
    <space-form-drawer />
  </v-app>    
</template>
<script>
export default {
  data: () => ({
    keyword: null,
    loadingSpaces: false,
    initialized: false,
    selectedTemplateId: '0',
  }),
};
</script>
