<template>
  <v-app class="hiddenable-widget">
    <div v-if="display" class="onlinePortlet">
      <div id="onlineContent" class="white">
        <v-card-title class="title center">
          {{ $t('header.label') }}
        </v-card-title>
        <ul id="onlineList" class="gallery uiContentBox">
          <li
            v-for="user in users"
            :key="user.id"
            :id="user.id">
            <a :href="user.href" class="avatarXSmall">
              <v-avatar size="37" class="mx-1">
                <img
                  :src="user.avatar"
                  class="ma-auto object-fit-cover"
                  loading="lazy">
              </v-avatar>
            </a>
          </li>
        </ul>
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
        this.initPopup();
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
          this.initPopup();
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
    initPopup() {
      const restUrl = `//${this.$spacesConstants.HOST_NAME}${this.$spacesConstants.PORTAL}/${this.$spacesConstants.PORTAL_REST}/social/people/getPeopleInfo/{0}.json`;
      if (!this.labels) {
        this.labels = {
          youHaveSentAnInvitation: this.$t('message.label'),
        };
      }
      $('#onlineList').find('a').each(function (idx, el) {
        $(el).userPopup({
          restURL: restUrl,
          labels: this.labels,
          content: false,
          defaultPosition: 'left',
          keepAlive: true,
          maxWidth: '240px'
        });
      });
    }
  }
};
</script>
