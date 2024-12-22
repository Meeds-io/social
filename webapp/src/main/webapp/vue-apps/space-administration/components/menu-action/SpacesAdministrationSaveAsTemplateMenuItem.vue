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
  <v-list-item
    dense
    @click="saveAsTemplate">
    <v-card
      class="d-flex full-height justify-center"
      color="transparent"
      min-width="20"
      flat>
      <v-icon size="16">fa-columns</v-icon>
    </v-card>
    <v-list-item-title class="ps-2">
      {{ $t('social.spaces.administration.manageSpaces.saveAsTemplate') }}
    </v-list-item-title>
  </v-list-item>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
  methods: {
    saveAsTemplate() {
      this.$emit('loading', true);
      window.require(['PORTLET/social/SpaceTemplateManagement'], () => {
        window.setTimeout(async () => {
          const bannerBlob = await fetch(this.space.bannerUrl, {
            credentials: 'include',
            method: 'GET',
          }).then(resp => resp?.ok && resp.blob());
          const bannerData = bannerBlob && await this.$utils.blobToBase64(bannerBlob);
          const bannerUploadId = bannerBlob && await this.$uploadService.upload(bannerBlob);

          const translationConfiguration = await this.$translationService.getTranslationConfiguration();
          const nameTranslations = {};
          const descriptionTranslations = {};
          nameTranslations[translationConfiguration?.defaultLanguage] = this.space.displayName;
          descriptionTranslations[translationConfiguration?.defaultLanguage] = this.space.description;
          const spaceTemplate = this.space.templateId && this.$root.spaceTemplates.find(t => t.id === this.space.templateId);
          const permissions = await this.$spaceAdministrationService.getSpacePermission(this.space.id);

          this.$root.$emit('space-templates-name-open',
            {
              enabled: true,
              icon: spaceTemplate?.icon || 'fa-people-arrows',
              spaceFields: spaceTemplate?.spaceFields || ['name', 'invitation', 'properties', 'access'],
              permissions: spaceTemplate?.permissions,
              layout: `group::${this.space.groupId}`,
              spaceDefaultVisibility: this.space.visibility?.toUpperCase?.(),
              spaceDefaultRegistration: this.space.subscription?.toUpperCase?.(),
              spaceAllowContentCreation: !!this.space.redactorsCount,
              spaceLayoutPermissions: permissions?.layoutPermissions?.map?.(p => (p === `manager:${this.space.groupId}` ? 'spaceAdmin' : p)),
              spacePublicSitePermissions: permissions?.publicSitePermissions?.map?.(p => (p === `manager:${this.space.groupId}` ? 'spaceAdmin' : p)),
              spaceDeletePermissions: permissions?.deletePermissions?.map?.(p => (p === `manager:${this.space.groupId}` ? 'spaceAdmin' : p)),
            },
            this.space.displayName,
            nameTranslations,
            this.space.description,
            descriptionTranslations,
            true,
            bannerUploadId,
            bannerData);
          this.$emit('loading', false);
        }, 200);
      });
    },
  },
};
</script>