<!--
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2025 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<template>
  <exo-drawer
    ref="profileHeaderSettingsDrawer"
    id="profileHeaderSettingsDrawer"
    :right="!$vuetify.rtl"
    @closed="reset">
    <template #title>
      <div class="d-flex my-auto text-title font-weight-bold text-color">
        {{ $t('profileHeader.edit.settings.title') }}
      </div>
    </template>
    <template #content>
      <v-form ref="form">
        <div class="pa-5">
          <label
            for="displayOption"
            class="v-label text-header">
            {{ $t('profileHeader.displayOptions.label') }}
          </label>
          <v-radio-group
            name="displayOption"
            v-model="displayOption"
            mandatory>
            <v-radio
              :label="$t('profileHeader.display.option.one.label')"
              value="name" />
            <v-radio
              :label="$t('profileHeader.display.option.two.label')"
              value="welcome" />
          </v-radio-group>
          <label class="v-label mt-3 text-header">
            {{ $t('profileHeader.advancedDisplay.label') }}
          </label>
          <p class="text-color mb-0 mt-5 text-font-size font-weight-bold">
            {{ $t('profileHeader.avatarSize.label') }}
          </p>
          <div class="d-flex align-center">
            <label class="v-label mt-2 text-color">
              {{ $t('profileHeader.minimum.label') }}
            </label>
            <div class="ms-auto">
              <number-input
                v-model="avatarMinSize"
                :min="20"
                :max="400"
                :step="1"
                editable />
            </div>
          </div>
          <div class="d-flex align-center">
            <label class="v-label text-color">
              {{ $t('profileHeader.maximum.label') }}
            </label>
            <div class="ms-auto">
              <number-input
                v-model="avatarMaxSize"
                :min="20"
                :max="400"
                :step="1"
                editable />
            </div>
          </div>
          <p class="text-color mb-0 mt-2 text-font-size font-weight-bold">
            {{ $t('profileHeader.bannerSize.label') }}
          </p>
          <div class="d-flex mt-1 align-center">
            <label class="v-label text-color">
              {{ $t('profileHeader.maxHeight.label') }}
            </label>
            <div class="ms-auto">
              <number-input
                v-model="bannerMaxHeight"
                :min="60"
                :max="400"
                :step="1"
                editable />
            </div>
          </div>
          <div class="mt-1 align-start">
            <label
              for="bannerHeightOption"
              class="v-label text-color">
              {{ $t('profileHeader.height.label') }}
            </label>
            <v-radio-group
              name="bannerHeightOption"
              v-model="bannerHeightOption"
              mandatory>
              <v-radio
                :label="$t('profileHeader.height.auto.label')"
                value="auto"
                class="my-auto" />
              <div
                :class="{'mt-2': bannerAutoHeightOption}"
                class="d-flex align-center">
                <v-radio
                  :label="$t('profileHeader.height.fixed.label')"
                  class="my-auto"
                  value="fixed" />
                <div
                  v-if="!bannerAutoHeightOption"
                  class="ms-auto">
                  <number-input
                    v-model="bannerHeight"
                    :min="60"
                    :max="bannerMaxHeight"
                    :step="1"
                    editable />
                </div>
              </div>
            </v-radio-group>
          </div>
        </div>
      </v-form>
    </template>
    <template #footer>
      <div class="d-flex width-fit-content ms-auto">
        <v-btn
          class="btn me-5"
          @click="reset">
          {{ $t('profileHeader.settings.cancel.label') }}
        </v-btn>
        <v-btn
          class="btn btn-primary"
          :disabled="!enableSave"
          :loading="isSaving"
          @click="save">
          {{ $t('profileHeader.settings.save.label') }}
        </v-btn>
      </div>
    </template>
  </exo-drawer>
</template>

<script>

export default {
  data() {
    return {
      defaultBannerHeight: 'auto',
      displayOption: 'name',
      avatarMinSize: 44,
      avatarMaxSize: 160,
      bannerMaxHeight: 175,
      bannerHeightOption: 'auto',
      bannerHeight: 175,
      isSaving: false
    };
  },
  props: {
    savedSettings: {
      type: Object,
      default: null
    },
    saveSettingsUrl: {
      type: String,
      default: null
    }
  },
  watch: {
    bannerMaxHeight() {
      if (this.bannerHeight > this.bannerMaxHeight) {
        this.bannerHeight = this.bannerMaxHeight;
      }
    },
    bannerHeightOption() {
      if (!this.bannerAutoHeightOption && this.savedSettings.bannerHeight === this.defaultBannerHeight) {
        this.bannerHeight = this.bannerMaxHeight;
      } else if (this.bannerAutoHeightOption) {
        this.bannerHeight = this.defaultBannerHeight;
      } else {
        this.bannerHeight = this.savedSettings.bannerHeight;
      }
    }
  },
  computed: {
    bannerAutoHeightOption() {
      return this.bannerHeightOption === this.defaultBannerHeight;
    },
    enableSave() {
      return this.savedSettings.avatarMinSize !== this.avatarMinSize || this.savedSettings.avatarMaxSize !== this.avatarMaxSize
                                                                     || this.savedSettings.bannerMaxHeight !== this.bannerMaxHeight
                                                                     || this.savedSettings.displayOption !== this.displayOption
                                                                     || this.savedSettings.bannerHeight !== this.bannerHeight;
    }
  },
  methods: {
    open() {
      this.restoreSavedSettings();
      this.$refs.profileHeaderSettingsDrawer.open();
    },
    close() {
      this.$refs.profileHeaderSettingsDrawer.close();
    },
    reset() {
      this.restoreSavedSettings();
    },
    restoreSavedSettings() {
      this.displayOption = this.savedSettings?.displayOption;
      this.avatarMaxSize = this.savedSettings.avatarMaxSize;
      this.avatarMinSize = this.savedSettings.avatarMinSize;
      this.bannerMaxHeight = this.savedSettings.bannerMaxHeight;
      this.bannerHeightOption = (this.savedSettings.bannerHeight !== this.defaultBannerHeight) ? 'fixed' : this.defaultBannerHeight;
      this.bannerHeight = this.savedSettings.bannerHeight;
    },
    save() {
      this.isSaving = true;
      const settings = {
        displayOption: this.displayOption,
        avatarMaxSize: this.avatarMaxSize,
        avatarMinSize: this.avatarMinSize,
        bannerMaxHeight: this.bannerMaxHeight,
        bannerHeight: (this.bannerHeightOption === this.defaultBannerHeight) ? this.defaultBannerHeight : this.bannerHeight
      };
      this.$profileHeaderService.saveSettings(this.saveSettingsUrl , settings).then(() => {
        this.$emit('updated', settings);
        this.$root.$emit('alert-message', this.$t('profileHeader.settings.save.success.message'), 'success');
      }).catch(() => {
        this.$root.$emit('alert-message', this.$t('profileHeader.settings.save.error.message'), 'error');
      }).finally(() => this.isSaving = false);
    }
  }
};
</script>
