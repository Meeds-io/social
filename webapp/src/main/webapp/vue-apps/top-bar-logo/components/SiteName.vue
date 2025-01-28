<template>
  <space-logo-banner v-if="$root.displaySite && $root.spaceId" />
  <div v-else-if="$root.displaySite" class="d-inline-flex">
    <a
      v-if="$root.displaySiteLogo"
      :href="$root.siteHomePath"
      :title="tooltip">
      <v-tooltip :disabled="$root.displaySiteTitle" bottom>
        <template #activator="{on, attrs}">
          <v-list-item-avatar
            v-on="on"
            v-bind="attrs"
            id="UserHomePortalLink"
            size="36"
            class="ma-0"
            tile>
            <v-icon size="28">{{ $root.siteIcon || 'fa-folder' }}</v-icon>
          </v-list-item-avatar>
        </template>
        {{ $root.siteTitle }}
      </v-tooltip>
    </a>
    <a
      v-if="$root.displaySiteTitle"
      :href="$root.siteHomePath"
      :class="$root.displaySiteLogo && 'ms-4'"
      :title="tooltip"
      class="align-self-center brandingContainer">
      <div class="siteTitle top-bar-text-body text-truncate">
        {{ $root.siteTitle }}
      </div>
    </a>
  </div>
</template>
<script>
export default {
  computed: {
    tooltip() {
      return this.$root.isSitePage && this.$t('menu.pageNameTooltip', {
        0: this.$root.siteTitle,
      }) || this.$t('menu.siteNameTooltip', {
        0: this.$root.siteTitle,
      });
    },
  },
};
</script>
