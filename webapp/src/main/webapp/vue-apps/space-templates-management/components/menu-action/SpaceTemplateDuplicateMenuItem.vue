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
    @click="duplicate">
    <v-icon size="13">
      fa-copy
    </v-icon>
    <v-list-item-title class="ps-2">
      {{ $t('spaceTemplate.label.duplicate') }}
    </v-list-item-title>
  </v-list-item>
</template>
<script>
export default {
  props: {
    spaceTemplate: {
      type: Object,
      default: null,
    },
  },
  methods: {
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
        system: false,
      }, nameTranslations?.[translationConfiguration?.defaultLanguage],
      nameTranslations,
      descriptionTranslations?.[translationConfiguration?.defaultLanguage],
      descriptionTranslations,
      true,
      bannerUploadId,
      bannerData);
    },
  },
};
</script>