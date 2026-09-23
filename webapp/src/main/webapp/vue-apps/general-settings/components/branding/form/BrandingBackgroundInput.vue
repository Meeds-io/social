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
  <styling-background-input
    v-if="container"
    v-model="container"
    :default-background-color="defaultBackgroundColor"
    :scroll-color="stickyOption && !!branding.sticky"
    :no-gradient="noGradient"
    :hide-switch="hideSwitch"
    hide-title
    no-layer-options>
    <template v-if="stickyOption" #after-color>
      <v-list-item
        class="pa-0"
        dense>
        <v-list-item-content class="my-auto">
          {{ $t('generalSettings.topBar.fixPositionWhenScrolling') }}
        </v-list-item-content>
        <v-list-item-action class="my-auto me-0 ms-auto">
          <v-switch
            v-model="branding.sticky"
            class="my-auto me-n2" />
        </v-list-item-action>
      </v-list-item>
      <div class="text-subtitle mb-2">
        {{ $t('generalSettings.topBar.fixPositionWhenScrolling.help') }}
      </div>
    </template>
    <template #image>
      <portal-general-settings-background-image-attachment
        v-model="backgroundImageUploadId"
        :has-file="hasFile"
        class="ms-auto my-auto me-n2"
        @image-data-updated="setBackgroundData"
        @reset="deleteBackground" />
    </template>
  </styling-background-input>
</template>
<script>
/**
 * Adapter between a Branding and Theme area (a "branding" object: colour,
 * gradient, uploaded file, image options, optional fixed-on-scroll state) and
 * the shared styling background input of social's stylingInputs module, which
 * works on a container-shaped object and attaches its image through a slot.
 */
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
    // Page background only: the image attachment option (Scrolling) is persisted
    scrolling: {
      type: Boolean,
      default: false,
    },
    // Topbar: the gradient choice is removed, a plain colour is offered
    noGradient: {
      type: Boolean,
      default: false,
    },
    // Topbar: "Fix Position when Scrolling" switch under the Colour field; when on,
    // the colour splits into On Top (backgroundColor) and On Scroll (backgroundScrollColor)
    stickyOption: {
      type: Boolean,
      default: false,
    },
    // Branding areas always have a background: no enable/disable switch by default
    hideSwitch: {
      type: Boolean,
      default: true,
    },
    // URL of the stored image, used to show the image options when a file exists
    imagePath: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    branding: null,
    container: null,
    backgroundImageUploadId: null,
    backgroundData: null,
    initialized: false,
  }),
  computed: {
    hasFile() {
      return this.backgroundImageUploadId !== 0 && (this.backgroundImageUploadId || this.branding?.background?.fileId !== 0);
    },
    imageSrc() {
      if (this.backgroundData) {
        return this.backgroundData;
      } else if (this.hasFile) {
        return this.imagePath || 'branding-image';
      }
      return null;
    },
  },
  watch: {
    container: {
      deep: true,
      handler() {
        if (this.initialized) {
          this.applyContainer();
        }
      },
    },
    'branding.sticky'() {
      if (this.initialized && this.stickyOption && !this.branding.sticky) {
        this.branding.backgroundScrollColor = null;
      }
    },
    imageSrc() {
      if (this.initialized) {
        this.container.backgroundImage = this.imageSrc;
        if (!this.imageSrc) {
          // the image options are meaningless without an image
          this.container.backgroundSize = null;
          this.container.backgroundPosition = null;
          this.container.backgroundRepeat = null;
          this.container.backgroundAttachment = null;
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
    this.backgroundImageUploadId = this.branding?.background?.uploadId;
    const scrollColor = this.stickyOption && this.branding.sticky && (this.branding.backgroundScrollColor || this.branding.backgroundColor);
    this.container = {
      backgroundColor: this.branding.backgroundColor
        && (scrollColor ? `${this.branding.backgroundColor}@${scrollColor}` : this.branding.backgroundColor)
        || null,
      backgroundEffect: this.noGradient ? null : (this.branding.backgroundEffect || null),
      backgroundImage: this.imageSrc,
      backgroundPosition: this.branding.backgroundPosition || null,
      backgroundSize: this.branding.backgroundSize || null,
      backgroundRepeat: this.branding.backgroundRepeat || null,
      backgroundAttachment: this.scrolling && this.branding.backgroundAttachment || null,
    };
    if (this.noGradient && this.branding.backgroundEffect) {
      // the gradient option is removed for this area: an existing gradient falls back to the plain colour
      this.branding.backgroundEffect = null;
    }
    this.$nextTick().then(() => this.initialized = true);
  },
  methods: {
    applyContainer() {
      const color = this.container.backgroundColor;
      if (this.stickyOption && this.branding.sticky && color?.includes?.('@')) {
        this.branding.backgroundColor = color.split('@')[0];
        this.branding.backgroundScrollColor = color.split('@')[1];
      } else {
        this.branding.backgroundColor = color?.includes?.('@') ? color.split('@')[0] : color;
        if (!this.stickyOption || !this.branding.sticky) {
          this.branding.backgroundScrollColor = null;
        }
      }
      this.branding.backgroundEffect = this.container.backgroundEffect || null;
      this.branding.backgroundPosition = this.container.backgroundPosition || null;
      this.branding.backgroundSize = this.container.backgroundSize || null;
      this.branding.backgroundRepeat = this.container.backgroundRepeat || null;
      if (this.scrolling) {
        this.branding.backgroundAttachment = this.container.backgroundAttachment || null;
      }
      this.$emit('input', this.branding);
    },
    deleteBackground() {
      this.branding.background = {
        data: null,
        fileId: 0,
        updatedDate: 0,
        uploadId: 0,
      };
      this.backgroundData = null;
      this.$emit('input', this.branding);
    },
    setBackgroundData(data) {
      this.branding.background.data = data;
      this.backgroundData = data;
      this.$emit('input', this.branding);
    },
  },
};
</script>
