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
  <tr :key="space.id">
    <td
      align="left"
      :width="headers[0].width">
      <space-avatar
        :space="space"
        text-truncate-class="text-truncate-2"
        link-target="_blank"
        link-style />
    </td>
    <template v-if="!$root.isMobile">
      <td
        v-for="extension in $root.tableColumnExtensions"
        :key="extension.name"
        :width="extension.header.width"
        :align="extension.header.align || 'left'">
        <component
          :is="extension.componentName"
          :space="space" />
      </td>
    </template>
    <td
      v-if="!$root.isMobile"
      :title="!spaceTemplateIcon && $t('social.spaces.administration.manageSpaces.noTemplate')"
      :width="!$root.isMobile && headers[extensionsSize + 1].width"
      align="center">
      <v-tooltip
        v-if="spaceTemplateIcon"
        bottom>
        <template #activator="{attrs, on}">
          <v-icon
            v-on="on"
            v-bind="attrs"
            :aria-label="spaceTemplateName"
            size="20">
            {{ spaceTemplateIcon }}
          </v-icon>
        </template>
        {{ spaceTemplateName }}
      </v-tooltip>
      <span v-else>-</span>
    </td>
    <td
      v-if="!$root.isMobile"
      :width="!$root.isMobile && headers[extensionsSize + 2].width"
      align="center"
      class="position-relative text-no-wrap">
      <exo-user-avatars-list
        :users="sortedManagers"
        :default-length="space.managersCount"
        :margin-left="space.managersCount > 1 && 'ml-n5' || ''"
        :icon-size="33"
        :max="3"
        class="absolute-all-center"
        compact
        clickable
        popover
        @open-detail="$root.$emit('space-administration-managers-drawer-open', sortedManagers, space.displayName)" />
    </td>
    <td
      :width="headers[$root.isMobile && 1 || (extensionsSize + 3)].width"
      align="center">
      <number-format
        :title="space.membersCount"
        :value="space.membersCount"
        use-k-suffix />
    </td>
    <td
      v-if="!$root.isMobile"
      :width="!$root.isMobile && headers[extensionsSize + 4].width"
      align="center">
      <v-btn
        :title="bindingStatusTitle"
        icon
        @click="$root.$emit('space-administration-sync-members-drawer-open', space)">
        <v-icon :class="boundToGroup && 'success--text'" size="20">fa-users</v-icon>
      </v-btn>
    </td>
    <td
      :width="headers[$root.isMobile && 2 || (extensionsSize + 5)].width"
      align="center">
      <spaces-administration-item-menu :space="space" />
    </td>
  </tr>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
    headers: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    hoverMenu: false,
  }),
  computed: {
    spaceTemplate() {
      return this.$root.spaceTemplates?.find?.(t => t.id === Number(this.space.templateId));
    },
    spaceTemplateName() {
      return this.spaceTemplate?.name;
    },
    spaceTemplateIcon() {
      return this.spaceTemplate?.icon;
    },
    sortedManagers() {
      const managers = this.space.managers;
      managers.sort((a, b) => this.$root.collator.compare(a.fullname.toLowerCase(), b.fullname.toLowerCase()));
      return managers;
    },
    boundToGroup() {
      return this.space.hasBindings;
    },
    totalBoundUsers() {
      return this.space.totalBoundUsers > 1000 ? `${parseInt(this.space.totalBoundUsers / 1000)}k` : this.space.totalBoundUsers || 0;
    },
    bindingStatusTitle() {
      return this.boundToGroup ? this.$t('social.spaces.administration.manageSpaces.bindingStatus.tooltip', {0: this.totalBoundUsers}) : this.$t('social.spaces.administration.manageSpaces.noBindingStatus.tooltip');
    },
    extensionsSize() {
      return this.$root.tableColumnExtensions.length;
    },
  },
  watch: {
    hoverMenu() {
      if (!this.hoverMenu) {
        window.setTimeout(() => {
          if (!this.hoverMenu) {
            this.menu = false;
          }
        }, 200);
      }
    },
  },
};
</script>