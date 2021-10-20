<template>
  <v-app>
    <v-menu
      rounded="rounded"
      elevation="2"
      v-model="menu"
      open-on-hover
      :close-on-content-click="false"
      :nudge-width="200"
      max-width="350"
      min-width="300"
      offset-y>
      <template v-slot:activator="{ on, attrs }">
        <div class="d-inline-flex">
          <a
            v-if="logoPath"
            id="UserHomePortalLink"
            :href="portalPath"
            class="pe-3 logoContainer">
            <img
              v-bind="attrs"
              v-on="on"
              :src="logoPath"
              class="spaceAvatar"
              :alt="logoTitle">
          </a>
          <a
            :href="portalPath"
            :title="logoTitle"
            :class="'ps-2 align-self-center brandingContainer space'">
            <div class="logoTitle subtitle-2 font-weight-bold text-truncate">
              {{ logoTitle }}
            </div>
          </a>
        </div>
      </template>
      <v-card elevation="2">
        <v-list class="pa-0">
          <v-list-item>
            <v-list-item-avatar
              width="60"
              height="60">
              <v-img
                class="object-fit-cover"
                :src="logoPath" />
            </v-list-item-avatar>
            <v-list-item-content class="pb-0">
              <v-list-item-title>
                <span class="blue--text text--darken-3 font-weight-bold">
                  {{ logoTitle }}
                </span>
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ membersNumber }} {{ $t('space.logo.banner.popover.members') }}
              </v-list-item-subtitle>
              <p class="text-truncate-3 text-caption text--primary font-weight-medium">
                {{ spaceDescription }}
              </p>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-divider />
        <v-list class="pa-0 mt-0 mb-0">
          <v-list-item class="pt-0 pb-0">
            <v-list-item-content>
              <v-container class="pa-0">
                <v-row no-gutters>
                  <v-col
                    cols="4"
                    class="pt-1 body-2 grey--text text--darken-1"
                    justify="center">
                    {{ $t('space.logo.banner.popover.managers') }}
                  </v-col>
                  <v-col
                    cols="8"
                    justify="center"
                    class="d-flex flex-nowrap pa-0">
                    <exo-user-avatar
                      v-for="manager in mangersToDisplay"
                      :key="manager.id"
                      :username="manager.userName"
                      :title="manager.fullName"
                      :avatar-url="manager.avatar"
                      :size="30"
                      :retrieve-extra-information="false"
                      class="me-1 pa-0 mt-0 mb-0" />
                    <v-avatar
                      v-if="!KeepDisplayManagers"
                      :size="30"
                      class="ma-0">
                      <img
                        :src="lastManagerToDisplay.avatar"
                        :title="lastManagerToDisplay.fullName"
                        class="object-fit-cover"
                        loading="lazy"
                        :alt="lastManagerToDisplay.fullName">
                      <span
                        id="showMoreManagers"
                        class="font-weight-bold white--text">
                        <p class="mt-2">+{{ showMoreManagers }}</p>
                      </span>
                    </v-avatar>
                  </v-col>
                </v-row>
              </v-container>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-divider />
        <v-list
          class="pa-0 mt-0 mb-0">
          <v-list-item
            class="pt-0 pb-0">
            <v-list-item-content>
              <v-list-item-title>
                <v-btn
                  :href="homePath"
                  color="primary"
                  text
                  class="pa-0 pe-2">
                  <v-icon
                    dense
                    right
                    class="me-1">
                    mdi-home
                  </v-icon>
                  <span class="text-caption pt-1">{{ $t('space.logo.banner.popover.home') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action>
              <div class="pa-0 ma-0">
                <v-btn
                  class="d-none"
                  color="primary"
                  icon>
                  <v-icon dense>mdi-send</v-icon>
                </v-btn>
                <v-btn
                  color="primary"
                  @click="openChatDrawer"
                  icon>
                  <v-icon dense>mdi-chat-processing</v-icon>
                </v-btn>
                <space-popover-action-component />
              </div>
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </v-card>
    </v-menu>
  </v-app>
</template>

<script>
export default {
  props: {
    logoPath: {
      type: String,
      default: function () {
        return null;
      },
    },
    portalPath: {
      type: String,
      default: function () {
        return null;
      },
    },
    logoTitle: {
      type: String,
      default: function () {
        return null;
      },
    },
    membersNumber: {
      type: Number,
      default: function () {
        return null;
      },
    },
    spaceDescription: {
      type: String,
      default: function () {
        return '';
      },
    },
    homePath: {
      type: String,
      default: function () {
        return '';
      },
    },
    managers: {
      type: Array,
      default: function () {
        return null;
      },
    },
    sizeToDisplay: {
      type: Number,
      default: function () {
        return 4;
      },
    },
  },
  computed: {
    KeepDisplayManagers() {
      return this.managers && this.managers.length <= this.sizeToDisplay;
    },
    mangersToDisplay() {
      return this.managers && this.managers.slice(0, this.sizeToDisplay);
    },
    lastManagerToDisplay() {
      return this.managers && this.managers.length > this.sizeToDisplay && this.managers[this.sizeToDisplay];
    },
    showMoreManagers() {
      return this.managers && this.managers.length - this.sizeToDisplay;
    }
  },
  methods: {
    openChatDrawer() {
      document.dispatchEvent(new CustomEvent('exo-chat-room-open-requested', {detail: {name: `${eXo.env.portal.spaceGroup}`, type: 'space-name'}}));
    },
  },
};
</script>