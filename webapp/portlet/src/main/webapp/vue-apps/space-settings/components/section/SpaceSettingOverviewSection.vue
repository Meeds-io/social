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
  <widget-wrapper v-if="$root.space" no-margin>
    <template #title>
      <v-list-item class="px-0 pt-2px mt-n1 mb-n5">
        <v-list-item-action class="my-auto me-3 ms-n2">
          <v-btn
            :title="$t('generalSettings.access.backToMain')"
            size="24"
            icon
            @click="$root.showMain">
            <v-icon size="18" class="icon-default-color">
              {{ $vuetify.rtl && 'fa-arrow-right' || 'fa-arrow-left' }}
            </v-icon>
          </v-btn>
        </v-list-item-action>
        <v-list-item-content>
          <v-list-item-title>
            <v-card
              :title="$t('SpaceSettings.backToMain')"
              class="flex-grow-0 text-title text-start py-1"
              flat
              @click="$root.showMain">
              {{ $t('SpaceSettings.overview') }}
            </v-card>
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
    </template>
    <template v-if="space" #default>
      <form
        ref="form"
        :disabled="savingSpace"
        class="mx-0 mx-sm-n2 d-flex flex-column"
        @submit.prevent.stop="saveSpace">
        <div class="d-flex flex-column flex-sm-row">
          <div class="d-flex flex-column flex-grow-1 flex-shrink-0 col-12 col-sm-6 pa-0 mx-0 mx-sm-2">
            <div id="spaceSettingsName" class="my-4 text-header">
              {{ $t('SpaceSettings.label.name') }}
            </div>
            <input
              v-model="displayName"
              :aria-label="$t('SpaceSettings.label.name')"
              :placeholder="$t('SpaceSettings.label.displayName')"
              name="name"
              maxlength="200"
              class="input-block-level ignore-vuetify-classes mb-2"
              required>
            <div class="my-4 text-header">
              {{ $t('SpaceSettings.label.description') }}
            </div>
            <rich-editor
              id="spaceDescriptionRichEditor"
              v-model="description"
              :placeholder="$t('SpaceSettings.label.description')"
              :max-length="maxDescriptionLength"
              :tag-enabled="false"
              class="mb-2"
              ck-editor-type="spaceDescription"
              disable-suggester />
          </div>
          <div class="d-flex flex-column flex-grow-1 flex-shrink-1 col-12 col-sm-6 pa-0 mx-0 mx-sm-2">
            <space-setting-avatar
              v-model="avatarUploadId" />
            <space-setting-banner
              v-model="bannerUploadId" />
          </div>
        </div>
      </form>
      <div class="d-flex mt-8">
        <v-spacer />
        <v-btn
          :disabled="savingSpace"
          class="btn me-4"
          @click="$root.showMain">
          {{ $t('SpaceSettings.button.cancel') }}
        </v-btn>
        <v-btn
          :loading="savingSpace"
          :disabled="saveButtonDisabled"
          class="btn btn-primary"
          @click.prevent.stop="saveSpace">
          {{ $t('SpaceSettings.button.updateSpace') }}
        </v-btn>
      </div>
    </template>
  </widget-wrapper>
</template>
<script>
export default {
  props: {
    maxUploadSize: {
      type: Number,
      default: () => 2,
    },
  },
  data: () => ({
    space: {},
    displayName: null,
    description: null,
    avatarUploadId: null,
    bannerUploadId: null,
    savingSpace: false,
    maxDescriptionLength: 2000,
    cropOptions: {
      aspectRatio: 1,
      viewMode: 1,
    },
  }),
  computed: {
    saveButtonDisabled() {
      return this.savingSpace
        || !this.displayName
        || (this.description?.length || 0) > this.maxDescriptionLength;
    },
    maxUploadSizeInBytes() {
      return Number(this.maxUploadSize) * 1024 *1024;
    },
  },
  created() {
    this.reset();
  },
  methods: {
    reset() {
      this.displayName = this.$root?.space?.displayName;
      this.description = this.$root?.space?.description;
      this.avatarUploadId = null;
      this.bannerUploadId = null;
    },
    saveSpace() {
      if (this.savingSpace) {
        return;
      }
      this.savingSpace = true;
      this.$spaceService.updateSpace({
        id: this.$root.spaceId,
        displayName: this.displayName,
        description: this.description,
        avatarId: this.avatarUploadId,
        bannerId: this.bannerUploadId,
      })
        .then(space => {
          this.avatarUploadId = null;
          this.bannerUploadId = null;
          this.$root.$emit('space-settings-updated', space);
          this.$root.$emit('alert-message', this.$t('SpaceSettings.successfullySaved'), 'success');
        })
        .catch(e => {
          if (String(e).indexOf('SPACE_ALREADY_EXIST') >= 0) {
            this.$root.$emit('alert-message', this.$t('SpaceSettings.error.spaceWithSameNameExists'), 'error');
          } else if (String(e).indexOf('INVALID_SPACE_NAME') >= 0) {
            this.$root.$emit('alert-message', this.$t('spacesList.error.InvalidSpaceName'), 'error');
          } else {
            this.$root.$emit('alert-message', this.$t('SpaceSettings.error.unknownErrorWhenSavingSpace'), 'error');
          }
        })
        .finally(() => this.savingSpace = false);
    },
  },
};
</script>
