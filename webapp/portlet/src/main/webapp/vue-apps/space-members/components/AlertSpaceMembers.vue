<script>
export default {
  props: {
    spaceDisplayName: {
      type: String,
      default: null,
    },
  },
  created() {
    const params = new URL(location.href).searchParams;
    const feedbackMessage = params.get('feedbackMessage');
    if (feedbackMessage && feedbackMessage === 'SpaceRequestAlreadyMember') {
      const userName = params.get('userName');
      this.$identityService.getIdentityByProviderIdAndRemoteId('organization', userName)
        .then(identity => {
          const userFullName = identity.profile.fullname;
          this.$root.$emit('alert-message-html', this.$t(`Notification.feedback.message.${feedbackMessage}`, {
            0: userFullName,
            1: this.spaceDisplayName,
          }), 'warning');
        });
    }   
  },
};
</script>