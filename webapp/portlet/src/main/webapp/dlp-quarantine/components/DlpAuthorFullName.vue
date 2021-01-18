<template>
  <a
    :href="dlpItemOwnerLink"
    class="text-decoration-underline"
    target="_blank">
    {{ authorFullName }}
  </a>
</template>

<script>
export default {
  props: {
    username: {
      type: String,
      default: function () {
        return null;
      },
    },
  },
  data () {
    return { authorFullName : ''};
  },
  computed: {
    dlpItemOwnerLink() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
    }
  },
  created() {
    this.$userService.getUser(this.username)
      .then(user => {
        this.authorFullName = user.fullname;
      });
  },
};
</script>