<template>
  <tr>
    <td>{{ $t(`social.spaces.templates.name.${template.name}`) }}</td>
    <td>{{ $t(`social.spaces.templates.description.${template.name}`) }}</td>
    <td class="center">
      <span v-if="template.visibility === 'hidden'">{{ $t('social.spaces.templates.yes') }}</span>
      <span v-else>{{ $t('social.spaces.templates.no') }}</span>
    </td>
    <td class="center">{{ template.registration }}</td>
    <td>
      <span>{{ applications }}</span>
    </td>
    <td>{{ template.permissionsLabels }}</td>
    <td class="center">
      <a @click="$emit('display-banner', bannerImage)">
        <i class="uiIconWatch"></i>
      </a>
    </td>
  </tr>
</template>

<script>
export default {
  props: {
    template: {
      type: Object,
      default: () => {return {};}
    }
  },
  computed: {
    applications() {
      let applications = `${this.$t(`${this.template.spaceHomeApplication.portletName}.label.name`)}, `;
      const size = this.template.spaceApplicationList.length;
      for (let i=0; i<size-1; i++) {
        applications += `${this.$t(`${this.template.spaceApplicationList[i].portletName}.label.name`)}, `;
      }
      applications += this.$t(`${this.template.spaceApplicationList[size-1].portletName}.label.name`);
      return applications;
    },
    bannerImage() {
      return `${this.$spacesConstants.SPACES_TEMPLATES_API}/${this.template.name}/banner`;
    }
  },
};
</script>
