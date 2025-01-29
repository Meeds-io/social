<template>
  <div class="d-flex align-center">
    <v-menu
      v-model="menu"
      :close-on-content-click="false"
      :open-on-hover="!isAnonymous"
      :nudge-width="200"
      rounded="rounded"
      elevation="2"
      transition="slide-x-transition"
      max-width="350"
      min-width="300"
      content-class="no-box-shadow full-height pa-1 mt-6"
      offset-y>
      <template #activator="{ on, attrs }">
        <v-card
          v-on="on"
          v-bind="attrs"
          height="28"
          class="d-inline-flex transparent"
          flat>
          <a
            v-if="$root.displaySiteLogo"
            :href="$root.spacePortalPath"
            :aria-label="$t('space.avatar.href.title',{0: $root.spaceLogoTitle})">
            <v-list-item-avatar
              v-if="$root.spaceLogoPath"
              id="UserHomePortalLink"
              size="28"
              class="mx-0 my-auto spaceAvatar"
              tile>
              <img
                :src="$root.spaceLogoPath"
                :alt="$t('space.avatar.img.alt',{0: $root.spaceLogoTitle})"
                height="28"
                width="auto"
                class="object-fit-contain">
            </v-list-item-avatar>
          </a>
          <a
            v-if="$root.displaySiteTitle"
            :href="$root.spacePortalPath"
            :class="$root.displaySiteLogo && 'ms-4'"
            class="align-self-center brandingContainer space">
            <div class="logoTitle text-body menu-text-color font-weight-bold text-truncate">
              {{ $root.spaceLogoTitle }}
            </div>
          </a>
        </v-card>
      </template>
      <v-card
        v-if="menu"
        :class="isTopBarElement && 'layout-top-bar' || ''"
        elevation="2">
        <v-list class="pa-0 transparent">
          <v-list-item class="pt-3">
            <v-list-item-avatar
              class="spaceAvatar mt-0 align-self-start"
              width="60"
              height="60">
              <v-img
                :alt="$t('space.avatar.img.alt',{0: $root.spaceLogoTitle})"
                :src="`${$root.spaceLogoPath}&size=60x60`"
                class="object-fit-cover" />
            </v-list-item-avatar>
            <v-list-item-content class="pb-0 pt-0">
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <span
                    v-on="on"
                    v-bind="attrs"
                    class="primary--text text--darken-3 font-weight-bold text-truncate-2">
                    {{ $root.spaceLogoTitle }}
                  </span>
                </template>
                <span>{{ $root.spaceLogoTitle }}</span>
              </v-tooltip>
              <v-list-item-subtitle>
                <span class="text-body">{{ $root.membersNumber }} {{ $t('space.logo.banner.popover.members') }}</span>
              </v-list-item-subtitle>
              <p v-sanitized-html="$root.spaceDescription" class="text-truncate-2 text-caption text--primary font-weight-medium"></p>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="pa-0 mt-0 mb-0 transparent">
          <v-list-item class="pt-0 pb-0">
            <v-list-item-content>
              <v-container class="pa-0">
                <v-row no-gutters class="align-center">
                  <v-col
                    cols="6"
                    class="text-truncate text-body text-left">
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
          class="pa-0 mt-0 mb-0 transparent">
          <v-list-item
            class="pt-0 pb-0 justify-end">
            <v-list-item-action class="space-logo-popover flex-row mr-0">
              <space-mute-notification-button
                :space-id="$root.spaceId"
                :muted="$root.muted"
                origin="spaceTopbarpopoverAction" />
              <space-favorite-action
                :is-favorite="isFavorite"
                :space-id="$root.spaceId"
                entity-type="SPACE_TOP_BAR_TIPTIP"
                @added="$root.isFavorite = 'true'"
                @removed="$root.isFavorite = 'false'" />
              <extension-registry-components
                :params="params"
                name="SpacePopover"
                type="space-popover-action"
                parent-element="div"
                element="div"
                element-class="mx-auto ma-lg-0"
                class="d-flex" />
              <space-popover-action-component />
            </v-list-item-action>
          </v-list-item>
        </v-list>
      </v-card>
    </v-menu>
    <space-hosts-drawer v-if="!isAnonymous" />
  </div>
</template>

<script>
export default {
  data: () => ({
    menu: false,
    isAnonymous: !eXo.env.portal.userName,
  }),
  computed: {
    mangersToDisplay() {
      return this.$root.managers;
    },
    isFavorite() {
      return this.$root.isFavorite;
    },
    params() {
      return {
        identityType: 'space',
        identityId: this.$root.spaceId,
        identityEnabled: true,
        identityDeleted: false,
        canRedactOnSpace: this.$root.canRedactOnSpace,
      };
    },
    isTopBarElement() {
      return this.$root.isTopBarElement;
    }
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
          && metadata.objectId === this.$root.spaceId
          && metadata.favorite !== this.$root.isFavorite) {
        this.$root.isFavorite = `${metadata.favorite}`;
      }
    },
  }
};
</script>
