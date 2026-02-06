<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io

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
  <div class="d-flex flex-column mb-4">
    <div class="d-flex py-2">
      <label for="spaceTemplateGroupsSelectorSwitch" class="flex-grow-1 text-truncate font-weight-bold">
        {{ $t('spaceTemplate.enclosingMembership.title') }}
      </label>
      <div class="position-relative mx-8">
        <v-switch
          id="spaceTemplateGroupsSelectorSwitch"
          v-model="canEncloseMembership"
          class="mb-0 mt-1 me-2 pa-0 r-0 absolute-vertical-center" />
      </div>
    </div>
    <template v-if="canEncloseMembership">
      <span class="text-body">{{ $t('spaceTemplate.enclosingMembership.label') }}</span>
      <exo-identity-suggester
        ref="spaceTemplateAssociatedGroups"
        v-model="groups"
        :labels="suggesterLabels"
        :ignore-items="ignoredItems"
        :search-options="{filterType: 'all'}"
        name="groups"
        class="mb-n3"
        include-groups
        all-groups-for-admin
        multiple />
    </template>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: String,
      default: null,
    }
  },
  data: () => ({
    canEncloseMembership: false,
    templateMembershipType: '*',
    inheritedMembershipType: '~',
    groups: null,
  }),
  computed: {
    suggesterLabels() {
      return {
        placeholder: this.$t('spaceTemplate.groupSuggester.placeholder'),
        noDataLabel: this.$t('spaceTemplate.groupSuggester.noData')
      };
    },
    enclosingMembership() {
      if (this.groups?.length) {
        return this.groups?.filter?.(g => g)?.map?.(g => `${this.templateMembershipType}:${this.inheritedMembershipType}:${g.groupId}`) || [];
      } else {
        return [];
      }
    },
    ignoredItems() {
      return this.$root.spaceTemplates?.filter(item => item?.groupId).map(item => {
        const groupId = item.groupId;
        const groupName = groupId.substring(groupId.lastIndexOf('/') + 1);
        return `group:${groupName}`;
      }) || [];
    }

  },
  watch: {
    enclosingMembership() {
      if (JSON.stringify(this.enclosingMembership) !== JSON.stringify(this.value)) {
        this.$emit('input', this.enclosingMembership.length && this.enclosingMembership || null);
      }
    },
  },
  created() {
    this.groups = [];
    const associatedGroups = this.value?.map(membership => membership.split(':')[2]).filter(Boolean);
    if (associatedGroups?.length) {
      associatedGroups.forEach(this.retrieveGroup);
      this.canEncloseMembership = true;
    }
  },
  methods: {
    async retrieveGroup(groupId) {
      const group = await this.$identityService.getIdentityByProviderIdAndRemoteId('group', groupId);
      if (group) {
        this.groups.push({
          id: `group:${group.remoteId}`,
          remoteId: group.remoteId,
          spaceId: groupId,
          groupId: groupId,
          providerId: 'group',
          displayName: group.profile?.fullname,
          profile: {
            fullName: group.profile?.fullname,
            originalName: group.profile?.fullname,
          },
        });
      }
    }
  },
};
</script>