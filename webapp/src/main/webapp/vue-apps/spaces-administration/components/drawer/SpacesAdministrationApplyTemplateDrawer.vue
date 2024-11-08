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
    id="SpaceApplyTemplateDrawer"
    ref="drawer"
    v-model="drawer"
    :loading="loading"
    no-x-scroll
    right>
    <template #title>
      {{ $t('social.spaces.administration.manageSpaces.applyTemplate') }}
    </template>
    <template v-if="drawer && space" #content>
      <div class="pa-4">
        <div class="mb-4">
          {{ $t('social.spaces.administration.manageSpaces.applyTemplateDescription1') }}
        </div>
        <div class="mb-4">
          {{ $t('social.spaces.administration.manageSpaces.applyTemplateDescription2') }}
        </div>
        <div class="mb-1">
          {{ $t('social.spaces.administration.manageSpaces.space') }}
        </div>
        <space-avatar
          :space="space"
          class="mb-4" />
        <div class="text-header mb-1">
          {{ $t('social.spaces.administration.manageSpaces.selectNewTemplate') }}
        </div>
        <select
          v-model="spaceTemplateId"
          class="flex-grow-0 ignore-vuetify-classes py-2 height-auto full-width text-truncate mt-0 mb-4"
          @change="$emit('filter-select-change', select)">
          <option
            v-for="item in spaceTemplateItems"
            :key="item.value"
            :value="item.value">
            {{ item.text }}
          </option>
        </select>
        <template v-if="spaceTemplate && spacePermissions">
          <div class="text-header mb-4">
            {{ $t('social.spaces.administration.manageSpaces.chooseTemplateCharacteristicsToApply') }}
          </div>
          <div class="font-weight-bold mb-2">
            {{ $t('social.spaces.administration.manageSpaces.layoutAndNavigation') }}
          </div>
          <div class="mb-4">
            {{ $t('social.spaces.administration.manageSpaces.selectedLayoutAndNavigationWillBeApplied') }}
          </div>
          <spaces-administration-template-characteristic
            v-model="accessRules"
            title="social.spaces.administration.manageSpaces.accessRules">
            <template #spaceValue>
              <div>
                {{ $t(`social.spaces.administration.manageSpaces.registration.${space.subscription}`) }}
              </div>
              <span class="mx-2">/</span>
              <div>
                {{ $t(`social.spaces.administration.manageSpaces.visibility.${space.visibility}`) }}
              </div>
            </template>
            <template #templateValue>
              <div>
                {{ $t(`social.spaces.administration.manageSpaces.registration.${spaceTemplate.spaceDefaultRegistration.toLowerCase()}`) }}
              </div>
              <span class="mx-2">/</span>
              <div>
                {{ $t(`social.spaces.administration.manageSpaces.visibility.${spaceTemplate.spaceDefaultVisibility.toLowerCase()}`) }}
              </div>
            </template>
          </spaces-administration-template-characteristic>
          <spaces-administration-template-characteristic
            v-model="editorialMode"
            title="social.spaces.administration.manageSpaces.editorialMode">
            <template #spaceValue>
              <div>
                {{ $t(`social.spaces.administration.manageSpaces.${space.redactorsCount && 'on' || 'off'}`) }}
              </div>
            </template>
            <template #templateValue>
              <div>
                {{ $t(`social.spaces.administration.manageSpaces.${spaceTemplate.spaceAllowContentCreation && 'on' || 'off'}`) }}
              </div>
            </template>
          </spaces-administration-template-characteristic>
          <spaces-administration-template-characteristic
            v-model="layoutPermissions"
            title="social.spaces.administration.manageSpaces.navigationPermission">
            <template #spaceValue>
              <spaces-administration-permissions-label
                :value="spacePermissions.layoutPermissions"
                :space-admin-membership-type="`manager:${space.groupId}`"
                class="text-end" />
            </template>
            <template #templateValue>
              <spaces-administration-permissions-label
                :value="spaceTemplate.spaceLayoutPermissions"
                class="text-end" />
            </template>
          </spaces-administration-template-characteristic>
          <spaces-administration-template-characteristic
            v-model="publicSitePermissions"
            title="social.spaces.administration.manageSpaces.spacePublicSitePermission">
            <template #spaceValue>
              <spaces-administration-permissions-label
                :value="spacePermissions.publicSitePermissions"
                :space-admin-membership-type="`manager:${space.groupId}`"
                class="text-end" />
            </template>
            <template #templateValue>
              <spaces-administration-permissions-label
                :value="spaceTemplate.spacePublicSitePermissions"
                class="text-end" />
            </template>
          </spaces-administration-template-characteristic>
          <spaces-administration-template-characteristic
            v-model="deletePermissions"
            title="social.spaces.administration.manageSpaces.deletionPermission">
            <template #spaceValue>
              <spaces-administration-permissions-label
                :value="spacePermissions.deletePermissions"
                :space-admin-membership-type="`manager:${space.groupId}`"
                class="text-end" />
            </template>
            <template #templateValue>
              <spaces-administration-permissions-label
                :value="spaceTemplate.spaceDeletePermissions"
                class="text-end" />
            </template>
          </spaces-administration-template-characteristic>
        </template>
      </div>
    </template>
    <template #footer>
      <div class="d-flex">
        <v-spacer />
        <v-btn
          class="btn me-2"
          @click="close">
          {{ $t('social.spaces.administration.manageSpaces.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="!modified"
          class="btn-primary"
          elevation="0"
          @click="apply">
          {{ $t('social.spaces.administration.manageSpaces.apply') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    saving: false,
    space: null,
    spaceTemplateId: null,
    spacePermissions: null,
    accessRules: false,
    editorialMode: false,
    layoutPermissions: false,
    publicSitePermissions: false,
    deletePermissions: false,
  }),
  computed: {
    modified() {
      return this.spaceTemplateId
        && Number(this.spaceTemplateId)
        && (Number(this.spaceTemplateId) !== this.space.templateId
            || this.accessRules
            || this.editorialMode
            || this.layoutPermissions
            || this.publicSitePermissions
            || this.deletePermissions
        );
    },
    spaceTemplate() {
      return this.$root.spaceTemplates.find(t => t.id === Number(this.spaceTemplateId));
    },
    spaceTemplateItems() {
      const spaceTemplateItems = [{
        text: '',
        value: '0',
      }];
      if (this.$root.spaceTemplates?.length) {
        spaceTemplateItems.push(...this.$root.spaceTemplates.map(t => ({
          text: t.name,
          value: t.id,
        })));
      }
      return spaceTemplateItems;
    },
  },
  created() {
    this.$root.$on('space-administration-apply-template-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-apply-template-drawer-open', this.open);
  },
  methods: {
    async open(space) {
      this.space = space;
      this.spaceTemplateId = space.templateId && `${space.templateId}` || '0';
      this.spacePermissions = null;
      this.accessRules = false;
      this.editorialMode = false;
      this.layoutPermissions = false;
      this.publicSitePermissions = false;
      this.deletePermissions = false;
      this.$refs.drawer.open();
      this.spacePermissions = await this.$spaceAdministrationService.getSpacePermission(this.space.id);
    },
    async apply() {
      this.saving = true;
      try {
        await this.$spaceAdministrationService.applySpaceTemplate(this.space.id, {
          templateId: this.spaceTemplateId,
          accessRules: this.accessRules,
          editorialMode: this.editorialMode,
          layoutPermissions: this.layoutPermissions,
          publicSitePermissions: this.publicSitePermissions,
          deletePermissions: this.deletePermissions,
        });
        this.$root.$emit('spaces-administration-list-refresh');
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spaceTemplateCharacteristicsUpdateSuccess'), 'success');
        this.close();
      } catch (e) {
        this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spaceTemplateCharacteristicsUpdateError', {0: this.space.displayName}), 'error');
      } finally {
        this.saving = false;
      }
    },
    close() {
      this.$refs.drawer.close();
    },
  },
};
</script>