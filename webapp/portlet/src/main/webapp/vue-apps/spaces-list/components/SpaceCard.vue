<template>
  <v-hover v-model="hoverCard">
    <v-card
      :href="spaceUrl"
      :min-height="minHeight"
      :height="height"
      :max-height="height"
      :elevation="hoverCard && 3 || 0"
      :min-width="minWidth"
      :max-width="maxWidth"
      width="auto"
      class="d-flex flex-column border-color">
      <space-card-unread-badge :space="space" />
      <v-card
        :min-height="bannerHeight"
        :max-height="bannerHeight"
        min-width="100%"
        max-width="100%"
        class="overflow-hidden no-border-radius position-relative d-flex justify-center z-index-one"
        flat>
        <img
          :src="space.bannerUrl"
          :alt="$t('spaceList.spaceBanner.alt')"
          style="max-width: 1000%; min-width: 100%; max-height: 100%; min-height: 100%;"
          class="position-absolute border-top-left-radius border-top-right-radius"
          height="100%"
          width="auto">
      </v-card>
      <v-card
        :min-height="avatarSize"
        :max-height="avatarSize"
        min-width="100%"
        max-width="100%"
        class="d-flex mt-n2 px-2"
        flat>
        <v-card
          :min-width="avatarSize"
          :max-width="avatarSize"
          :min-height="avatarSize"
          :max-height="avatarSize"
          class="overflow-hidden d-flex align-center justify-center z-index-two"
          flat>
          <img
            :src="space.avatarUrl"
            :alt="$t('spaceList.spaceAvatar.alt')"
            style="max-width: 1000%; max-height: 100%;"
            height="100%"
            width="auto"
            class="overflow-hidden border-radius">
        </v-card>
        <div class="d-flex flex-column flex-grow-1 justify-center flex-shrink-1 overflow-hidden ps-2 pt-4">
          <div
            v-text="spaceDisplayName"
            class="flex-shrink-0 text-truncate-2 max-height-2lh font-weight-bold line-height-normal full-width"></div>
          <div
            v-sanitized-html="spaceMembersCount"
            class="flex-shrink-0 text-subtitle pb-1"></div>
        </div>
      </v-card>
      <div class="flex-grow-1 flex-shrink-1">
        <div
          v-if="!$root.isMobile"
          v-sanitized-html="spaceDescription"
          class="text-truncate-3 max-height-3lh full-width flex-shrink-1 px-2 mt-2"></div>
      </div>
      <v-card
        class="d-flex align-center full-width flex-grow-0 flex-shrink-0 my-2 position-absolute b-0"
        flat>
        <space-card-button
          v-if="spacePublicSiteUrl"
          :extension="{
            icon: 'fa-globe',
            href: spacePublicSiteUrl,
            title: $t('spaceList.publicSite'),
          }"
          :space="space"
          icon />
        <v-spacer />
        <space-favorite-action
          v-if="space.isMember"
          :is-favorite="space.isFavorite"
          :space-id="space.id"
          :icon-size="20"
          class="ms-1" />
        <space-card-button
          v-for="(extension, i) in enabledProfileActionExtensions"
          :key="i"
          :extension="extension"
          :space="space"
          class="ms-1" />
        <space-card-membership-options
          :space="space"
          class="ms-1" />
      </v-card>
    </v-card>
  </v-hover>
</template>
<script>
export default {
  props: {
    space: {
      type: Object,
      default: () => ({}),
    },
    height: {
      type: Number,
      default: () => 227,
    },
    minHeight: {
      type: Number,
      default: () => 227,
    },
    minWidth: {
      type: Number,
      default: () => 220,
    },
    maxWidth: {
      type: Number,
      default: () => (366 - 18),
    },
    profileActionExtensions: {
      type: Array,
      default: null,
    },
  },
  data: () => ({
    avatarSize: 65,
    bannerHeight: 50,
    hoverCard: false,
  }),
  computed: {
    spaceDisplayName() {
      return this.space.displayName;
    },
    spaceDescription() {
      return this.space.description?.replace?.(/<p>/g, '<div>')?.replace?.(/<\/p>/g, '</div>')?.replace?.(/<ul>/g, '<ul class="ma-0 pa-0">') || '';
    },
    spaceMembersCount() {
      return this.$t('spaceList.spaceMembers', {0: `<strong>${this.space.membersCount}</strong>`});
    },
    spaceUrl() {
      return `${eXo.env.portal.context}/s/${this.space.id}`;
    },
    spacePublicSiteUrl() {
      return this.space.publicSiteName
        && this.space.publicSiteVisibility !== 'manager'
        && `${eXo.env.portal.context}/${this.space.publicSiteName}`;
    },
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.space || !this.space.isMember) {
        return [];
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.space));
    },
  },
};
</script>