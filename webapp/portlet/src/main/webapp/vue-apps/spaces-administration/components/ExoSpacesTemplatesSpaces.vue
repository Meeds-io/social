<template>
  <div class="spaceTemplates">
    <table class="uiGrid table table-hover table-striped text-break">
      <tr class="text-no-wrap">
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
      <v-img :src="bannerImage" />
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