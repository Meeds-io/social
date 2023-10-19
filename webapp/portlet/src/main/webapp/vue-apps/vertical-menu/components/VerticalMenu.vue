<template>
  <v-app>
    <v-menu
      :value="true"
      :absolute="false"
      :close-on-click="false"
      :close-on-content-click="false"
      :min-width="'350px'"
      :max-width="'350px'"
      content-class="white VerticalMenu elevation-0 z-index-drawer"
      attach="#ParentSiteStickyMenu"
      eager
      tile>
      <site-details
        display-sequentially
        :site="site"
        :extra-class="' px-0 py-0 '" />
    </v-menu>
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
      return this.$VerticalMenuService.getSiteById(this.siteId, true, true, eXo.env.portal.language)
        .then(site => {
          this.site = site;
        });
    },
  }
};
</script>
