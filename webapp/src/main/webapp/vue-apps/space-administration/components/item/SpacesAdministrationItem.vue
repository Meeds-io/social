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
    <td align="center">
      <v-tooltip
        v-if="bulkOperationStatus === 'done'"
        top>
        <template #activator="{on, attrs}">
          <v-icon
            v-bind="attrs"
            class="fa-lg success--text my-auto pt-2"
            v-on="on">
            fa-check-circle
          </v-icon>
        </template>
        <span>{{ $t('social.spaces.administration.manageSpaces.successSpacesBulkOperation') }}</span>
      </v-tooltip>
      <v-tooltip
        v-else-if="bulkOperationStatus === 'error'"
        top>
        <template #activator="{on, attrs}">
          <v-icon
            v-bind="attrs"
            class="fa-lg error--text my-auto pt-2"
            v-on="on">
            fa-exclamation-circle
          </v-icon>
        </template>
        <span>{{ $t('social.spaces.administration.manageSpaces.errorSpacesBulkOperation') }}</span>
      </v-tooltip>
      <v-tooltip
        v-else-if="bulkOperationStatus === 'processing'"
        top>
        <template #activator="{on, attrs}">
          <v-icon
            v-bind="attrs"
            class="fa-lg primary--text my-auto pt-2"
            v-on="on">
            fa-spinner
          </v-icon>
        </template>
        <span>{{ $t('social.spaces.administration.manageSpaces.processingSpacesBulkOperation') }}</span>
      </v-tooltip>
      <v-checkbox
        v-else
        class="my-auto pt-2"
        :disabled="bulkOperationStatus === 'disabled'"
        off-icon="far fa-square fa-lg"
        on-icon="fas fa-check-square fa-lg primary--text"
        :value="selected || $root.allSpacesSelected"
        @change="changeCheckboxStatus" />
    </td>
    <td
      align="left"
      :width="headers[0].width">
      <space-avatar
        link-style
        link-target="_blank"
        :space="space"
        text-truncate-class="text-truncate-2" />
    </td>
    <template v-if="!$root.isMobile">
      <td
        v-for="extension in $root.tableColumnExtensions"
        :key="extension.name"
        :align="extension.header.align || 'left'"
        :width="extension.header.width">
        <component
          :is="extension.componentName"
          :space="space" />
      </td>
    </template>
    <td
      v-if="!$root.isMobile"
      align="center"
      :title="!spaceTemplateIcon && $t('social.spaces.administration.manageSpaces.noTemplate')"
      :width="!$root.isMobile && headers[extensionsSize + 1].width">
      <v-tooltip
        v-if="spaceTemplateIcon"
        bottom>
        <template #activator="{attrs, on}">
          <v-icon
            v-bind="attrs"
            :aria-label="spaceTemplateName"
            size="20"
            v-on="on">
            {{ spaceTemplateIcon }}
          </v-icon>
        </template>
        {{ spaceTemplateName }}
      </v-tooltip>
      <span v-else>-</span>
    </td>
    <td
      v-if="!$root.isMobile"
      align="center"
      class="position-relative text-no-wrap"
      :width="!$root.isMobile && headers[extensionsSize + 2].width">
      <exo-user-avatars-list
        class="absolute-all-center"
        clickable
        compact
        :default-length="space.managersCount"
        :icon-size="33"
        :margin-left="space.managersCount > 1 && 'ml-n5' || ''"
        :max="3"
        popover
        :users="sortedManagers"
        @open-detail="$root.$emit('space-administration-managers-drawer-open', sortedManagers, space.displayName)" />
    </td>
    <td
      align="center"
      :width="headers[$root.isMobile && 1 || (extensionsSize + 3)].width">
      <number-format
        :title="space.membersCount"
        use-k-suffix
        :value="space.membersCount" />
    </td>
    <td
      v-if="!$root.isMobile"
      align="center"
      :width="!$root.isMobile && headers[extensionsSize + 4].width">
      <v-btn
        :disabled="$root.isBulkProcessing"
        icon
        :title="bindingStatusTitle"
        @click="$root.$emit('space-administration-sync-members-drawer-open', space)">
        <v-icon
          :class="boundToGroup && 'success--text'"
          size="20">
          fa-users
        </v-icon>
      </v-btn>
    </td>
    <td
      align="center"
      :width="headers[$root.isMobile && 2 || (extensionsSize + 5)].width">
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
      selected: {
        type: Boolean,
        default: false,
      },
      select: {
        type: Object,
        default: null,
      },
    },
    data: () => ({
      menu: false,
      hoverMenu: false,
      bulkOperationStatus: null,
    }),
    computed: {
      spaceTemplate () {
        return this.$root.spaceTemplates?.find?.(t => t.id === Number(this.space.templateId));
      },
      spaceTemplateName () {
        return this.spaceTemplate?.name;
      },
      spaceTemplateIcon () {
        return this.spaceTemplate?.icon;
      },
      sortedManagers () {
        const managers = this.space.managers.slice();
        managers.sort((a, b) => this.$root.collator.compare(a.fullname.toLowerCase(), b.fullname.toLowerCase()));
        return managers;
      },
      boundToGroup () {
        return this.space.hasBindings;
      },
      totalBoundUsers () {
        return this.space.totalBoundUsers > 1000 ? `${parseInt(this.space.totalBoundUsers / 1000)}k` : this.space.totalBoundUsers || 0;
      },
      bindingStatusTitle () {
        return this.boundToGroup ? this.$t('social.spaces.administration.manageSpaces.bindingStatus.tooltip', { 0: this.totalBoundUsers }) : this.$t('social.spaces.administration.manageSpaces.noBindingStatus.tooltip');
      },
      extensionsSize () {
        return this.$root.tableColumnExtensions.length;
      },
      selectedSpacesOperationStatus () {
        return this.$root.selectedSpacesOperationStatus;
      },
    },
    watch: {
      bulkOperationStatus () {
        if (this.bulkOperationStatus === 'processing') {
          const dimensions = this.$el.getBoundingClientRect();
          if ((dimensions.top + dimensions.height) > (window.innerHeight - 120)) {
            this.$el.scrollIntoView({
              behavior: 'smooth',
              block: 'start',
            });
          }
        }
      },
      hoverMenu () {
        if (!this.hoverMenu) {
          window.setTimeout(() => {
            if (!this.hoverMenu) {
              this.menu = false;
            }
          }, 200);
        }
      },
    },
    created () {
      this.$root.$on('spaces-administration-bulk-operation-status', this.changeProcessingStatus);
    },
    beforeUnmount () {
      this.$root.$off('spaces-administration-bulk-operation-status', this.changeProcessingStatus);
    },
    methods: {
      changeProcessingStatus (id, status) {
        if (!id || (id === this.space.id)) {
          this.bulkOperationStatus = status;
        }
      },
      changeCheckboxStatus (status) {
        if (!this.$root.isBulkProcessing) {
          this.select(status);
        }
      },
    },
  };
</script>