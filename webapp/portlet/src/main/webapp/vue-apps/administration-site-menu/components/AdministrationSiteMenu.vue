<template>
  <v-app>
    <v-menu
      :value="true"
      :absolute="false"
      :close-on-click="false"
      :close-on-content-click="false"
      :min-width="'350px'"
      :max-width="'350px'"
      content-class="white AdministrationSiteMenu elevation-0 z-index-drawer"
      attach="#ParentSiteStickyMenu"
      eager
      tile>
      <site-details
        display-sequentially
        :site="administrationSite"
        :extra-class="' px-0 py-0 '"
        class="AdministrationSiteClass " />
    </v-menu>
  </v-app>
</template>
<script>
export default {
  data: () => ({
    administrationSite: null,
  }),
  created() {
    this.retrieveAdministrationSite();
  },
  methods: {
    retrieveAdministrationSite(){
      return this.$AdministrationSiteMenuService.getAdministrationSite()
        .then(site => {
          this.administrationSite = site ;
        });
    },
  }
};
</script>
