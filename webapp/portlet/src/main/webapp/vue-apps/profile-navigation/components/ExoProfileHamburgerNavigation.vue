<template>
  <v-flex id="ProfileHamburgerNavigation">
    <v-row class="accountTitleWrapper mx-0">
      <v-list-item
        :href="PROFILE_URI"
        class="accountTitleItem py-3">
        <v-list-item-avatar size="44" class="me-3 mt-0 mb-0 elevation-1">
          <v-img :src="avatar" eager />
        </v-list-item-avatar>
        <v-list-item-content class="py-0 accountTitleLabel">
          <v-list-item-title class="font-weight-bold body-2 mb-0">{{ fullName }} <span v-if="external" class="externalFlagClass">{{ $t('menu.profile.external') }}</span></v-list-item-title>
          <v-list-item-subtitle class="font-italic caption">{{ position }}</v-list-item-subtitle>
        </v-list-item-content>
      </v-list-item>
    </v-row>
  </v-flex>
</template>

<script>
export default {
  data() {
    return {
      PROFILE_URI: `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile`,
      IDENTITY_REST_API_URI: `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/identities/${eXo.env.portal.userIdentityId}`,
      profile: null,
    };
  },
  computed: {
    avatar() {
      return this.profile && this.profile.avatar || '';
    },
    fullName() {
      return this.profile && this.profile.fullname || '';
    },
    position() {
      return this.profile && this.profile.position || '';
    },
    external() {
      return this.profile && this.profile.dataEntity && this.profile.dataEntity.external === 'true' ;
    },
  },
  created() {
    document.addEventListener('userModified', event => {
      if (event?.detail) {
        this.profile = Object.assign({}, this.profile, event.detail);
        this.$nextTick().then(() => this.$root.$emit('application-loaded'));
      } else {
        this.getUserInformation();
      }
    });
    this.retrieveUserInformation();
  },
  methods: {
    retrieveUserInformation() {
      this.profile = this.$currentUserIdentity && this.$currentUserIdentity.profile;
      if (!this.profile) {
        return this.getUserInformation();
      }
      this.$root.$applicationLoaded();
    },
    getUserInformation() {
      return this.$identityService.getIdentityById(eXo.env.portal.userIdentityId)
        .then(data => this.profile = data && data.profile)
        .finally(() => this.$root.$applicationLoaded());
    },
  },
};
</script>
