<template>
  <v-app class="hiddenable-widget">
    <div v-if="display" class="onlinePortlet">
      <div id="onlineContent" class="white">
        <v-card-title class="title center">
          {{ $t('header.label') }}
        </v-card-title>
        <div id="onlineList" class="d-flex align-center justify-center flex-wrap  py-3">
          <exo-user-avatar
            v-for="user in users"
            :key="user.id"
            :identity="user"
            :size="36"
            :popover-left-position="true"
            popover
            avatar 
            class="mx-1" />
        </div>
      </div>
    </div>
  </v-app>
</template>

<script>
import * as whoIsOnlineServices from '../whoIsOnlineServices';

export default {
  data() {
    return {
      users: null,
      labels: null,
      delay: 120000,
      loaded: false,
    };
  },
  computed: {
    display() {
      return this.users && this.users.length;
    },
  },
  mounted() {
    this.$root.$applicationLoaded();
  },
  created() {
    document.onreadystatechange = () => {
      if (document.readyState === 'complete' && !this.loaded) {
        this.initOnlineUsers(this.$root.onlineUsers && this.$root.onlineUsers.users || []);
        setInterval(function () {
          this.retrieveOnlineUsers();
        }.bind(this), this.delay);
        this.loaded=true;
      }
    };
  },
  methods: {
    retrieveOnlineUsers() {
      return whoIsOnlineServices.getOnlineUsers(eXo.env.portal.spaceId)
        .then(data => {
          this.initOnlineUsers(data && data.users || []);
        });
    },
    initOnlineUsers(users) {
      if (users && users.length) {
        for (const user of users) {
          user.href = `${this.$spacesConstants.PORTAL}/${this.$spacesConstants.PORTAL_NAME}/profile/${user.username}`;
        }
        this.users = users;
      } else {
        this.users = [];
      }
    },
  }
};
</script>
