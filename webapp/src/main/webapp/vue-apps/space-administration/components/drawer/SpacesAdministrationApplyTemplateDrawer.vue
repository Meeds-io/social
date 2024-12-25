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
    :loading="saving || loading"
    no-x-scroll
    right>
    <template #title>
      {{ $t('social.spaces.administration.manageSpaces.applyTemplate') }}
    </template>
    <template v-if="drawer && initialized && (space || spaces)" #content>
      <div class="pa-4">
        <div class="mb-4">
          {{ $t('social.spaces.administration.manageSpaces.applyTemplateDescription1') }}
        </div>
        <div class="mb-4">
          {{ $t('social.spaces.administration.manageSpaces.applyTemplateDescription2') }}
        </div>
        <div class="text-header mb-2">
          {{ $t('social.spaces.administration.manageSpaces.space') }}
        </div>
        <space-avatar
          v-if="space"
          :space="space"
          class="mb-4" />
        <v-chip
          v-else-if="spaces"
          class="mb-4 light-grey-color"
          height="40">
          <span>
            {{ $t('social.spaces.administration.manageSpaces.selectedSpacesCount', {
              0: selectionCount,
            }) }}
          </span>
        </v-chip>
        <div class="text-header mb-1">
          {{ $t('social.spaces.administration.manageSpaces.selectNewTemplate') }}
        </div>
        <select
          v-model="spaceTemplateId"
          :aria-label="$t('social.spaces.administration.manageSpaces.templateSelection')"
          class="flex-grow-0 ignore-vuetify-classes py-2 height-auto full-width text-truncate mt-0 mb-4">
          <option
            v-for="item in spaceTemplateItems"
            :key="item.value"
            :value="item.value">
            {{ item.text }}{{ item.deleted && ` ${$t('social.spaces.administration.manageSpaces.deletedSpaceTemplate')}` || (!item.enabled && ` ${$t('social.spaces.administration.manageSpaces.disabledSpaceTemplate')}`) || '' }}
          </option>
        </select>
        <template v-if="spaceTemplate">
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
            <template v-if="spacePermissions" #spaceValue>
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
            <template v-if="space" #spaceValue>
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
            <template v-if="spacePermissions && space" #spaceValue>
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
            <template v-if="spacePermissions" #spaceValue>
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
            v-model="updateCategories"
            title="social.spaces.administration.manageSpaces.updateCategories">
            <template v-if="!selectionCount" #spaceValue>
              {{ oldSpaceTemplateCategoryNames }}
            </template>
            <template #templateValue>
              {{ newSpaceTemplateCategoryNames }}
            </template>
          </spaces-administration-template-characteristic>
          <spaces-administration-template-characteristic
            v-if="updateCategories"
            v-model="removeCategories"
            title="social.spaces.administration.manageSpaces.removeCategories" />
          <spaces-administration-template-characteristic
            v-model="deletePermissions"
            title="social.spaces.administration.manageSpaces.deletionPermission">
            <template v-if="spacePermissions" #spaceValue>
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
          :disabled="saving"
          class="btn me-2"
          @click="close">
          {{ $t('social.spaces.administration.manageSpaces.cancel') }}
        </v-btn>
        <v-btn
          :loading="saving"
          :disabled="!modified || disabledTemplate || loading"
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
    spaceCategories: null,
    spacePermissions: null,
    accessRules: false,
    editorialMode: false,
    layoutPermissions: false,
    publicSitePermissions: false,
    deletePermissions: false,
    updateCategories: false,
    removeCategories: false,
    spaceTemplateCategories: null,
    spaces: null,
    selectionCount: null,
    callback: null,
    initialized: false,
  }),
  computed: {
    modified() {
      return this.spaceTemplateId
        && Number(this.spaceTemplateId)
        && (this.spaces?.length
        || (Number(this.spaceTemplateId) !== this.space.templateId
            || this.accessRules
            || this.editorialMode
            || this.layoutPermissions
            || this.publicSitePermissions
            || this.deletePermissions
        ));
    },
    spaceTemplate() {
      return this.$root.spaceTemplates?.find?.(t => t.id === Number(this.spaceTemplateId));
    },
    disabledTemplate() {
      return this.spaceTemplate?.deleted || !this.spaceTemplate?.enabled;
    },
    loading() {
      return this.drawer && !this.initialized;
    },
    spaceTemplateItems() {
      const spaceTemplateItems = [{
        text: '',
        value: '0',
        enabled: true,
        deleted: false,
      }];
      if (this.$root.spaceTemplates?.length) {
        spaceTemplateItems.push(...this.$root.spaceTemplates.map(t => ({
          text: t.name,
          value: t.id,
          enabled: t.enabled,
          deleted: t.deleted,
        })));
      }
      return spaceTemplateItems;
    },
    spaceCategoryIds() {
      return this.space?.categoryIds || [];
    },
    spaceTemplateCategoryIds() {
      return this.spaceTemplate?.spaceDefaultCategoryIds || [];
    },
    newSpaceTemplateCategories() {
      const spaceTemplateCategories = this.spaceTemplateCategories?.slice?.() || [];
      if (!this.removeCategories
          && this.spaceCategories?.length
          && this.selectionCount === 0) {
        spaceTemplateCategories.push(...this.spaceCategories.filter(c => !this.spaceTemplateCategories.find(ct => ct.id === c.id)));
      }
      return spaceTemplateCategories;
    },
    newSpaceTemplateCategoryNames() {
      const newSpaceTemplateCategories = this.newSpaceTemplateCategories?.slice?.() || [];
      newSpaceTemplateCategories?.sort((a, b) => this.$root.collator.compare(a.name.toLowerCase(), b.name.toLowerCase()));
      return newSpaceTemplateCategories.map(c => c.name).join(', ');
    },
    oldSpaceTemplateCategoryNames() {
      const spaceCategories = this.spaceCategories?.slice?.() || [];
      spaceCategories?.sort((a, b) => this.$root.collator.compare(a.name.toLowerCase(), b.name.toLowerCase()));
      return spaceCategories?.map?.(c => c.name)?.join?.(', ') || '';
    },
  },
  watch: {
    async spaceCategoryIds() {
      if (this.spaceCategoryIds?.length) {
        const spaceCategories = await Promise.all(this.spaceCategoryIds.map(id => this.$categoryService.getCategory(id)));
        this.spaceCategories = spaceCategories.filter(c => c);
      } else {
        this.spaceCategories = [];
      }
      this.$forceUpdate();
    },
    async spaceTemplateCategoryIds() {
      if (this.spaceTemplateCategoryIds?.length) {
        const spaceTemplateCategories = await Promise.all(this.spaceTemplateCategoryIds.map(id => this.$categoryService.getCategory(id)));
        this.spaceTemplateCategories = spaceTemplateCategories.filter(c => c);
      } else {
        this.spaceTemplateCategories = [];
      }
      this.$forceUpdate();
    },
  },
  created() {
    this.$root.$on('space-administration-apply-template-drawer-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-administration-apply-template-drawer-open', this.open);
  },
  methods: {
    async open(obj, selectionCount, callback) {
      this.initialized = false;
      try {
        this.$refs.drawer.open();
        this.accessRules = false;
        this.editorialMode = false;
        this.layoutPermissions = false;
        this.publicSitePermissions = false;
        this.deletePermissions = false;
        this.updateCategories = false;
        this.removeCategories = true;
        if (obj?.id) {
          this.space = obj;
          this.spaces = null;
          this.selectionCount = 0;
          this.callback = null;
          this.spaceTemplateId = this.space.templateId && `${this.space.templateId}` || '0';
          this.spacePermissions = await this.$spaceAdministrationService.getSpacePermission(this.space.id);
        } else {
          this.space = null;
          this.spaces = obj;
          this.selectionCount = selectionCount;
          this.callback = callback;
          this.spaceTemplateId = null;
          this.spacePermissions = null;
        }
      } finally {
        await this.$nextTick();
        this.initialized = true;
      }
    },
    async apply() {
      this.saving = true;
      try {
        if (this.callback) {
          this.callback({
            templateId: this.spaceTemplateId,
            accessRules: this.accessRules,
            editorialMode: this.editorialMode,
            layoutPermissions: this.layoutPermissions,
            publicSitePermissions: this.publicSitePermissions,
            deletePermissions: this.deletePermissions,
            updateCategories: this.updateCategories,
            removeExistingCategories: this.removeCategories,
          });
        } else {
          await this.$spaceAdministrationService.applySpaceTemplate(this.space.id, {
            templateId: this.spaceTemplateId,
            accessRules: this.accessRules,
            editorialMode: this.editorialMode,
            layoutPermissions: this.layoutPermissions,
            publicSitePermissions: this.publicSitePermissions,
            deletePermissions: this.deletePermissions,
            updateCategories: this.updateCategories,
            removeExistingCategories: this.removeCategories,
          });
          this.$root.$emit('spaces-administration-list-refresh', this.$root.isFilteredByTemplate);
          this.$root.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.spaceTemplateCharacteristicsUpdateSuccess'), 'success');
        }
        this.close();
      } catch (e) {
        // eslint-disable-next-line no-console
        console.error(e);
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