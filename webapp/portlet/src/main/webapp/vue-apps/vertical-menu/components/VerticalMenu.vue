<template>
  <v-app>
    <v-card class="VerticalMenu elevation-0">
      <site-details
        v-if="site"
        display-sequentially
        :site="site"
        :extra-class="' px-0 py-0 '" />
    </v-card>
    <drawers-overlay />
  </v-app>
</template>
<script>
export default {
  data: () => ({
    site: null,
    siteId: eXo.env.portal.siteId,
  }),
  created() {
    this.retrieveCurrentSite();
  },
  methods: {
    retrieveCurrentSite(){
      return this.$siteService.getSiteById(this.siteId, true, true, eXo.env.portal.language, ['displayed', 'temporal'], true, true)
        .then(site => {
          this.site = site;
        });
    },
  }
};
</script>
