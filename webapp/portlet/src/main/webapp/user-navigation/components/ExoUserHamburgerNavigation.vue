<template>
  <v-container
    id="UserHamburgerNavigation"
    px-0
    pb-4
    pt-0
    class="white">
    <v-row class="mx-0 settingsTitle">
      <v-list-item :href="settingsUrl" class="settingsTitleWrapper">
        <v-list-item-icon class="my-auto mr-6"><i class="uiIcon uiIconToolbarNavItem settingsIcon"></i></v-list-item-icon>
        <v-list-item-content class="subtitle-2 settingsTitleLabel">{{ this.$t('menu.settings.title') }}</v-list-item-content>
      </v-list-item>
    </v-row>
    <v-row class="mx-0 logoutLinks">
      <v-list-item @click="logOut()">
        <v-list-item-icon class="my-auto mr-6"><i class="uiIcon uiIconToolbarNavItem logoutIcon"></i></v-list-item-icon>
        <v-list-item-content class="subtitle-2 settingsTitleLabel">{{ this.$t('menu.logout.label') }}</v-list-item-content>
      </v-list-item>
    </v-row>
  </v-container>
</template>
<script>
export default {
  data: () => ({
    settingsUrl: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/settings`,
  }),
  methods: {
    logOut() {
      eXo.portal.logout();
    },
    changeLanguage() {
      if (document.getElementById('UIMaskWorkspace')) {
        window.ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'ChangeLanguage', true));}
    },
    accountSettings() {
      if (document.getElementById('UIMaskWorkspace')) {
        window.ajaxGet(eXo.env.server.createPortalURL('UIPortal', 'AccountSettings', true));
      }
    },
  }
};
</script>

