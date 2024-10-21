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
  <component
    v-model="menu"
    :is="$root.isMobile && 'v-bottom-sheet' || 'v-menu'"
    :left="!$vuetify.rtl"
    :right="$vuetify.rtl"
    :content-class="menuId"
    offset-y>
    <template #activator="{ on, attrs }">
      <v-btn
        :aria-label="$t('spaceTemplates.menu.open')"
        icon
        small
        class="mx-auto"
        v-bind="attrs"
        v-on="on">
        <v-icon size="16" class="icon-default-color">fas fa-ellipsis-v</v-icon>
      </v-btn>
    </template>
    <v-hover v-if="menu" @input="hoverMenu = $event">
      <v-list
        class="pa-0"
        dense
        @mouseout="menu = false"
        @focusout="menu = false">
        <v-subheader v-if="$root.isMobile">
          <div class="d-flex full-width">
            <div class="d-flex flex-grow-1 flex-shrink-1 align-center subtitle-1 text-truncate">
              {{ $t('spaceTemplate.label.templateMenu', {0: name}) }}
            </div>
            <div class="flex-shrink-0">
              <v-btn
                :aria-label="$t('spaceTemplate.label.closeMenu')"
                icon
                @click="menu = false">
                <v-icon>fa-times</v-icon>
              </v-btn>
            </div>
          </div>
        </v-subheader>
        <v-list-item-group v-model="listItem">
          <v-list-item
            dense
            @click="$root.$emit('space-templates-characteristics-open', spaceTemplate)">
            <v-icon size="13">
              fa-edit
            </v-icon>
            <v-list-item-title class="ps-2">
              {{ $t('spaceTemplate.label.editProperties') }}
            </v-list-item-title>
          </v-list-item>
          <v-list-item
            dense
            @click="duplicate">
            <v-icon size="13">
              fa-copy
            </v-icon>
            <v-list-item-title class="ps-2">
              {{ $t('spaceTemplate.label.duplicate') }}
            </v-list-item-title>
          </v-list-item>
          <v-tooltip :disabled="!spaceTemplate.system" bottom>
            <template #activator="{ on, attrs }">
              <div
                v-on="on"
                v-bind="attrs">
                <v-list-item
                  :disabled="spaceTemplate.system"
                  dense
                  @click="$root.$emit('space-templates-delete', spaceTemplate)">
                  <v-icon
                    :class="!spaceTemplate.system && 'error--text' || 'disabled--text'"
                    size="13">
                    fa-trash
                  </v-icon>
                  <v-list-item-title class="ps-2">
                    <span :class="!spaceTemplate.system && 'error--text' || 'disabled--text'">{{ $t('spaceTemplate.label.delete') }}</span>
                  </v-list-item-title>
                </v-list-item>
              </div>
            </template>
            <span>{{ $t('spaceTemplate.label.system.noDelete') }}</span>
          </v-tooltip>
        </v-list-item-group>
      </v-list>
    </v-hover>
  </component>
</template>
<script>
export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    menu: false,
    hoverMenu: false,
    listItem: null,
    menuId: `spaceTemplateMenu${parseInt(Math.random() * 10000)}`,
  }),
  computed: {
    spaceTemplateId() {
      return this.spaceTemplate?.id;
    },
    name() {
      return this.$te(this.spaceTemplate?.name) ? this.$t(this.spaceTemplate?.name) : this.spaceTemplate?.name;
    },
  },
  watch: {
    listItem() {
      if (this.menu) {
        this.menu = false;
        this.listItem = null;
      }
    },
    menu() {
      if (this.menu) {
        this.$root.$emit('space-management-menu-opened', this.spaceTemplateId);
      } else {
        this.$root.$emit('space-management-menu-closed', this.spaceTemplateId);
      }
    },
    hoverMenu() {
      if (!this.hoverMenu) {
        window.setTimeout(() => {
          if (!this.hoverMenu) {
            this.menu = false;
          }
        }, 200);
      }
    },
  },
  created() {
    this.$root.$on('space-management-menu-opened', this.checkMenuStatus);
    document.addEventListener('click', this.closeMenuOnClick);
  },
  beforeDestroy() {
    this.$root.$off('space-management-menu-opened', this.checkMenuStatus);
    document.removeEventListener('click', this.closeMenuOnClick);
  },
  methods: {
    closeMenuOnClick(e) {
      if (e.target && !e.target.closest(`.${this.menuId}`)) {
        this.menu = false;
      }
    },
    async duplicate() {
      const nameTranslations = await this.$translationService.getTranslations('spaceTemplate', this.spaceTemplate.id, 'name');
      const descriptionTranslations = await this.$translationService.getTranslations('spaceTemplate', this.spaceTemplate.id, 'description');
      const translationConfiguration = await this.$translationService.getTranslationConfiguration();

      const bannerBlob = !this.spaceTemplate.bannerFileId ? null : await fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/attachments/spaceTemplateBanner/${this.spaceTemplate.id}/${this.spaceTemplate.bannerFileId}`, {
        credentials: 'include',
        method: 'GET',
      }).then(resp => resp?.ok && resp.blob());
      const bannerData = bannerBlob && await this.$utils.blobToBase64(bannerBlob);
      const bannerUploadId = bannerBlob && await this.$uploadService.upload(bannerBlob);

      this.$root.$emit('space-templates-name-open', {
        ...this.spaceTemplate,
        id: null,
        bannerFileId: null,
      }, nameTranslations?.[translationConfiguration?.defaultLanguage],
      nameTranslations,
      descriptionTranslations?.[translationConfiguration?.defaultLanguage],
      descriptionTranslations,
      true,
      bannerUploadId,
      bannerData);
    },
    checkMenuStatus(templateId) {
      if (this.menu && templateId !== this.spaceTemplate.id) {
        this.menu = false;
      }
    },
  },
};
</script>