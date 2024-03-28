<template>
  <v-app>
    <v-menu
      v-model="menu"
      rounded="rounded"
      elevation="2"
      open-on-hover
      transition="slide-x-transition"
      :close-on-content-click="false"
      :nudge-width="200"
      max-width="350"
      min-width="300"
      content-class="no-box-shadow full-height pa-1 mt-6 "
      offset-y>
      <template #activator="{ on, attrs }">
        <div
          v-on="on"
          v-bind="attrs"
          class="d-inline-flex">
          <a :href="portalPath" :aria-label="$t('space.avatar.href.title',{0:logoTitle})">
            <v-list-item-avatar 
              v-if="logoPath"
              id="UserHomePortalLink"
              size="30"
              class="tile my-0 spaceAvatar ms-0 me-3"
              tile>
              <v-img :src="logoPath" :alt="$t('space.avatar.img.alt',{0:logoTitle})" />
            </v-list-item-avatar>
          </a>
          <a
            :href="portalPath"
            :class="'ps-2 align-self-center brandingContainer space'">
            <div class="logoTitle subtitle-2 font-weight-bold text-truncate">
              {{ logoTitle }}
            </div>
          </a>
        </div>
      </template>
      <v-card v-if="menu" elevation="2">
        <v-list class="pa-0">
          <v-list-item class="pt-3">
            <v-list-item-avatar
              class="spaceAvatar mt-0 align-self-start"
              width="60"
              height="60">
              <v-img
                :alt="$t('space.avatar.img.alt',{0:logoTitle})"
                class="object-fit-cover"
                :src="`${logoPath}&size=60x60`" />
            </v-list-item-avatar>
            <v-list-item-content class="pb-0 pt-0">
              <v-list-item-title>
                <v-tooltip bottom>
                  <template #activator="{ on, attrs }">
                    <span
                      v-on="on"
                      v-bind="attrs"
                      class="primary--text text--darken-3 font-weight-bold">
                      {{ logoTitle }}
                    </span>
                  </template>
                  <span>{{ logoTitle }}</span>
                </v-tooltip>
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ membersNumber }} {{ $t('space.logo.banner.popover.members') }}
              </v-list-item-subtitle>
              <p class="text-truncate-2 text-caption text--primary font-weight-medium">
                {{ spaceDescription }}
              </p>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="pa-0 mt-0 mb-0">
          <v-list-item class="pt-0 pb-0">
            <v-list-item-content>
              <v-container class="pa-0">
                <v-row no-gutters class="align-center">
                  <v-col
                    cols="6"
                    class="body-2 grey--text text-truncate text--darken-1 text-left">
                    {{ $t('space.logo.banner.popover.managers') }}
                  </v-col>
                  <v-col
                    cols="6"
                    class="d-flex flex-nowrap justify-end pa-0">
                    <exo-user-avatars-list
                      :users="mangersToDisplay"
                      :icon-size="30"
                      :popover="false"
                      :margin-left="mangersToDisplay.length > 1 && 'ml-n4' || ''"
                      :compact="mangersToDisplay.length > 1"
                      clickable="'false'"
                      max="3"
                      retrieve-extra-information
                      avatar-overlay-position
                      @open-detail="openDetails()" />
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
            <v-list-item-content class="py-1">
              <v-list-item-title>
                <v-btn
                  :href="homePath"
                  color="primary"
                  text
                  class="pa-0 pe-2"
                  @click="popoverActionEvent('backToHome')">
                  <v-icon
                    dense
                    right
                    class="me-1 ms-0">
                    mdi-home
                  </v-icon>
                  <span class="text-caption pt-1">{{ $t('space.logo.banner.popover.home') }}</span>
                </v-btn>
              </v-list-item-title>
            </v-list-item-content>
            <v-list-item-action class="space-logo-popover flex-row">
              <space-mute-notification-button
                :space-id="spaceId"
                :muted="muted"
                origin="spaceTopbarpopoverAction" />
              <exo-space-favorite-action
                :is-favorite="isFavorite"
                :space-id="spaceId"
                entity-type="SPACE_TOP_BAR_TIPTIP"
                @added="$root.isFavorite = 'true'"
                @removed="$root.isFavorite = 'false'" />
              <extension-registry-components
                :params="params"
                name="SpacePopover"
                type="space-popover-action"
                parent-element="div"
                element="div"
                element-class="mx-auto ma-lg-0" />
              <space-popover-action-component />
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </v-card>
    </v-menu>
    <space-hosts-drawer />
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
    spaceId: {
      type: String,
      default: ''
    },
    muted: {
      type: Boolean,
      default: false
    },
    isMember: {
      type: Boolean,
      default: false
    },
  },
  data: () => ({
    menu: false,
  }),
  computed: {
    mangersToDisplay() {
      return this.managers;
    },
    isFavorite() {
      return this.$root.isFavorite;
    },
    params() {
      return {
        identityType: 'space',
        identityId: eXo.env.portal.spaceId
      };
    },
  },
  created() {
    document.addEventListener('metadata.favorite.updated', this.favoriteUpdated);
  },
  destroyed() {
    document.removeEventListener('metadata.favorite.updated', this.favoriteUpdated);
  },
  methods: {
    popoverActionEvent(clickedItem) {
      document.dispatchEvent(new CustomEvent('space-topbar-popover-action', {detail: clickedItem} ));
    },
    openDetails() {
      this.$root.$emit('displaySpaceHosts', this.mangersToDisplay);
      this.popoverActionEvent('displaySpaceHosts');
    },
    favoriteUpdated(event) {
      const metadata = event && event.detail;
      if (metadata && metadata.objectType === 'space'
          && metadata.objectId === this.spaceId
          && metadata.favorite !== this.isFavorite) {
        this.$root.isFavorite = `${metadata.favorite}`;
      }
    },
  }
};
</script>
