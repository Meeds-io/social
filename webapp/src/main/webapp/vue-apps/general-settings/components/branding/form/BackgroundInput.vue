<!--

 This file is part of the Meeds project (https://meeds.io/).

 Copyright (C) 2020 - 2025 Meeds Association contact@meeds.io

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
  <div>
    <v-list-item
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        <v-radio-group
          v-model="choice"
          class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
          mandatory>
          <v-radio
            value="color"
            class="mx-0">
            <template #label>
              <span>{{ $t('generalSettings.color') }}</span>
            </template>
          </v-radio>
          <v-radio
            value="gradient"
            class="mx-0">
            <template #label>
              <span>{{ $t('generalSettings.gradient') }}</span>
            </template>
          </v-radio>
        </v-radio-group>
      </v-list-item-content>
      <v-list-item-action
        :class="choice === 'color' && 'mb-auto' || 'my-auto'"
        class="me-0 ms-auto">
        <portal-general-settings-color-picker
          v-if="choice === 'color'"
          v-model="branding.backgroundColor"
          :height="colorInputHeight"
          :width="colorInputWidth"
          class="my-auto" />
        <div v-else>
          <portal-general-settings-color-picker
            v-model="backgroundGradientFrom"
            :label="$t('generalSettings.gradientFrom')"
            :height="colorInputHeight"
            :width="colorInputWidth"
            class="ma-auto" />
          <portal-general-settings-color-picker
            v-model="backgroundGradientTo"
            :label="$t('generalSettings.gradientTo')"
            :height="colorInputHeight"
            :width="colorInputWidth"
            class="ma-auto" />
        </div>
      </v-list-item-action>
    </v-list-item>

    <v-list-item
      class="pa-0"
      dense>
      <v-list-item-content class="my-auto">
        {{ $t('generalSettings.image') }}
      </v-list-item-content>
      <v-list-item-action class="my-auto me-0 ms-auto">
        <portal-general-settings-background-image-attachment
          v-model="backgroundImageUploadId"
          :has-file="hasFile"
          class="ms-auto my-auto me-n2"
          @image-data-updated="setbackgroundData"
          @reset="deletebackground" />
      </v-list-item-action>
    </v-list-item>
    <div v-if="hasFile" class="d-flex">
      <v-radio-group
        v-model="backgroundImageStyle"
        class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
        mandatory>
        <v-radio
          value="cover"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imageSizeCover') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="contain"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imageSizeContain') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="repeat"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imageRepeat') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="no-repeat"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imageNoRepeat') }}</span>
          </template>
        </v-radio>
      </v-radio-group>
      <v-radio-group
        v-model="branding.backgroundPosition"
        class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
        mandatory>
        <v-radio
          value="top left"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imagePositionTopLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="top right"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imagePositionTopRight') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="bottom left"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imagePositionBottomLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          value="bottom right"
          class="mx-0">
          <template #label>
            <span>{{ $t('generalSettings.imagePositionBottomRight') }}</span>
          </template>
        </v-radio>
      </v-radio-group>
    </div>
  </div>
</template>
<script>
export default {
  props: {
    value: {
      type: Object,
      default: null,
    },
    defaultBackgroundColor: {
      type: String,
      default: () => '#FFFFFFFF',
    },
  },
  data: () => ({
    branding: null,
    choice: null,
    backgroundImageStyle: null,
    backgroundGradientFrom: null,
    backgroundGradientTo: null,
    backgroundImageUploadId: null,
    initialized: false,
    defaultBranding: null,
    colorInputWidth: '36px',
    colorInputHeight: '36px'
  }),
  computed: {
    hasFile() {
      return this.backgroundImageUploadId !== 0 && (this.backgroundImageUploadId || this.branding.background?.fileId !== 0);
    },
  },
  watch: {
    branding: {
      deep: true,
      handler() {
        if (this.branding) {
          this.$emit('input', this.branding);
        }
      },
    },
    backgroundImageStyle() {
      if (this.initialized) {
        if (this.backgroundImageStyle === 'cover' || this.backgroundImageStyle === 'contain') {
          this.branding.backgroundSize = this.backgroundImageStyle;
          this.branding.backgroundRepeat = null;
        } else {
          this.branding.backgroundSize = null;
          this.branding.backgroundRepeat = this.backgroundImageStyle;
        }
      }
    },
    backgroundGradientFrom() {
      if (this.backgroundGradientFrom && this.backgroundGradientTo && this.choice === 'gradient') {
        this.branding.backgroundEffect = `linear-gradient(${this.backgroundGradientFrom}, ${this.backgroundGradientTo})`;
      } else {
        this.branding.backgroundEffect = null;
      }
    },
    backgroundGradientTo() {
      if (this.backgroundGradientFrom && this.backgroundGradientTo && this.choice === 'gradient') {
        this.branding.backgroundEffect = `linear-gradient(${this.backgroundGradientFrom}, ${this.backgroundGradientTo})`;
      } else {
        this.branding.backgroundEffect = null;
      }
    },
    choice() {
      if (this.initialized) {
        if (this.choice === 'color') {
          this.branding.backgroundColor = this.branding.backgroundColor || this.defaultBackgroundColor;
          this.backgroundGradientFrom = null;
          this.backgroundGradientTo = null;
        } else if (this.choice === 'gradient') {
          if (this.branding.backgroundEffect && this.branding.backgroundEffect.startsWith('linear-gradient(')) {
            this.backgroundGradientFrom = this.branding.backgroundEffect.replace('linear-gradient(', '').split(',')[0].trim();
            this.backgroundGradientTo = this.branding.backgroundEffect.replace('linear-gradient(', '').split(',')[1].replace(/\)$/g, '').trim();
          } else {
            this.backgroundGradientFrom = this.defaultBackgroundColor;
            this.backgroundGradientTo = '#F0F0F0FF';
          }
          this.branding.backgroundColor = this.defaultBackgroundColor;
        }
      }
    },
    backgroundImageUploadId() {
      if (this.initialized && this.branding.background) {
        this.branding.background.uploadId = this.backgroundImageUploadId || 0;
        this.branding.background.fileId = 0;
      }
    },
  },
  created() {
    this.branding = this.value;
    if (this.branding.backgroundSize || this.branding.backgroundRepeat) {
      if (this.branding.backgroundSize === 'cover'
          || this.branding.backgroundSize === 'contain') {
        this.backgroundImageStyle = this.branding.backgroundSize;
      } else {
        this.backgroundImageStyle = this.branding.backgroundRepeat;
      }
    }
    if (this.branding.backgroundEffect && this.branding.backgroundEffect.startsWith('linear-gradient(')) {
      this.choice = 'gradient';
      this.backgroundGradientFrom = this.branding.backgroundEffect.replace('linear-gradient(', '').split(',')[0].trim();
      this.backgroundGradientTo = this.branding.backgroundEffect.replace('linear-gradient(', '').split(',')[1].replace(/\)$/g, '').trim();
    } else {
      this.choice = 'color';
    }
    this.backgroundImageUploadId = this.branding?.background?.uploadId;
    this.defaultBranding = JSON.parse(JSON.stringify(this.branding));
    this.$nextTick().then(() => this.initialized = true);
  },
  methods: {
    deletebackground() {
      this.branding.background = {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      };
    },
    setbackgroundData(data) {
      this.branding.background.data = data;
      this.$emit('input', this.branding);
    },
  },
};
</script>