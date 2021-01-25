
<template>
  <v-btn
    v-exo-tooltip.bottom.body="$t('item.dlp.quarantine.open')"
    :href="dlpItemUrl"
    target="_blank"
    icon
    text>
    <i class="uiIconWatch"></i>
  </v-btn>
</template>

<script>
import * as dlpAdministrationServices from '../dlpAdministrationServices';
export default {
  props: {
    item: {
      type: Object,
      default: () => ({}),
    },
  },
  data () {
    return { itemUrl : ''};
  },
  computed: {
    dlpItemUrl() {
      return `${this.itemUrl}`;
    }
  },
  created() {
    dlpAdministrationServices.getDlpPositiveItemUrl(this.item.reference).then(data => {
      this.itemUrl = data.dlpPositiveItemUrl;
    });
  },
};
</script>
