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
    :loading="saving"
    :confirm-close="modified"
    :confirm-close-labels="closeConfirmLabels"
    class="categoryFormDrawer"
    right>
    <template #title>
      {{ isNew && $t('categoryManagement.drawer.newCategory') || $t('categoryManagement.drawer.editCategory') }}
    </template>
    <template v-if="drawer && category" #content>
      <div class="px-4 pb-4">
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
            {{ $t('categoryManagement.identifyTheCategory') }}
          </div>
        </v-card>
        <div v-if="step === 1" class="d-flex flex-column mb-4">
          <translation-text-field
            id="categoryName"
            v-model="nameTranslations"
            :field-value.sync="name"
            :placeholder="$t('categoryManagement.namePlaceholder')"
            :maxlength="maxNameLength"
            :object-id="categoryId"
            :rules="rules.name"
            object-type="category"
            field-name="name"
            drawer-title="categoryManagement.nameDrawerTitle"
            class="width-auto flex-grow-1 pb-1"
            back-icon
            required
            @initialized="setOriginalInfo">
            <template #title>
              {{ $t('categoryManagement.nameLabel') }}
            </template>
          </translation-text-field>
          <div class="mt-4">
            {{ $t('categoryManagement.selectedParent') }}
          </div>
          <div class="d-flex mt-2">
            <v-icon size="20" class="me-2">{{ parentIcon }}</v-icon>
            <div>{{ parentName }}</div>
          </div>
          <font-icon-input
            v-model="category.icon"
            class="mt-4" />
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
            {{ $t('categoryManagement.categoryAccessPermission') }}
          </div>
        </v-card>
        <div v-show="step === 2" class="mb-4">
          <div>{{ $t('categoryManagement.categoryAccessPermission.title') }}</div>
          <div class="mb-2 text-subtitle">{{ $t('categoryManagement.categoryAccessPermission.subtitle') }}</div>
          <category-management-permissions
            ref="accessPermissions"
            v-model="category.accessPermissionIds"
            class="mb-4"
            users
            admins />
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
            {{ $t('categoryManagement.categoryLinkPermission') }}
          </div>
        </v-card>
        <div
          v-if="step === 3 || category.linkPermissionIds?.length"
          v-show="step === 3"
          class="mb-4">
          <div>{{ $t('categoryManagement.categoryLinkPermission.title') }}</div>
          <div class="mb-2 text-subtitle">{{ $t('categoryManagement.categoryLinkPermission.subtitle') }}</div>
          <category-management-permissions
            ref="linkPermissions"
            v-model="category.linkPermissionIds"
            permissions-type="link"
            class="mb-4"
            users
            admins />
        </div>
      </div>
    </template>
    <template #footer>
      <div class="d-flex align-center">
        <v-btn
          v-if="step > 1"
          :title="$t('categoryManagement.previous')"
          :disabled="saving"
          class="btn me-2 hidden-xs-only"
          @click="step--">
          {{ $t('categoryManagement.previous') }}
        </v-btn>
        <v-btn
          :title="$t('categoryManagement.cancel')"
          class="btn ms-auto me-2"
          @click="close()">
          {{ $t('categoryManagement.cancel') }}
        </v-btn>
        <v-btn
          v-if="step < 3"
          :disabled="disabledNextStep"
          :loading="saving"
          class="btn primary"
          @click="nextStep">
          {{ $t('categoryManagement.next') }}
        </v-btn>
        <v-btn
          v-else
          :disabled="disabled"
          :loading="saving"
          class="btn primary"
          @click="save">
          {{ isNew && $t('categoryManagement.create') || $t('categoryManagement.update') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>
<script>
export default {
  data: () => ({
    drawer: false,
    isNew: false,
    initialized: false,
    saving: false,
    category: null,
    bannerUploadId: null,
    bannerData: null,
    name: null,
    description: null,
    step: 1,
    maxNameLength: 150,
    maxDescriptionLength: 1300,
    nameTranslations: {},
    originalNameTranslations: null,
    originalCategory: null,
  }),
  computed: {
    rules() {
      return {
        name: [
          v => !!v?.length || ' ',
          v => !v?.length || v.length <= this.maxNameLength || this.$t('categoryManagement.nameExceedsMaxLength', {
            0: this.maxNameLength,
          }),
        ],
      };
    },
    modified() {
      return (JSON.stringify(this.category) !== JSON.stringify(this.originalCategory))
        || (JSON.stringify(this.originalNameTranslations) !== JSON.stringify(this.nameTranslations));
    },
    closeConfirmLabels() {
      return {
        title: this.$t('categoryManagement.closeConfirmLabels.title'),
        message: this.$t('categoryManagement.closeConfirmLabels.message'),
        ok: this.$t('categoryManagement.ok'),
        cancel: this.$t('categoryManagement.cancel'),
      };
    },
    disabled() {
      return !this.name?.length || this.name.length > this.maxNameLength;
    },
    disabledFirstStep() {
      return this.disabled;
    },
    step2Enabled() {
      return !this.disabledFirstStep;
    },
    step3Enabled() {
      return this.step2Enabled;
    },
    disabledNextStep() {
      if (this.step === 1) {
        return this.disabledFirstStep;
      } else if (this.step === 2) {
        return !this.step2Enabled;
      } else if (this.step === 3) {
        return !this.step3Enabled;
      } else {
        return false;
      }
    },
    categoryId() {
      return this.category?.id;
    },
    parentCategory() {
      return this.category?.parentId && this.$root.getCategory(this.category.parentId) || this.$root.categoryTree;
    },
    isRoot() {
      return this.parentCategory?.id === this.$root.categoryTree.id;
    },
    parentName() {
      return this.isRoot && this.$t('categoryManagement.rootName') || this.parentCategory?.name;
    },
    parentIcon() {
      return this.isRoot && 'fa-home' || this.parentCategory?.icon;
    },
  },
  created() {
    this.$root.$on('category-form-open', this.open);
  },
  beforeDestroy() {
    this.$root.$off('category-form-open', this.open);
  },
  methods: {
    open(category, parentId) {
      this.isNew = !category?.id;
      this.step = 1;
      category = category && JSON.parse(JSON.stringify(category)) || {
        id: null,
        parentId: parentId || this.$root.categoryRootId,
        name: null,
        icon: 'fa-th-large',
        accessPermissionIds: [],
        linkPermissionIds: [],
        ownerId: this.$root.categoryOwnerId,
      };
      delete category.categories;
      this.category = category;
      this.name = this.category.name || null;
      this.nameTranslations = null;
      this.initialized = false;
      this.$refs.drawer.open();
    },
    nextStep() {
      if (this.step === 2 && this.drawer && this.isNew && !this.category.linkPermissionIds?.length) {
        this.category.linkPermissionIds = this.category.accessPermissionIds.slice();
      }
      this.step++;
    },
    setOriginalInfo() {
      if (!this.initialized) {
        this.originalCategory = JSON.parse(JSON.stringify(this.category));
        this.originalNameTranslations = JSON.parse(JSON.stringify(this.nameTranslations));
      }
    },
    async close() {
      this.category = null;
      this.nameTranslations = null;
      this.originalCategory = null;
      this.originalNameTranslations = null;
      await this.$nextTick();
      this.$refs.drawer.close();
    },
    async save() {
      this.saving = true;
      try {
        if (this.isNew) {
          this.category = await this.$categoryService.createCategory(this.category);
          await this.$nextTick();
        } else {
          await this.$categoryService.updateCategory(this.category);
        }
        if (this.nameTranslations) {
          await this.$translationService.saveTranslations('category', this.category.id, 'name', this.nameTranslations);
        }
        this.category.name = this.name;
        if (this.isNew) {
          this.$root.$emit('alert-message', this.$t('categoryManagement.categoryCreatedSuccessfully'), 'success');
          this.$root.$emit('category-created', this.category);
        } else {
          this.$root.$emit('alert-message', this.$t('categoryManagement.categoryUpdatedSuccessfully'), 'success');
          this.$root.$emit('category-updated', this.category);
        }
        this.close();
      } catch (e) {
        if (this.isNew) {
          this.$root.$emit('alert-message', this.$t('categoryManagement.categoryCreateError'), 'success');
        } else {
          this.$root.$emit('alert-message', this.$t('categoryManagement.categoryUpdateError'), 'success');
        }
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>