<template>
  <v-hover v-slot="{ hover }">
    <v-card
      @click="openUserProfile()"
      class="border-box-sizing fill-height"
      :class="{
        'full-width': isMobile
      }"
      width="350"
      :height="80"
      :min-height="80"
      :max-height="80"
      :elevation="hover && 3 || 0"
      outlined
      flat
      rounded>
      <people-card
        class="d-flex fill-height"
        :user="result"
        :profile-action-extensions="profileActionExtensions"
        :mobile-display="isMobile"
        :attach-menu="false"
        display-compact-menu-button
        compact-display />
    </v-card>
  </v-hover>
</template>

<script>
export default {
  props: {
    term: {
      type: String,
      default: null,
    },
    result: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    profileActionExtensions: [],
  }),
  computed: {
    isMobile() {
      return this.$vuetify.breakpoint.mobile;
    },
  },
  created() {
    this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
  },
  methods: {
    openUserProfile() {
      if (this.result?.username) {
        window.location.href = `${eXo.env.portal.context}/${eXo.env.portal.metaPortalName}/profile/${this.result.username}`;
      }
    }
  }
};
</script>
