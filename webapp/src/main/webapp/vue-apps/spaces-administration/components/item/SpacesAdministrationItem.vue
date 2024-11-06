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
  <tr>
    <!-- name -->
    <td
      :width="$root.isMobile && '100%' || 'auto'"
      align="left">
      <space-avatar
        :space="space"
        link-target="_blank"
        link-style />
    </td>
    <!-- description -->
    <td
      v-if="!$root.isMobile"
      :title="description"
      align="left">
      <span v-sanitized-html="description" class="text-truncate-2"></span>
    </td>
    <td
      v-if="!$root.isMobile"
      :title="!spaceTemplateName && $t('social.spaces.administration.manageSpaces.noTemplate')"
      align="left"
      class="text-center"
      width="70px">
      <template v-if="spaceTemplateName">
        {{ spaceTemplateName }}
      </template>
      <span v-else>-</span>
    </td>
    <td
      v-if="!$root.isMobile"
      align="center"
      width="50px">
      {{ $t(`social.spaces.administration.manageSpaces.registration.${space.subscription}`) }}
    </td>
    <td
      v-if="!$root.isMobile"
      align="center"
      width="50px">
      {{ $t(`social.spaces.administration.manageSpaces.visibility.${space.visibility}`) }}
    </td>
    <td
      align="center"
      width="50px"
      class="position-relative">
      <exo-user-avatars-list
        :users="sortedManagers"
        :default-length="space.managersCount"
        :margin-left="space.managersCount > 1 && 'ml-n5' || ''"
        :icon-size="33"
        :max="3"
        :class="$vuetify.rtl && 'l-0' || 'r-0'"
        class="absolute-vertical-center"
        compact
        clickable
        popover
        @open-detail="$root.$emit('space-administration-managers-drawer-open', sortedManagers, space.displayName)" />
    </td>
    <td
      align="center"
      width="50px">
      <number-format :value="space.membersCount" />
    </td>
    <td
      align="center"
      width="50px">
      <v-btn
        :title="bindingStatusTitle"
        icon>
        <v-icon :class="boundToGroup && 'success--text'" size="20">fa-users</v-icon>
      </v-btn>
    </td>
    <td
      align="center"
      width="50px">
      -
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
    description() {
      return this.$utils.htmlToText(this.space?.description || '');
    },
    sortedManagers() {
      const managers = this.space.managers;
      managers.sort((a, b) => this.$root.collator.compare(a.fullname.toLowerCase(), b.fullname.toLowerCase()));
      return managers;
    },
    boundToGroup() {
      return this.space.hasBinding;
    },
    totalBoundUsers() {
      return this.space.totalBoundUsers > 1000 ? `${parseInt(this.space.totalBoundUsers)}K` : this.space.totalBoundUsers || 0;
    },
    bindingStatusTitle() {
      return this.boundToGroup ? this.$t('social.spaces.administration.manageSpaces.bindingStatus.tooltip', {0: this.totalBoundUsers}) : this.$t('social.spaces.administration.manageSpaces.noBindingStatus.tooltip');
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