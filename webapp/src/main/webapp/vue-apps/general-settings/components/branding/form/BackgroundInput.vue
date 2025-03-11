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
            class="mx-0"
            value="color">
            <template #label>
              <span>{{ $t('generalSettings.color') }}</span>
            </template>
          </v-radio>
          <v-radio
            class="mx-0"
            value="gradient">
            <template #label>
              <span>{{ $t('generalSettings.gradient') }}</span>
            </template>
          </v-radio>
        </v-radio-group>
      </v-list-item-content>
      <v-list-item-action
        class="me-0 ms-auto"
        :class="choice === 'color' && 'mb-auto' || 'my-auto'">
        <portal-general-settings-color-picker
          v-if="choice === 'color'"
          v-model="branding.backgroundColor"
          class="my-auto"
          :height="colorInputHeight"
          :width="colorInputWidth" />
        <div v-else>
          <portal-general-settings-color-picker
            v-model="backgroundGradientFrom"
            class="ma-auto"
            :height="colorInputHeight"
            :label="$t('generalSettings.gradientFrom')"
            :width="colorInputWidth" />
          <portal-general-settings-color-picker
            v-model="backgroundGradientTo"
            class="ma-auto"
            :height="colorInputHeight"
            :label="$t('generalSettings.gradientTo')"
            :width="colorInputWidth" />
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
          class="ms-auto my-auto me-n2"
          :has-file="hasFile"
          @image-data-updated="setbackgroundData"
          @reset="deletebackground" />
      </v-list-item-action>
    </v-list-item>
    <div
      v-if="hasFile"
      class="d-flex">
      <v-radio-group
        v-model="backgroundImageStyle"
        class="my-auto text-no-wrap flex-grow-1 flex-shrink-0"
        mandatory>
        <v-radio
          class="mx-0"
          value="cover">
          <template #label>
            <span>{{ $t('generalSettings.imageSizeCover') }}</span>
          </template>
        </v-radio>
        <v-radio
          class="mx-0"
          value="contain">
          <template #label>
            <span>{{ $t('generalSettings.imageSizeContain') }}</span>
          </template>
        </v-radio>
        <v-radio
          class="mx-0"
          value="repeat">
          <template #label>
            <span>{{ $t('generalSettings.imageRepeat') }}</span>
          </template>
        </v-radio>
        <v-radio
          class="mx-0"
          value="no-repeat">
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
          class="mx-0"
          value="top left">
          <template #label>
            <span>{{ $t('generalSettings.imagePositionTopLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          class="mx-0"
          value="top right">
          <template #label>
            <span>{{ $t('generalSettings.imagePositionTopRight') }}</span>
          </template>
        </v-radio>
        <v-radio
          class="mx-0"
          value="bottom left">
          <template #label>
            <span>{{ $t('generalSettings.imagePositionBottomLeft') }}</span>
          </template>
        </v-radio>
        <v-radio
          class="mx-0"
          value="bottom right">
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
      colorInputHeight: '36px',
    }),
    computed: {
      hasFile () {
        return this.backgroundImageUploadId !== 0 && (this.backgroundImageUploadId || this.branding.background?.fileId !== 0);
      },
    },
    watch: {
      branding: {
        deep: true,
        handler () {
          if (this.branding) {
            this.$emit('input', this.branding);
          }
        },
      },
      backgroundImageStyle () {
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
      backgroundGradientFrom () {
        if (this.backgroundGradientFrom && this.backgroundGradientTo && this.choice === 'gradient') {
          this.branding.backgroundEffect = `linear-gradient(${this.backgroundGradientFrom}, ${this.backgroundGradientTo})`;
        } else {
          this.branding.backgroundEffect = null;
        }
      },
      backgroundGradientTo () {
        if (this.backgroundGradientFrom && this.backgroundGradientTo && this.choice === 'gradient') {
          this.branding.backgroundEffect = `linear-gradient(${this.backgroundGradientFrom}, ${this.backgroundGradientTo})`;
        } else {
          this.branding.backgroundEffect = null;
        }
      },
      choice () {
        if (this.initialized) {
          if (this.choice === 'color') {
            this.branding.backgroundColor = this.branding.backgroundColor || this.defaultBackgroundColor;
            this.backgroundGradientFrom = null;
            this.backgroundGradientTo = null;
          } else if (this.choice === 'gradient') {
            if (this.branding.backgroundEffect) {
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
      backgroundImageUploadId () {
        if (this.initialized && this.branding.background) {
          this.branding.background.uploadId = this.backgroundImageUploadId || 0;
          this.branding.background.fileId = 0;
        }
      },
    },
    created () {
      this.branding = this.value;
      if (this.branding.backgroundSize || this.branding.backgroundRepeat) {
        if (this.branding.backgroundSize === 'cover'
          || this.branding.backgroundSize === 'contain') {
          this.backgroundImageStyle = this.branding.backgroundSize;
        } else {
          this.backgroundImageStyle = this.branding.backgroundRepeat;
        }
      }
      if (this.branding.backgroundEffect) {
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
      deletebackground () {
        this.branding.background = {
          data: null,
          fileId: 0,
          updatedDate: 0,
          uploadId: 0,
        };
      },
      setbackgroundData (data) {
        this.branding.background.data = data;
        this.$emit('input', this.branding);
      },
    },
  };
</script>