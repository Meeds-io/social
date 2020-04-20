<template>
  <v-app
    :class="owner && 'profileWorkExperience' || 'profileWorkExperienceOther'"
    class="white">
    
  </v-app>
</template>

<script>
import * as userService from '../../common/js/UserService.js'; 

export default {
  data: () => ({
    user: null,
    owner: eXo.env.portal.profileOwner === eXo.env.portal.userName,
    error: null,
    saving: null,
  }),
  mounted() {
    document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
  },
  methods: {
    refresh() {
      return userService.getUser(eXo.env.portal.profileOwner, 'all')
        .then(user => {
          this.user = user;
          return this.$nextTick();
        })
        .then(() => this.skeleton = false)
        .catch((e) => {
          console.warn('Error while retrieving user details', e); // eslint-disable-line no-console
        })
        .finally(() => {
          document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
        });
    },
  },
};
</script>