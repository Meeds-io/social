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
      <div class="pa-4" flat>
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
            {{ $t('spaceTemplate.mandatoryCreationStep') }}
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
            {{ $t('spaceTemplate.defaultSpaceConfigurationStep') }}
          </div>
        </v-card>
        <div v-show="step === 2" class="mb-4">
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
            <space-templates-management-access
              v-model="spaceTemplate.spaceDefaultRegistration" />
            <space-templates-management-visibility
              v-model="spaceTemplate.spaceDefaultVisibility"
              class="mb-n2" />
          </div>
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
            {{ $t('spaceTemplate.permissionsStep') }}
          </div>
        </v-card>
        <div v-if="step === 3" class="d-flex flex-column mb-4">
          <span v-sanitized-html="permissionsStepDescription1" class="mb-2"></span>
          <span class="mb-4">
            {{ $t('spaceTemplate.permissionsStepDescription2') }}
          </span>
          <space-templates-management-permissions
            v-model="spaceTemplate.permissions"
            label="spaceTemplate.permissionsStepCreateSpacePermissionLabel"
            class="mb-4"
            users
            admins />
          <space-templates-management-permissions
            v-model="spaceTemplate.spaceLayoutPermissions"
            label="spaceTemplate.permissionsStepEditSpaceLayoutPermissionLabel"
            class="mb-4"
            admins
            space-admin />
          <space-templates-management-permissions-editorial
            v-model="spaceTemplate.spaceAllowContentCreation"
            class="mb-4" />
          <space-templates-management-permissions
            v-model="spaceTemplate.spaceDeletePermissions"
            label="spaceTemplate.permissionsStepDeleteSpacePermissionLabel"
            class="mb-4"
            admins
            space-admin />
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
          v-if="step < 3"
          :disabled="disabledFirstStep"
          :loading="saving"
          class="btn primary"
          @click="step++">
          {{ $t('spaceTemplate.next') }}
        </v-btn>
        <v-btn
          v-else-if="step > 2"
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
          || !this.spaceTemplate?.permissions?.length
          || !this.spaceTemplate?.spaceLayoutPermissions?.length
          || !this.spaceTemplate?.spaceDeletePermissions?.length
          || (!this.spaceTemplate?.spaceFields?.includes?.('name') && !this.spaceTemplate?.spaceFields?.includes?.('invitation'));
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
  },
  created() {
    this.$root.$on('space-templates-characteristics-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('space-templates-characteristics-open', this.open);
  },
  methods: {
    open(spaceTemplate, name, nameTranslations, description, descriptionTranslations, modified, bannerUploadId, bannerData) {
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
          await this.$translationService.saveTranslations('spaceTemplate', this.spaceTemplate.id, 'description', this.descriptionTranslations);
        }
        await this.$refs.bannerInput.save(this.spaceTemplate.id);
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
  },
};
</script>