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
  <v-app class="full-window-height full-width">
    <v-hover v-slot="{ hover }">
      <v-card
        flat
        :color="hasCustomBackground && 'transparent' || 'primary'"
        class="rounded-0  transparent full-height d-flex"
        :class="textAlign">
        <div
          v-if="$root.canEdit"
          class="position-absolute t-0 r-0"
          :class="{
            'l-0': $vuetify.rtl,
            'r-0': !$vuetify.rtl,
          }">
          <v-fab-transition hide-on-leave>
            <v-btn
              :title="$t('sidebarLogin.settings.editTooltip')"
              class="z-index-two me-2 mt-2"
              small
              icon
              @click="$root.$emit('sidebar-login-settings')"
              taxindex="0"
              @focus="focus = true"
              @focusout="focus = false"
              :style="{ opacity: (focus || hover) ? 1 : 0 }">
              <v-icon size="18">fa-cog</v-icon>
            </v-btn>
          </v-fab-transition>
        </div>

        <img
          v-if="hasCustomBackground"
          :src="backgroundPath"
          :alt="backgroundAltText || ''"
          class="full-height full-width object-fit-cover rounded-0">
        <v-card
          :class="hasCustomBackground && 'position-absolute t-0'"
          tile
          flat
          class="fill-height transparent full-width">
          <v-card-title
            class="d-flex flex-column pa-0 fill-height"
            :class="[vAlign, hAlign]">
            <span class="text-title text-wrap text-break mx-4 mt-4">
              {{ title }}
            </span>
            <span class="text-body text-wrap text-break mx-4 mb-4">
              {{ subtitle }}
            </span>
          </v-card-title>
        </v-card>
      </v-card>
    </v-hover>
    <sidebar-login-settings-drawer
      :branding="branding"
      :background-alt-text="this.$root.backgroundFileId !== 0 ? backgroundAltText : null"
      v-if="$root.canEdit" />
  </v-app>
</template>

<script>
export default {
  data: () => ({
    branding: null,
    refreshImageIndex: Date.now(),
    backgroundAltText: null,
    focus: false,
  }),
  computed: {
    title() {
      const title = this.$root.title || this.branding?.loginTitle[eXo.env.portal.language] || this.branding?.loginTitle[eXo.env.portal.defaultLanguage];
      return decodeURIComponent(this.$t(title));
    },
    subtitle() {
      const subtitle = this.$root.subtitle || this.branding?.loginSubtitle[eXo.env.portal.language] || this.branding?.loginSubtitle[eXo.env.portal.defaultLanguage];
      return decodeURIComponent(this.$t(subtitle));
    },
    backgroundPath() {
      if (this.$root.backgroundFileId !== 0) {
        return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/cmsPortlet/${this.$root.translationIdentifier}/${this.$root.backgroundFileId}?refresh=${this.refreshImageIndex}`;
      } else if (this.$root.branding?.loginBackground.fileId !== 0 ) {
        return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/platform/branding/loginBackground?v=${this.refreshImageIndex}`;
      } else {
        return null;
      }
    },
    hasCustomBackground() {
      return this.$root.backgroundFileId !== 0 || this.$root.branding?.loginBackground.fileId !== 0 ;
    },
    vAlign() {
      switch (this.$root.vAlign) {
      case 'START': {
        return 'justify-start';
      }
      case 'END': {
        return 'justify-end';
      }
      default: {
        return 'justify-center';

      }
      }
    },
    hAlign() {
      switch (this.$root.hAlign) {
      case 'START': {
        return 'align-start';
      }
      case 'END': {
        return 'align-end';

      }
      default: {
        return 'align-center';
      }
      }
    },
    textAlign() {
      switch (this.$root.hAlign) {
      case 'START': {
        return 'text-left';
      }
      case 'END': {
        return 'text-right';
      }
      default: {
        return 'text-center';
      }
      }
    }
  },
  created() {
    this.branding = this.$root.branding;
    this.getAltText();
    this.$root.$on('sidebar-login-settings-updated', this.refresh);
  },
  methods: {
    refresh(titleTranslations, subtitleTranslations, vAlign, hAlign, backgroundFileId) {
      this.$root.title = titleTranslations[eXo.env.portal.language] || titleTranslations[eXo.env.portal.defaultLanguage];
      this.$root.subtitle = subtitleTranslations[eXo.env.portal.language] || subtitleTranslations[eXo.env.portal.defaultLanguage];
      this.$root.vAlign=vAlign;
      this.$root.hAlign=hAlign;
      this.$root.backgroundFileId = backgroundFileId;
      this.getAltText();
    },
    getAltText() {
      if (this.$root.backgroundFileId !== 0) {
        this.$fileAttachmentService.getAttachments(this.$root.objectType, this.$root.translationIdentifier).then(data => {
          const imageItem = data?.attachments?.[0];
          if (imageItem) {
            this.backgroundAltText = imageItem.altText;
          }
        });
      } else if (this.$root.branding?.loginBackground.fileId !== 0 ) {
        this.backgroundAltText = this.$root.branding?.loginBackgroundAltText;
      } else {
        this.backgroundAltText = null;
      }
    }
  },
};
</script>
