<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <v-tooltip :disabled="canManageRelationships" bottom>
    <template #activator="{ on, attrs }">
      <div
        v-on="on"
        v-bind="attrs">
        <v-list-item
          :disabled="!canManageRelationships"
          dense
          @click="$root.$emit('space-administration-manage-relationships-drawer-open', space)">
          <v-card
            class="d-flex full-height justify-center"
            color="transparent"
            min-width="20"
            flat>
            <v-icon
              :class="!canManageRelationships && 'disabled--text'"
              size="16">
              fas fa-link
            </v-icon>
          </v-card>
          <v-list-item-title class="ps-2">
            <span :class="!canManageRelationships && 'disabled--text'">{{ $t('social.spaces.administration.manageSpaces.manageRelationships') }}</span>
          </v-list-item-title>
        </v-list-item>
      </div>
    </template>
    <span>{{ $t('social.spaces.administration.manageSpaces.manageRelationships.noParentSpaceTemplate') }}</span>
  </v-tooltip>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
  computed: {
    spaceTemplateId() {
      return Number(this.space?.templateId);
    },
    parentSpaceTemplates() {
      // Same rule as SpaceTemplateService.getTemplateIdsAllowingSubspaces,
      // which bounds the parent spaces the drawer's suggester can list
      return this.$root.spaceTemplates?.filter?.(t => t.enabled
        && !t.deleted
        && t.allowedSubspaceTemplates?.length) || [];
    },
    canManageRelationships() {
      return !!this.space?.parentSpaceId
        || this.parentSpaceTemplates.some(t => t.id === this.spaceTemplateId
          || t.allowedSubspaceTemplates.some(entry => Number(String(entry).split(':')[0]) === this.spaceTemplateId));
    },
  },
};
</script>
