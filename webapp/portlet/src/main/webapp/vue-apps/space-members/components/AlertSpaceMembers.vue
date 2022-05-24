<template>
  <v-alert
    v-model="displayAlert"
    :type="alertType"
    dismissible
    :icon="alertType === 'warning' ? 'mdi-alert-circle' : ''">
    <span v-sanitized-html="alertMessage" class="mt-8"> </span>
  </v-alert>
</template>

<script>
export default {
  props: {
    spaceDisplayName: {
      type: String,
      default: null,
    },
  },
  data: () => ({
    displayAlert: false,
    alertMessage: null,
    alertType: null,
  }),
  created() {
    const params = new URL(location.href).searchParams;
    const feedbackMessage = params.get('feedbackMessage');
    if (feedbackMessage && feedbackMessage === 'SpaceRequestAlreadyMember') {
      const userName = params.get('userName');
      this.$identityService.getIdentityByProviderIdAndRemoteId('organization', userName)
        .then(identity => {
          const userFullName = identity.profile.fullname;
          this.alertType = 'warning';
          this.alertMessage = this.$t(`Notification.feedback.message.${feedbackMessage}`, {
            0: userFullName,
            1: this.spaceDisplayName,
          });
          this.displayAlert = true;
        });
    }   
  },
};
</script>