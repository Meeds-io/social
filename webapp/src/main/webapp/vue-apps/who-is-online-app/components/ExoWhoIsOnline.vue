<template>
  <v-app class="hiddenable-widget">
    <widget-wrapper 
      v-if="display"
      extra-class="onlinePortlet application-body"
      :title="$t('header.label')">
      <div
        id="onlineList"
        class="d-flex align-center justify-center flex-wrap">
        <exo-user-avatar
          v-for="user in users"
          :key="user.id"
          avatar
          extra-class="mx-1 mb-1"
          :identity="user"
          popover
          :popover-left-position="true" 
          :size="34" />
      </div>
    </widget-wrapper>
  </v-app>
</template>

<script>
  import * as whoIsOnlineServices from '../whoIsOnlineServices';

  export default {
    data () {
      return {
        users: null,
        labels: null,
        delay: 120000,
        loaded: false,
      };
    },
    computed: {
      display () {
        return this.users && this.users.length;
      },
    },
    watch: {
      display () {
        this.$root.$updateApplicationVisibility(this.display);
      },
    },
    mounted () {
      this.$root.$applicationLoaded();
    },
    created () {
      if (document.readyState === 'complete') {
        this.init();
      } else {
        document.onreadystatechange = () => {
          if (document.readyState === 'complete' && !this.loaded) {
            this.init();
          }
        };
        window.setTimeout(() => {
          if (!this.loaded) {
            this.init();
          }
        }, 2000);
      }
      if (window.whosOnlineInterval) {
        window.clearInterval(window.whosOnlineInterval);
      }
      window.whosOnlineInterval = window.setInterval(() => this.retrieveOnlineUsers(), this.delay);
    },
    methods: {
      init () {
        this.loaded = true;
        this.initOnlineUsers(this.$root.onlineUsers && this.$root.onlineUsers.users || []);
      },
      retrieveOnlineUsers () {
        return whoIsOnlineServices.getOnlineUsers(eXo.env.portal.spaceId)
          .then(data => {
            this.initOnlineUsers(data && data.users || []);
          });
      },
      initOnlineUsers (users) {
        if (users && users.length) {
          for (const user of users) {
            user.href = `${eXo.$spacesConstants.PORTAL}/${eXo.$spacesConstants.PORTAL_NAME}/profile/${user.username}`;
          }
          this.users = users;
        } else {
          this.users = [];
        }
      },
    },
  };
</script>
