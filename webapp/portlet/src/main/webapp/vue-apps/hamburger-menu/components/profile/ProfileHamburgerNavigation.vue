<template>
  <v-flex id="ProfileHamburgerNavigation">
    <v-list-item
      :href="PROFILE_URI"
      class="accountTitleItem top-bar-height">
      <v-list-item-avatar size="44" class="me-3 mt-0 mb-0 elevation-1">
        <v-img :src="avatar" eager />
      </v-list-item-avatar>
      <v-list-item-content class="py-0 accountTitleLabel">
        <v-list-item-title class="font-weight-bold body-2 mb-0">{{ fullName }} <span v-if="external" class="externalFlagClass">{{ $t('menu.profile.external') }}</span></v-list-item-title>
        <v-list-item-subtitle class="font-italic caption">{{ position }}</v-list-item-subtitle>
      </v-list-item-content>
      <v-list-item-action v-if="stickyAllowed" class="my-auto">
        <v-btn
          :title="value && $t('menu.collapse') || $t('menu.expand')"
          icon
          @click="changeMenuStickiness">
          <v-icon>{{ value && 'fa-angle-double-left' || 'fa-angle-double-right' }}</v-icon>
        </v-btn>
      </v-list-item-action>
    </v-list-item>
    <v-divider />
  </v-flex>
</template>

<script>
export default {
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    stickyAllowed: {
      type: Boolean,
      default: false,
    },
  },
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
      if (event && event.detail) {
        Object.assign(this.profile, event.detail);
      }
    });
    this.retrieveUserInformation();
  },
  methods: {
    retrieveUserInformation() {
      this.profile = this.$currentUserIdentity && this.$currentUserIdentity.profile;
      if (!this.profile) {
        return this.$identityService.getIdentityById(eXo.env.portal.userIdentityId)
          .then(data => this.profile = data && data.profile);
      }
    },
    changeMenuStickiness(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }
      this.$settingService.setSettingValue('USER', eXo.env.portal.userName, 'APPLICATION', 'HamburgerMenu', 'Sticky', String(!this.value))
        .then(() => this.$emit('input', !this.value));
    },
  },
};
</script>
