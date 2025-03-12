<template>
  <div class="d-flex align-center">
    <v-menu
      v-model="menu"
      :close-on-content-click="false"
      content-class="no-box-shadow full-height pa-1 mt-6"
      elevation="2"
      max-width="350"
      min-width="300"
      :nudge-width="200"
      offset-y
      :open-on-hover="!isAnonymous"
      rounded="rounded"
      transition="slide-x-transition">
      <template #activator="{ on, attrs }">
        <v-card
          v-bind="attrs"
          class="d-inline-flex transparent"
          flat
          height="28"
          v-on="on">
          <a
            v-if="$root.displaySiteLogo"
            :aria-label="$t('space.avatar.href.title',{0: $root.spaceLogoTitle})"
            :href="$root.spacePortalPath">
            <v-list-item-avatar
              v-if="$root.spaceLogoPath"
              id="UserHomePortalLink"
              class="mx-0 my-auto spaceAvatar"
              size="28"
              tile>
              <img
                alt=""
                class="object-fit-contain"
                height="28"
                :src="$root.spaceLogoPath"
                width="auto">
            </v-list-item-avatar>
          </a>
          <a
            v-if="$root.displaySiteTitle"
            class="align-self-center brandingContainer space"
            :class="$root.displaySiteLogo && 'ms-4'"
            :href="$root.spacePortalPath">
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
              height="60"
              width="60">
              <v-img
                alt=""
                class="object-fit-cover"
                :src="`${$root.spaceLogoPath}&size=60x60`" />
            </v-list-item-avatar>
            <v-list-item-content class="pb-0 pt-0">
              <v-tooltip bottom>
                <template #activator="{ on, attrs }">
                  <span
                    v-bind="attrs"
                    class="primary--text text--darken-3 font-weight-bold text-truncate-2"
                    v-on="on">
                    {{ $root.spaceLogoTitle }}
                  </span>
                </template>
                <span>{{ $root.spaceLogoTitle }}</span>
              </v-tooltip>
              <v-list-item-subtitle>
                <span class="text-body">{{ $root.membersNumber }} {{ $t('space.logo.banner.popover.members') }}</span>
              </v-list-item-subtitle>
              <p
                v-sanitized-html="$root.spaceDescription"
                class="text-truncate-2 text-caption text--primary font-weight-medium"></p>
            </v-list-item-content>
          </v-list-item>
        </v-list>
        <v-list class="pa-0 mt-0 mb-0 transparent">
          <v-list-item class="pt-0 pb-0">
            <v-list-item-content>
              <v-container class="pa-0">
                <v-row
                  class="align-center"
                  no-gutters>
                  <v-col
                    class="text-truncate text-body text-left"
                    cols="6">
                    {{ $t('space.logo.banner.popover.managers') }}
                  </v-col>
                  <v-col
                    class="d-flex flex-nowrap justify-end pa-0"
                    cols="6">
                    <exo-user-avatars-list
                      avatar-overlay-position
                      clickable="'false'"
                      :compact="mangersToDisplay.length > 1"
                      :icon-size="30"
                      :margin-left="mangersToDisplay.length > 1 && 'ml-n4' || ''"
                      max="3"
                      :popover="false"
                      retrieve-extra-information
                      :users="mangersToDisplay"
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
                :muted="$root.muted"
                origin="spaceTopbarpopoverAction"
                :space-id="$root.spaceId" />
              <space-favorite-action
                entity-type="SPACE_TOP_BAR_TIPTIP"
                :is-favorite="isFavorite"
                :space-id="$root.spaceId"
                @added="$root.isFavorite = 'true'"
                @removed="$root.isFavorite = 'false'" />
              <extension-registry-components
                class="d-flex"
                element="div"
                element-class="mx-auto ma-lg-0"
                name="SpacePopover"
                :params="params"
                parent-element="div"
                type="space-popover-action" />
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
      mangersToDisplay () {
        return this.$root.managers;
      },
      isFavorite () {
        return this.$root.isFavorite;
      },
      params () {
        return {
          identityType: 'space',
          identityId: this.$root.spaceId,
          identityEnabled: true,
          identityDeleted: false,
          canRedactOnSpace: this.$root.canRedactOnSpace,
        };
      },
      isTopBarElement () {
        return this.$root.isTopBarElement;
      },
    },
    created () {
      document.addEventListener('metadata.favorite.updated', this.favoriteUpdated);
    },
    unmounted () {
      document.removeEventListener('metadata.favorite.updated', this.favoriteUpdated);
    },
    methods: {
      popoverActionEvent (clickedItem) {
        document.dispatchEvent(new CustomEvent('space-topbar-popover-action', { detail: clickedItem } ));
      },
      openDetails () {
        this.$root.$emit('displaySpaceHosts', this.mangersToDisplay);
        this.popoverActionEvent('displaySpaceHosts');
      },
      favoriteUpdated (event) {
        const metadata = event && event.detail;
        if (metadata && metadata.objectType === 'space'
          && metadata.objectId === this.$root.spaceId
          && metadata.favorite !== this.$root.isFavorite) {
          this.$root.isFavorite = `${metadata.favorite}`;
        }
      },
    },
  };
</script>
