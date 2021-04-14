<template>
  <v-app class="hiddenable-widget">
    <div :class="appVisibilityClass" class="onlinePortlet">
      <div id="onlineContent" class="white">
        <v-card-title class="title center">
          {{ $t('header.label') }}
        </v-card-title>
        <ul id="onlineList" class="gallery uiContentBox">
          <li
            v-for="user in users"
            :key="user"
            :id="user.id">
            <a :href="user.href" class="avatarXSmall">
              <v-avatar size="37" class="mx-1">
                <v-img
                  :lazy-src="user.avatar"
                  :src="user.avatar"
                  transition="eager"
                  eager />
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
import { spacesConstants } from '../../js/spacesConstants.js';

export default {
  data() {
    return {
      users: [],
    };
  },
  computed: {
    appVisibilityClass() {
      if (!this.users || !this.users.length) {
        return 'd-none hidden';
      }
      return '';
    },
  },
  created() {
    this.initOnlineUsers();
    // And we should use setInterval with 60 seconds
    const delay = 60000;
    setInterval(function () {
      this.initOnlineUsers();
    }.bind(this), delay);
  },
  updated() {
    this.initPopup();
  },
  methods: {
    initOnlineUsers() {
      whoIsOnlineServices.getOnlineUsers(eXo.env.portal.spaceId)
        .then(response => {
          if (response) {
            const users = response.users || [];
            for (const user of users) {
              user.href = `${spacesConstants.PORTAL}/${spacesConstants.PORTAL_NAME}/profile/${user.username}`;
              if (!user.avatar) {
                user.avatar = `${spacesConstants.SOCIAL_USER_API}/${user.username}/avatar`;
              }
            }
            this.users = users;
          } else {
            this.users = [];
          }
          window.setTimeout(() => this.$root.$emit('application-loaded'), 200);
        });
    },
    initPopup() {
      const restUrl = `//${spacesConstants.HOST_NAME}${spacesConstants.PORTAL}/${spacesConstants.PORTAL_REST}/social/people/getPeopleInfo/{0}.json`;
      const labels = {
        youHaveSentAnInvitation: this.$t('message.label'),
        StatusTitle: this.$t('Loading.label'),
        Connect: this.$t('Connect.label'),
        Confirm: this.$t('Confirm.label'),
        CancelRequest: this.$t('CancelRequest.label'),
        RemoveConnection: this.$t('RemoveConnection.label'),
        Ignore: this.$t('Ignore.label'),
        Disabled: this.$t('Disabled.label')
      };
      $('#onlineList').find('a').each(function (idx, el) {
        $(el).userPopup({
          restURL: restUrl,
          labels: labels,
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