<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
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
  <div class="spaceTemplates">
    <table class="uiGrid table table-hover table-striped">
      <tr>
        <th width="14%">
          {{ $t('social.spaces.templates.template') }}
        </th>
        <th width="24%">
          {{ $t('social.spaces.templates.description') }}
        </th>
        <th width="5%">
          {{ $t('social.spaces.templates.hidden') }}
        </th>
        <th width="10%">
          {{ $t('social.spaces.templates.registration') }}
        </th>
        <th width="25%">
          {{ $t('social.spaces.templates.applications') }}
        </th>
        <th width="20%">
          {{ $t('social.spaces.templates.permissions') }}
        </th>
        <th width="2%">
          {{ $t('social.spaces.templates.banner') }}
        </th>
      </tr>
      <tr v-if="templates.length === 0">
        <td class="empty center" colspan="12"> {{ $t('social.spaces.templates.noTemplates') }} </td>
      </tr>
      <exo-space-template
        v-for="template in templates"
        v-else
        :key="template.name"
        :template="template"
        @display-banner="displayBanner" />
    </table>
    <v-dialog v-model="preview">
      <v-img :src="bannerImage"></v-img>
    </v-dialog>
  </div>
</template>
<script>
import * as spaceTemplatesServices from '../spaceTemplatesServices';
import ExoSpaceTemplate from './ExoSpaceTemplate.vue';

export default {
  components: {
    'exo-space-template': ExoSpaceTemplate
  },
  data() {
    return {
      templates: [],
      bannerImage: '',
      preview: false,
    };
  },
  created() {
    this.initTemplates();
  },
  methods: {
    initTemplates() {
      spaceTemplatesServices.getTemplates().then(data => {
        this.templates = data;
      });
    },
    displayBanner(bannerImage) {
      this.bannerImage = bannerImage;
      this.preview = true;
    },
  }
};
</script>