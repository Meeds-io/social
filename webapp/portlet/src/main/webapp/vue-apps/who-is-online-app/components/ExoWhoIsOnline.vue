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
    };
  },
  computed: {
    display() {
      return this.users && this.users.length;
    },
  },
  created() {
    this.initOnlineUsers(this.$root.onlineUsers && this.$root.onlineUsers.users || []);
    setInterval(function () {
      this.retrieveOnlineUsers();
    }.bind(this), this.delay);
  },
  methods: {
    retrieveOnlineUsers() {
      return whoIsOnlineServices.getOnlineUsers(eXo.env.portal.spaceId)
        .then(data => this.initOnlineUsers(data && data.users || []));
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
      return this.$nextTick()
        .then(() => {
          this.initPopup();
          this.$root.$applicationLoaded();
        });
    },
    initPopup() {
      const restUrl = `//${this.$spacesConstants.HOST_NAME}${this.$spacesConstants.PORTAL}/${this.$spacesConstants.PORTAL_REST}/social/people/getPeopleInfo/{0}.json`;
      if (!this.labels) {
        this.labels = {
          youHaveSentAnInvitation: this.$t('message.label'),
          StatusTitle: this.$t('Loading.label'),
          Connect: this.$t('Connect.label'),
          Confirm: this.$t('Confirm.label'),
          CancelRequest: this.$t('CancelRequest.label'),
          RemoveConnection: this.$t('RemoveConnection.label'),
          Ignore: this.$t('Ignore.label'),
          Disabled: this.$t('Disabled.label')
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