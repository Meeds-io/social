<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2026 Meeds Association contact@meeds.io

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
  <div class="people-list-group-filter mt-4">
    <template v-if="canSetGroupFilter">
      <div class="d-flex align-center text-start">
        <div class="text-subtitle-1 text-color">
          {{ $t('peopleList.settings.groupFilter') }}
        </div>
        <v-spacer />
        <v-switch
          :id="switchId"
          :input-value="enabled"
          :disabled="(otherGroupsApplied && !$root.unresolvableGroups) || (!enabled && !value.length && !canAddGroup)"
          :aria-label="$t('peopleList.settings.groupFilter')"
          :aria-describedby="$root.missingGroups && groupsUnavailableId || null"
          class="people-list-group-filter-switch ma-0 width-fit-content"
          hide-details
          @change="$emit('update:enabled', $event)"
          @keydown.native.enter.exact.prevent="$event.repeat || $emit('update:enabled', !enabled)" />
      </div>
      <template v-if="enabled">
        <label
          v-if="canAddGroup"
          :for="groupSearchId"
          class="d-sr-only">
          {{ $t('peopleList.settings.groupSearch') }}
        </label>
        <v-autocomplete
          v-if="canAddGroup"
          :id="groupSearchId"
          ref="groupSearch"
          v-model="selectedGroup"
          :items="suggestions"
          :loading="loading"
          :search-input.sync="query"
          :placeholder="$t('peopleList.settings.groupSearch')"
          :no-data-text="$t('peopleList.settings.groupSearch.noData')"
          :aria-describedby="groupSearchDescribedBy"
          item-text="label"
          item-value="groupId"
          class="people-list-group-search mt-2 mb-0 mx-0 pa-0 elevation-0 no-border"
          no-filter
          outlined
          dense
          hide-details
          return-object
          @focus="searchGroups"
          @change="addGroup">
          <template #item="{item}">
            <v-list-item-content class="people-directory-group-suggestion">
              <v-list-item-title>{{ item.label }}</v-list-item-title>
            </v-list-item-content>
          </template>
        </v-autocomplete>
        <!-- why Apply is disabled, read with the field; text, not color.
             The missing groups' message below says it already -->
        <div
          v-if="groupsMissing && !$root.missingGroups"
          :id="groupsMissingId"
          class="people-list-groups-missing caption error-color mt-1">
          {{ $t('peopleList.settings.group.required') }}
        </div>
        <div
          v-if="value.length"
          class="d-flex flex-wrap mt-2"
          role="list">
          <v-chip
            v-for="group in value"
            :key="group.groupId"
            color="primary"
            text-color="white"
            class="people-directory-group-chip me-2 mb-2"
            role="listitem"
            dark
            small>
            <v-icon
              size="12"
              class="me-1"
              aria-hidden="true">
              fa-users
            </v-icon>
            {{ group.label }}
            <!-- the platform's Vuetify renders a chip's own close icon
                 without its close-label: this button carries the name -->
            <v-btn
              :aria-label="$t('peopleList.settings.group.remove', {0: group.label})"
              :title="$t('peopleList.settings.group.remove', {0: group.label})"
              class="people-directory-group-remove ms-1 me-n1"
              width="18"
              height="18"
              icon
              dark
              @click="removeGroup(group)">
              <v-icon size="12" aria-hidden="true">fa-times</v-icon>
            </v-btn>
          </v-chip>
        </div>
      </template>
      <!-- the stored filter's state, placed as the note on the groups the
           editor does not manage: shown until an Apply by an editor who may
           set groups -->
      <div
        v-if="$root.missingGroups"
        :id="groupsUnavailableId"
        class="people-list-groups-unavailable d-flex align-start mt-2">
        <v-icon
          size="14"
          class="icon-default-color me-2 mt-1"
          aria-hidden="true">
          fa-exclamation-triangle
        </v-icon>
        <span class="caption text-sub-title">
          {{ $t('peopleList.settings.group.unavailable') }}
        </span>
      </div>
    </template>
    <div
      v-else-if="$root.missingGroups"
      class="people-list-groups-unavailable d-flex align-start mt-2">
      <v-icon
        size="14"
        class="icon-default-color me-2 mt-1"
        aria-hidden="true">
        fa-exclamation-triangle
      </v-icon>
      <span class="caption text-sub-title">
        {{ $t('peopleList.settings.group.unavailableNoRight') }}
      </span>
    </div>
    <div
      v-if="otherGroupsApplied"
      class="people-list-other-groups-applied d-flex align-start mt-2">
      <v-icon
        size="14"
        class="icon-default-color me-2 mt-1"
        aria-hidden="true">
        fa-info-circle
      </v-icon>
      <span class="caption text-sub-title">
        {{ $t('peopleList.settings.group.unmanagedGroupsApplied') }}
      </span>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    // the groups the editor selected: [{groupId, label}]
    value: {
      type: Array,
      default: () => [],
    },
    enabled: {
      type: Boolean,
      default: false,
    },
    canSetGroupFilter: {
      type: Boolean,
      default: false,
    },
    // whether groups the editor cannot select are also applied
    otherGroupsApplied: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    loading: false,
    query: null,
    groups: [],
    selectedGroup: null,
    searchTimeout: null,
    abortController: null,
    // PeopleDirectoryService.MAX_GROUPS: the portlet refuses more
    maxGroups: 20,
    // PeopleDirectoryService.MAX_GROUP_QUERY_LENGTH
    maxQueryLength: 100,
  }),
  computed: {
    switchId() {
      return `${this.$root.appId}GroupFilterSwitch`;
    },
    groupSearchId() {
      return `${this.$root.appId}GroupSearch`;
    },
    groupsUnavailableId() {
      return `${this.$root.appId}GroupsUnavailable`;
    },
    groupsMissingId() {
      return `${this.$root.appId}GroupsMissing`;
    },
    // ON with no group, and none the editor cannot select applied: Apply is
    // disabled (the drawer's groupsMissing)
    groupsMissing() {
      return this.enabled && !this.value.length && !this.otherGroupsApplied;
    },
    // the message the search field is read with: the missing groups' one
    // while the stored filter misses groups, else why Apply is disabled
    groupSearchDescribedBy() {
      if (this.$root.missingGroups) {
        return this.groupsUnavailableId;
      }
      return this.groupsMissing && this.groupsMissingId || null;
    },
    // the groups applied that the editor cannot select count too: with no
    // room left and no group of the editor's, the switch cannot be turned ON
    canAddGroup() {
      return this.value.length + (this.$root.otherGroupsCount || 0) < this.maxGroups;
    },
    // the groups the server offers, but those already selected
    suggestions() {
      const selected = new Set(this.value.map(group => group.groupId));
      return this.groups.filter(group => !selected.has(group.groupId));
    },
  },
  watch: {
    query() {
      // one request on the last keystroke
      window.clearTimeout(this.searchTimeout);
      this.searchTimeout = window.setTimeout(this.searchGroups, 300);
    },
  },
  beforeDestroy() {
    window.clearTimeout(this.searchTimeout);
    this.abortController?.abort?.();
  },
  methods: {
    // the groups the editor can select, as the server lists them
    searchGroups() {
      this.abortController?.abort?.();
      const abortController = new AbortController();
      this.abortController = abortController;
      this.loading = true;
      const params = new URLSearchParams({
        q: (this.query || '').substring(0, this.maxQueryLength),
      });
      // the groups already selected are left out before the server's bound
      this.value.forEach(group => params.append('exclude', group.groupId));
      const url = this.$root.groupsResourceUrl;
      const separator = url.includes('?') ? '&' : '?';
      return fetch(`${url}${separator}${params.toString()}`, {
        method: 'GET',
        credentials: 'include',
        signal: abortController.signal,
      }).then(resp => {
        if (!resp?.ok) {
          throw new Error('Error while reading the groups');
        }
        return resp.json();
      }).then(groups => this.groups = groups || [])
        .catch(e => {
          if (e?.name !== 'AbortError') {
            this.groups = [];
          }
        })
        .finally(() => {
          // an aborted request settles while the next one is in flight
          if (this.abortController === abortController) {
            this.loading = false;
          }
        });
    },
    addGroup(group) {
      if (!group?.groupId) {
        return;
      }
      if (!this.value.some(g => g.groupId === group.groupId)) {
        this.$emit('input', [...this.value, {groupId: group.groupId, label: group.label}]);
      }
      // the field is emptied once the menu closed, and keeps the focus to add
      // another group (RGAA 12.8)
      this.$nextTick(() => {
        this.selectedGroup = null;
        this.query = null;
        this.$refs.groupSearch?.focus?.();
      });
    },
    removeGroup(group) {
      this.$emit('input', this.value.filter(g => g.groupId !== group.groupId));
      // the focus goes back to the search field (RGAA 12.8), or to the switch
      // while the field is hidden
      this.$nextTick(() => {
        if (this.$refs.groupSearch) {
          this.$refs.groupSearch.focus();
        } else {
          document.getElementById(this.switchId)?.focus?.();
        }
      });
    },
  },
};
</script>
