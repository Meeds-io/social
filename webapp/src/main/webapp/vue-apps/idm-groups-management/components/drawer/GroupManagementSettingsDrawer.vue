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
  <exo-drawer
    id="groupManagementSettingsDrawer"
    ref="drawer"
    v-model="drawer"
    right>
    <template #title>
      {{ $t('GroupsManagement.groupSettings') }}
    </template>
    <template v-if="drawer" #content>
      <div class="application-body d-flex flex-column pa-5">
        <span class="text-header py-0">{{ $t('GroupsManagement.groupInfo') }}</span>
        <div class="d-flex align-center justify-space-between flex-grow-1 text-no-wrap">
          <span class="text-body text-capitalize-first-letter pt-4">{{ groupLabel }}</span>
          <v-btn
            icon
            :title="$t('GroupsManagement.editInfo.title')"
            @click="$root.$emit('edit-group', group)">
            <v-icon size="20">fa fa-edit</v-icon>
          </v-btn>
        </div>
        <span class="text-header pt-5">{{ $t('GroupsManagement.groupCharacteristics') }}</span>
        <div class="d-flex align-center justify-space-between flex-grow-1 text-no-wrap pt-4">
          <help-label
            label="GroupsManagement.organizationalUnit.label"
            label-class="text-body"
            tooltip="GroupsManagement.organizationalUnit.tooltip"
            drawer-title="GroupsManagement.organizationalUnit.help.title">
            <template #helpContent>
              <div class="text-title pb-2">
                {{ $t('GroupsManagement.organizationalUnit.help.heading') }}
              </div>
              <p>{{ $t('GroupsManagement.organizationalUnit.help.paragraph1') }}</p>
              <p class="d-flex align-start">
                <v-icon
                  size="16"
                  color="success"
                  class="me-1 mt-1">
                  mdi-checkbox-marked
                </v-icon>
                <span class="flex-grow-1">{{ $t('GroupsManagement.organizationalUnit.help.bullet1') }}</span>
              </p>
              <p class="d-flex align-start">
                <v-icon
                  size="16"
                  color="success"
                  class="me-1 mt-1">
                  mdi-checkbox-marked
                </v-icon>
                <span class="flex-grow-1">{{ $t('GroupsManagement.organizationalUnit.help.bullet2') }}</span>
              </p>
              <p class="mb-0">{{ $t('GroupsManagement.organizationalUnit.help.paragraph2') }}</p>
              <p class="mb-0">- {{ $t('GroupsManagement.organizationalUnit.help.item1') }}</p>
              <p class="mb-0">- {{ $t('GroupsManagement.organizationalUnit.help.item2') }}</p>
              <p>- {{ $t('GroupsManagement.organizationalUnit.help.item3') }}</p>
              <p>{{ $t('GroupsManagement.organizationalUnit.help.paragraph3') }}</p>
            </template>
          </help-label>
          <v-switch
            id="organizationalUnitSwitch"
            v-model="organizationalUnit"
            hide-details
            class="mt-0 pt-0"
            :aria-label="$t('GroupsManagement.organizationalUnit.ariaLabel')"
            :aria-checked="organizationalUnit ? 'true' : 'false'" />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          :disabled="saving"
          class="btn ms-auto me-2"
          @click="cancel">
          {{ $t('GroupsManagement.button.cancel') }}
        </v-btn>
        <v-btn
          :disabled="saving"
          :loading="saving"
          class="btn primary"
          @click="apply">
          {{ $t('activity.filter.button.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>
export default {
  data: () => ({
    drawer: false,
    group: null,
    organizationalUnit: false,
    saving: false,
  }),
  computed: {
    groupLabel() {
      return this.group?.label || '';
    },
  },
  created() {
    this.$root.$on('open-group-settings-drawer', this.open);
  },
  beforeDestroy() {
    this.$root.$off('open-group-settings-drawer', this.open);
  },
  methods: {
    open(group) {
      this.group = group;
      this.organizationalUnit = !!group?.organizationalUnit;
      this.drawer = true;
      this.$groupService.isOrganizationalUnit(group.id).then(organizationalUnit => {
        this.organizationalUnit = organizationalUnit;
        this.$set(this.group, 'organizationalUnit', organizationalUnit);
      });
    },
    cancel() {
      this.drawer = false;
    },
    apply() {
      this.saving = true;
      this.$groupService.updateOrganizationalUnit(this.group.id, this.organizationalUnit)
        .then(() => {
          this.$set(this.group, 'organizationalUnit', this.organizationalUnit);
          this.cancel();
        })
        .catch(() => this.$root.$emit('alert-message', this.$t('IDMManagement.error.UnknownServerError'), 'error'))
        .finally(() => this.saving = false);
    },
  },
};
</script>
