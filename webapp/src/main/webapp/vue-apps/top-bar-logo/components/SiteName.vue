<template>
  <space-logo-banner v-if="$root.displaySite && $root.spaceId" />
  <div v-else-if="$root.displaySite" class="d-inline-flex">
    <a
      v-if="$root.displaySiteLogo"
      :href="$root.portalPath"
      :aria-label="$t('space.avatar.href.title',{0: $root.logoTitle})">
      <v-list-item-avatar 
        id="UserHomePortalLink"
        size="36"
        class="ma-0"
        tile>
        <v-icon size="28">{{ $root.siteIcon || 'fa-globe' }}</v-icon>
      </v-list-item-avatar>
    </a>
    <a
      v-if="$root.displaySiteTitle"
      :href="$root.portalPath"
      :class="$root.displaySiteLogo && 'ms-4'"
      class="align-self-center brandingContainer">
      <div class="siteTitle text-body menu-text-color font-weight-bold text-truncate">
        {{ $root.siteTitle }}
      </div>
    </a>
  </div>
</template>
<script>
export default {
  computed: {
    
  },
};
</script>
