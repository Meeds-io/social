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
    ref="drawer"
    v-model="drawer"
    :confirm-close="modified"
    :confirm-close-labels="closeConfirmLabels"
    class="spaceTemplateNameFormDrawer"
    right
    allow-expand
    @expand-updated="expanded = $event">
    <template #title>
      {{ isNew && $t('spaceTemplate.add.drawer.newTemplate') || $t('spaceTemplate.add.drawer.editTemplate') }}
    </template>
    <template v-if="drawer && spaceTemplate" #content>
      <div class="pa-4 full-wdith overflow-hidden">
        <v-alert
          v-if="step === 1 && !isNew"
          type="info"
          outlined>
          <div class="text-color">
            <div class="mb-2">
              {{ $t('spaceTemplate.editWarningInfo1') }}
            </div>
            <span v-sanitized-html="editWarningInfo2"></span>
          </div>
        </v-alert>
        <div class="d-flex align-center">
          <v-icon size="24" class="me-3">{{ spaceTemplate.icon }}</v-icon>
          <span class="text-truncate text-start font-weight-bold flex-grow-1">{{ name }}</span>
          <v-btn
            :title="$t('spaceTemplate.editName')"
            class="me-n2"
            icon
            @click="openNameDrawer">
            <v-icon size="20">fa-edit</v-icon>
          </v-btn>
        </div>
        <v-card
          class="d-flex my-4"
          flat
          @click="step = 1">
          <v-card
            height="24"
            width="24"
            class="d-flex align-center justify-center tertiary border-radius-circle"
            flat>
            1
          </v-card>
          <div class="text-header mx-3">
            {{ $t('spaceTemplate.creationStep') }}
          </div>
        </v-card>
        <div v-if="step === 1" class="d-flex flex-column mb-4">
          <span class="mb-2">{{ $t('spaceTemplate.mandatoryCreationStepDescription') }}</span>
          <v-checkbox
            id="spaceFieldName"
            v-model="spaceFieldName"
            :label="$t('spaceTemplate.mandatoryCreationStepName')"
            name="spaceFieldName"
            on-icon="fa-check-square"
            class="mt-0 mb-2 ms-n1 pa-0"
            dense />
          <v-checkbox
            id="spaceFieldInvitation"
            v-model="spaceFieldInvitation"
            :label="$t('spaceTemplate.mandatoryCreationStepInvitation')"
            name="spaceFieldInvitation"
            on-icon="fa-check-square"
            class="mt-0 mb-2 ms-n1 pa-0"
            dense />
          <v-checkbox
            id="spaceFieldProperties"
            v-model="spaceFieldProperties"
            :label="$t('spaceTemplate.mandatoryCreationStepProperties')"
            name="spaceFieldProperties"
            on-icon="fa-check-square"
            class="mt-0 mb-2 ms-n1 pa-0"
            dense />
          <v-checkbox
            id="spaceFieldAccessControl"
            v-model="spaceFieldAccessControl"
            :label="$t('spaceTemplate.mandatoryCreationStepAccessControl')"
            name="spaceFieldAccessControl"
            on-icon="fa-check-square"
            class="mt-0 mb-2 ms-n1 pa-0"
            dense />
          <div v-if="!spaceFieldName && !spaceFieldInvitation" class="error--text mb-2">
            {{ $t('spaceTemplate.mandatoryCreationStepNameOrInvitationMandatory') }}
          </div>
        </div>
        <v-card
          v-on="step2Enabled && {
            click: () => step = 2,
          }"
          class="d-flex mb-4"
          flat>
          <v-card
            :class="step > 1 ? 'tertiary' : 'mask-color'"
            height="24"
            width="24"
            class="d-flex align-center justify-center border-radius-circle white--text"
            flat>
            2
          </v-card>
          <div class="text-header mx-3">
            {{ $t('spaceTemplate.spaceTemplateManagement') }}
          </div>
        </v-card>
        <div v-if="step === 2" class="mb-4">
          <space-templates-management-permissions
            v-model="spaceTemplate.permissions"
            label="spaceTemplate.permissionsStepCreateSpacePermissionLabel"
            class="mb-4"
            users
            admins
            any />
          <space-templates-management-permissions
            v-model="spaceTemplate.adminPermissions"
            help-label="spaceTemplate.permissionsStepSpaceAdminsPermissionLabel"
            help-tooltip="spaceTemplate.permissionsStepSpaceAdminsPermissionTooltip"
            class="font-weight-bold"
            users
            admins>
            <template #helpContent>
              <div>
                <div class="mb-2">
                  {{ $t('spaceTemplate.permissionsStepSpaceAdminsPermissionHelpContent1') }}
                </div>
                <div class="mb-2">
                  {{ $t('spaceTemplate.permissionsStepSpaceAdminsPermissionHelpContent2') }}
                  <div>
                    {{ $t('spaceTemplate.permissionsStepSpaceAdminsPermissionHelpContent3') }}
                  </div>
                  <div>
                    {{ $t('spaceTemplate.permissionsStepSpaceAdminsPermissionHelpContent4') }}
                  </div>
                  <div>
                    {{ $t('spaceTemplate.permissionsStepSpaceAdminsPermissionHelpContent5') }}
                  </div>
                </div>
                <div class="mb-2">
                  {{ $t('spaceTemplate.permissionsStepSpaceAdminsPermissionHelpContent6') }}
                </div>
              </div>
            </template>
          </space-templates-management-permissions>
          <space-template-enclosing-membership v-model="spaceTemplate.enclosingMemberships" />
        </div>
        <v-card
          v-on="step3Enabled && {
            click: () => step = 3,
          }"
          class="d-flex mb-4"
          flat>
          <v-card
            :class="step > 2 ? 'tertiary' : 'mask-color'"
            height="24"
            width="24"
            class="d-flex align-center justify-center border-radius-circle white--text"
            flat>
            3
          </v-card>
          <div class="text-header mx-3">
            {{ $t('spaceTemplate.defaultSpaceConfigurationStep') }}
          </div>
        </v-card>
        <div v-show="step === 3" class="mb-4">
          <div class="d-flex flex-column">
            <span class="mb-4">
              {{ $t('spaceTemplate.defaultSpaceConfigurationStepDescription') }}
            </span>
            <space-templates-management-banner
              ref="bannerInput"
              :banner-upload-id="bannerUploadId"
              :banner-data="bannerData"
              :space-template="spaceTemplate"
              class="mb-4"
              @data="bannerData = $event"
              @input="bannerUploadId = $event" />
            <category-input
              v-model="spaceTemplate.spaceDefaultCategoryIds"
              label="spaceTemplate.defaultCategories"
              label-class="font-weight-bold" />
            <space-templates-management-permissions-editorial
              v-model="spaceTemplate.spaceAllowContentCreation"
              class="mb-4" />
            <space-templates-management-access
              v-model="spaceTemplate.spaceDefaultRegistration" />
            <space-templates-management-visibility
              v-model="spaceTemplate.spaceDefaultVisibility"
              class="mb-n2" />
          </div>
        </div>
        <v-card
          v-on="step4Enabled && {
            click: () => step = 4,
          }"
          class="d-flex mb-4"
          flat>
          <v-card
            :class="step > 3 ? 'tertiary' : 'mask-color'"
            height="24"
            width="24"
            class="d-flex align-center justify-center border-radius-circle white--text"
            flat>
            4
          </v-card>
          <div class="text-header mx-3">
            {{ $t('spaceTemplate.spacePermissionsStep') }}
          </div>
        </v-card>
        <div v-if="step === 4" class="d-flex flex-column mb-4">
          <span v-sanitized-html="permissionsStepDescription1" class="mb-2"></span>
          <span class="mb-4">
            {{ $t('spaceTemplate.permissionsStepDescription2') }}
          </span>
          <space-templates-management-permissions
            v-model="spaceTemplate.spaceLayoutPermissions"
            label="spaceTemplate.permissionsStepEditSpaceLayoutPermissionLabel"
            class="mb-4"
            admins
            space-admin />
          <space-templates-management-permissions
            v-model="spaceTemplate.spacePublicSitePermissions"
            label="spaceTemplate.permissionsStepPublicSitePermissionLabel"
            class="mb-4"
            admins
            space-admin />
          <space-templates-management-permissions
            v-model="spaceTemplate.spaceDeletePermissions"
            label="spaceTemplate.permissionsStepDeleteSpacePermissionLabel"
            class="mb-4"
            admins
            space-admin />
        </div>
        <v-card
          v-on="step5Enabled && {
            click: () => step = 5,
          }"
          class="d-flex mb-4"
          flat>
          <v-card
            :class="step > 4 ? 'tertiary' : 'mask-color'"
            height="24"
            width="24"
            class="d-flex align-center justify-center border-radius-circle white--text"
            flat>
            5
          </v-card>
          <div class="text-header mx-3">
            {{ $t('spaceTemplate.subspacesConfigurationStep') }}
          </div>
        </v-card>
        <div v-if="step === 5" class="d-flex flex-column mb-4">
          <div class="d-flex flex-column">
            <div class="d-flex py-2">
              <label for="subspaceConfigurationSwitch" class="flex-grow-1">
                {{ $t('spaceTemplate.subspacesConfigurationStepCanHaveSubspaces') }}
              </label>
              <div class="position-relative mx-8">
                <v-switch
                  id="subspaceConfigurationSwitch"
                  v-model="canHaveSubspaces"
                  :aria-label="canHaveSubspaces && $t('spaceTemplate.subspacesConfigurationStepCanHaveSubspaces') || $t('spaceTemplate.subspacesConfigurationStepCannotHaveSubspaces')"
                  :aria-checked="canHaveSubspaces ? 'true' : 'false'"
                  :disabled="isSubspaceTemplate"
                  class="mb-0 mt-1 me-2 pa-0 r-0 absolute-vertical-center" />
              </div>
            </div>
            <span v-if="isSubspaceTemplate" class="text-subtitle pe-2">{{ subspaceDisableMessage }}</span>
          </div>
          <template v-if="canHaveSubspaces">
            <div class="d-flex flex-column">
              <div class="d-flex">
                <div class="flex-grow-1 align-self-center">
                  {{ $t('spaceTemplate.subspacesConfigurationStepSetMaximumLimit') }}
                </div>
                <div class="position-relative">
                  <v-card
                    v-if="subspacesMaxLimit === 0"
                    class="d-flex flex-row align-center justify-center"
                    flat>
                    <v-btn
                      :title="$t('spaceTemplate.subspacesConfigurationMinLimit')"
                      icon>
                      <v-icon size="16" class="icon-default-color">fa-minus</v-icon>
                    </v-btn>
                    <v-card-text class="pa-0">{{ $t('spaceTemplate.subspacesConfigurationStepNoLimit') }}</v-card-text>
                    <v-btn
                      :title="$t('spaceTemplate.subspacesConfigurationMaxLimit')"
                      icon
                      @click="subspacesMaxLimit++">
                      <v-icon size="16" class="icon-default-color">fa-plus</v-icon>
                    </v-btn>
                  </v-card>
                  <number-input
                    v-else
                    v-model="subspacesMaxLimit"
                    :max="maxSubspacesMaxLimit"
                    :min="minSubspacesMaxLimit"
                    :plus-title="$t('spaceTemplate.subspacesConfigurationMaxLimit')"
                    :minus-title="$t('spaceTemplate.subspacesConfigurationMinLimit')"
                    :step="1"
                    :label="$t('spaceTemplate.subspacesConfigurationStepMaxLimit')"
                    class="ms-auto"
                    editable />
                </div>
              </div>
            </div>
            <div class="flex-grow-1 pt-2">
              <div class="flex-grow-1 align-self-center">
                {{ $t('spaceTemplate.templateSuggester.label') }}
              </div>
              <space-templates-management-suggester
                v-model="subspaceTemplate"
                :labels="suggesterLabels"
                @input="selectSpaceTemplate($event)"
                multiple />
              <v-list
                class="pa-0"
                dense
                v-if="selectedSubspaceTemplates.length">
                <v-list-item
                  class="pa-0"
                  dense>
                  <v-list-item-content class="me-2 pa-0 text-truncate">
                    <v-list-item-title class="text-truncate">
                      {{ $t('spaceTemplate.subspacesConfigurationStepTemplate') }}
                    </v-list-item-title>
                  </v-list-item-content>
                  <v-list-item-action class="mx-0 my-auto">
                    {{ $t('spaceTemplate.subspacesConfigurationStepMaxLimit') }}
                  </v-list-item-action>
                </v-list-item>
                <v-divider />
              </v-list>
              <space-templates-management-subspace-template-item
                v-for="t in selectedSubspaceTemplates"
                :key="t.id"
                :space-template="t"
                :global-limit="subspacesMaxLimit"
                class="px-0"
                @remove-item="removeSelectedSpaceTemplate" />
            </div>
          </template>
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          v-if="step > 1"
          :title="$t('links.label.previous')"
          :disabled="saving"
          class="btn me-2 hidden-xs-only"
          @click="step--">
          {{ $t('spaceTemplate.previous') }}
        </v-btn>
        <v-btn
          :title="$t('links.label.cancel')"
          class="btn ms-auto me-2"
          @click="close()">
          {{ $t('spaceTemplate.cancel') }}
        </v-btn>
        <v-btn
          v-if="step < 5"
          :disabled="disabledNextStep"
          :loading="saving"
          class="btn primary"
          @click="step++">
          {{ $t('spaceTemplate.next') }}
        </v-btn>
        <v-btn
          v-else-if="step > 3"
          :disabled="disabled"
          :loading="saving"
          class="btn primary"
          @click="save">
          {{ isNew && $t('spaceTemplate.create') || $t('spaceTemplate.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    expanded: false,
    saving: false,
    isNew: false,
    basicInformationModified: false,
    spaceTemplate: null,
    originalSpaceTemplate: null,
    bannerUploadId: null,
    bannerData: null,
    name: null,
    description: null,
    nameTranslations: {},
    descriptionTranslations: {},
    templateId: null,
    step: 1,
    spaceFieldName: false,
    spaceFieldInvitation: false,
    spaceFieldProperties: false,
    spaceFieldAccessControl: false,
    spacesManagementUrl: '/portal/administration/home/organisation/spaces',
    canHaveSubspaces: false,
    subspacesMaxLimit: 0,
    subspaceTemplate: null,
    selectedSubspaceTemplates: [],
    maxSubspacesMaxLimit: 100,
    minSubspacesMaxLimit: 0,
    invalidSubspacesMaxLimit: false,
  }),
  computed: {
    rules() {
      return {
        name: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length < this.maxNameLength || this.$t('spaceTemplate.nameExceedsMaxLength', {
            0: this.maxNameLength,
          }),
        ],
        description: [
          v => !v?.length || v.length < this.maxDescriptionLength || this.$t('spaceTemplate.descriptionExceedsMaxLength', {
            0: this.maxDescriptionLength,
          }),
        ],
      };
    },
    disabledFirstStep() {
      return this.step === 1
        && !this.spaceTemplate?.spaceFields?.includes?.('name')
        && !this.spaceTemplate?.spaceFields?.includes?.('invitation');
    },
    step2Enabled() {
      return !this.disabledFirstStep;
    },
    step3Enabled() {
      return this.step2Enabled;
    },
    step4Enabled() {
      return this.step3Enabled;
    },
    step5Enabled() {
      return this.step4Enabled;
    },
    disabledNextStep() {
      if (this.step === 1) {
        return this.disabledFirstStep;
      } else if (this.step === 2) {
        return !this.step2Enabled;
      } else if (this.step === 3) {
        return !this.step3Enabled;
      } else if (this.step === 4) {
        return !this.step4Enabled;
      } else {
        return false;
      }
    },
    modified() {
      return this.isNew
        || this.basicInformationModified
        || (JSON.stringify(this.spaceTemplate) !== JSON.stringify(this.originalSpaceTemplate));
    },
    closeConfirmLabels() {
      return {
        title: this.$t('spaceTemplate.closeConfirmLabels.title'),
        message: this.$t('spaceTemplate.closeConfirmLabels.message'),
        ok: this.$t('spaceTemplate.closeConfirmLabels.ok'),
        cancel: this.$t('spaceTemplate.closeConfirmLabels.cancel'),
      };
    },
    disabled() {
      return !this.name?.length
          || this.name.length > this.maxNameLength
          || (this.description?.length && this.description.length > this.maxDescriptionLength)
          || (!this.spaceTemplate?.spaceFields?.includes?.('name') && !this.spaceTemplate?.spaceFields?.includes?.('invitation'))
          || (this.canHaveSubspaces && !this.selectedSubspaceTemplates.length)
          || (this.selectedSubspaceTemplates.length > 0 && this.subspacesMaxLimit > 0 && this.selectedSubspaceTemplates.some(t => t.subspacesMaxLimit > this.subspacesMaxLimit));
    },
    permissionsStepDescription1() {
      return this.$t('spaceTemplate.permissionsStepDescription1', {
        0: `<a href="${this.spacesManagementUrl}">`,
        1: '</a>',
      });
    },
    editWarningInfo2() {
      return this.$t('spaceTemplate.editWarningInfo2', {
        0: `<a href="${this.spacesManagementUrl}">`,
        1: '</a>',
      });
    },
    suggesterLabels() {
      return {
        searchPlaceholder: this.$t('spaceTemplate.templateSuggester.searchPlaceholder'),
        placeholder: this.$t('spaceTemplate.templateSuggester.placeholder'),
        noDataLabel: this.$t('spaceTemplate.templateSuggester.noDataLabel'),
      };
    },
    isSubspaceTemplate() {
      return this.$root?.subspacesTemplateIds?.includes(this.spaceTemplate?.id) || false;
    },
    parentSpaceTemplateName() {
      const parent = this.$root.spaceTemplates.find(template =>
        Array.isArray(template.allowedSubspaceTemplates) &&
          template.allowedSubspaceTemplates.some(subspaceId =>
            Number(subspaceId.split(':')[0]) === this.spaceTemplate.id
          )
      );
      return parent?.name || '';
    },
    subspaceDisableMessage() {
      if (!this.isSubspaceTemplate) {
        return '';
      }
      return this.$t('spaceTemplate.subspaces.disable.message', {
        0: this.parentSpaceTemplateName,
      });
    }
  },
  watch: {
    description() {
      if (this.$refs.descriptionTranslation) {
        this.$refs.descriptionTranslation.setValue(this.description);
      }
      if (this.$refs.spaceTemplateDescriptionEditor?.editor && this.description !== this.$refs.spaceTemplateDescriptionEditor.inputVal) {
        this.$refs.spaceTemplateDescriptionEditor.editor.setData(this.description);
      }
    },
    spaceFieldName(val) {
      if (!this.drawer) {
        return;
      } else if (val) {
        if (!this.spaceTemplate?.spaceFields?.includes?.('name')) {
          this.spaceTemplate.spaceFields.push('name');
        }
      } else if (this.spaceTemplate?.spaceFields?.includes?.('name')) {
        this.spaceTemplate.spaceFields.splice(this.spaceTemplate.spaceFields.indexOf('name'), 1);
      }
    },
    spaceFieldInvitation(val) {
      if (!this.drawer) {
        return;
      } else if (val) {
        if (!this.spaceTemplate?.spaceFields?.includes?.('invitation')) {
          this.spaceTemplate.spaceFields.push('invitation');
        }
      } else if (this.spaceTemplate?.spaceFields?.includes?.('invitation')) {
        this.spaceTemplate.spaceFields.splice(this.spaceTemplate.spaceFields.indexOf('invitation'), 1);
      }
    },
    spaceFieldProperties(val) {
      if (!this.drawer) {
        return;
      } else if (val) {
        if (!this.spaceTemplate?.spaceFields?.includes?.('properties')) {
          this.spaceTemplate.spaceFields.push('properties');
        }
      } else if (this.spaceTemplate?.spaceFields?.includes?.('properties')) {
        this.spaceTemplate.spaceFields.splice(this.spaceTemplate.spaceFields.indexOf('properties'), 1);
      }
    },
    spaceFieldAccessControl(val) {
      if (!this.drawer) {
        return;
      } else if (val) {
        if (!this.spaceTemplate?.spaceFields?.includes?.('access')) {
          this.spaceTemplate.spaceFields.push('access');
        }
      } else if (this.spaceTemplate?.spaceFields?.includes?.('access')) {
        this.spaceTemplate.spaceFields.splice(this.spaceTemplate.spaceFields.indexOf('access'), 1);
      }
    },
    canHaveSubspaces() {
      if (!this.canHaveSubspaces) {
        this.resetSubspaceTemplateSection();
      }
    }
  },
  created() {
    this.$root.$on('space-templates-characteristics-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-templates-characteristics-open', this.open);
  },
  methods: {
    async open(spaceTemplate, name, nameTranslations, description, descriptionTranslations, modified, bannerUploadId, bannerData) {
      this.isNew = !spaceTemplate?.id;
      this.basicInformationModified = modified;
      this.spaceTemplate = JSON.parse(JSON.stringify(spaceTemplate));
      this.bannerUploadId = bannerUploadId;
      this.bannerData = bannerData;
      this.originalSpaceTemplate = JSON.parse(JSON.stringify(spaceTemplate));
      this.name = name || spaceTemplate?.name;
      this.description = description || spaceTemplate?.description;
      this.nameTranslations = nameTranslations;
      this.descriptionTranslations = descriptionTranslations;
      this.step = 1;
      this.spaceFieldName = spaceTemplate.spaceFields.includes('name') || false;
      this.spaceFieldInvitation = spaceTemplate.spaceFields.includes('invitation') || false;
      this.spaceFieldProperties = spaceTemplate.spaceFields.includes('properties') || false;
      this.spaceFieldAccessControl = spaceTemplate.spaceFields.includes('access') || false;
      this.canHaveSubspaces = Array.isArray(spaceTemplate?.allowedSubspaceTemplates) &&
          spaceTemplate?.allowedSubspaceTemplates.length > 0 &&
          spaceTemplate?.allowedSubspaceTemplates.some(item => item && item.trim().length > 0);

      if (Array.isArray(spaceTemplate?.allowedSubspaceTemplates) && spaceTemplate?.allowedSubspaceTemplates.length > 0) {
        const allTemplates = await this.$spaceTemplateService.getSpaceTemplates();
        this.selectedSubspaceTemplates = this.canHaveSubspaces
          ? (spaceTemplate?.allowedSubspaceTemplates || []).map(item => {
            const [id, max] = item.split(':');
            const template = allTemplates.find(t => t.id === Number(id));
            return {
              ...template,
              subspacesMaxLimit: Number(max) || 0,
            };
          })
          : [];
        this.subspaceTemplate = [...this.selectedSubspaceTemplates];
      }
      this.subspacesMaxLimit = spaceTemplate?.subspacesMaxLimit;
      this.$refs.drawer.open();
    },
    async close() {
      this.spaceTemplate = null;
      this.originalSpaceTemplate = null;
      this.isNew = false;
      this.basicInformationModified = false;
      await this.$nextTick();
      this.$refs.drawer.close();
    },
    openNameDrawer() {
      this.$root.$emit('space-templates-name-open', this.spaceTemplate, this.name, this.nameTranslations, this.description, this.descriptionTranslations, this.modified, this.bannerUploadId, this.bannerData);
      this.close();
    },
    async save() {
      this.saving = true;
      try {
        if (this.canHaveSubspaces && this.selectedSubspaceTemplates.length > 0) {
          this.spaceTemplate.allowedSubspaceTemplates = this.selectedSubspaceTemplates.map(t => {
            const max = t.subspacesMaxLimit ?? 0;
            return `${t.id}:${max}`;
          });
          this.spaceTemplate.subspacesMaxLimit = this.subspacesMaxLimit;
        } else {
          this.spaceTemplate.allowedSubspaceTemplates = null;
          this.spaceTemplate.subspacesMaxLimit = null;
        }
        this.spaceTemplate.name = this.name;
        this.spaceTemplate.description = this.description;
        if (this.isNew) {
          this.spaceTemplate = await this.$spaceTemplateService.createSpaceTemplate(this.spaceTemplate);
          await this.$nextTick();
        } else {
          await this.$spaceTemplateService.updateSpaceTemplate(this.spaceTemplate);
        }
        if (this.nameTranslations) {
          await this.$translationService.saveTranslations('spaceTemplate', this.spaceTemplate.id, 'name', this.nameTranslations);
        }
        if (this.descriptionTranslations) {
          await this.$translationService.saveRichTranslations('spaceTemplate', this.spaceTemplate.id, 'description', this.descriptionTranslations);
        }
        await this.$refs.bannerInput.save(this.spaceTemplate.id);
        this.spaceTemplate.name = this.name;
        this.spaceTemplate.description = this.description;
        if (this.isNew) {
          this.$root.$emit('alert-message', this.$t('spaceTemplate.spaceTemplateCreatedSuccessfully'), 'success');
          this.$root.$emit('space-templates-created', this.spaceTemplate);
        } else {
          this.$root.$emit('alert-message', this.$t('spaceTemplate.spaceTemplateUpdatedSuccessfully'), 'success');
          this.$root.$emit('space-templates-updated', this.spaceTemplate);
        }
        this.close();
      } finally {
        this.saving = false;
      }
    },
    selectSpaceTemplate(value) {
      this.selectedSubspaceTemplates = value;
    },
    resetSubspaceTemplateSection() {
      this.selectedSubspaceTemplates = [];
      this.subspaceTemplate = null;
      this.subspacesMaxLimit = 0;
      this.invalidSubspacesMaxLimit = false;
    },
    removeSelectedSpaceTemplate(templateId) {
      this.selectedSubspaceTemplates = this.selectedSubspaceTemplates.filter(
        t => Number(t.id) !== templateId
      );
      this.subspaceTemplate = this.subspaceTemplate.filter(
        t => Number(t.id) !== templateId
      );
    },
  },
};
</script>