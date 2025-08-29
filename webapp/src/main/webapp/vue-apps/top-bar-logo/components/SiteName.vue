<template>
  <space-logo-banner v-if="$root.displaySite && $root.spaceId" />
  <div v-else-if="$root.displaySite" class="d-inline-flex">
    <a
      v-if="$root.displaySiteLogo"
      :href="$root.siteHomePath"
      :aria-label="tooltip">
      <v-tooltip :disabled="$root.displaySiteTitle" bottom>
        <template #activator="{on, attrs}">
          <v-list-item-avatar
            v-on="on"
            v-bind="{
              ...attrs,
              role: null}"
            id="UserHomePortalLink"
            size="36"
            class="ma-0"
            tile>
            <v-img
              v-if="iconUrl"
              :src="iconUrl"
              :max-height="iconSize"
              :height="iconSize"
              :max-width="iconSize"
              contain
              eager />
            <v-icon
              v-else
              :size="iconSize"
              class="icon-default-color">
              {{ icon || 'fa-folder' }}
            </v-icon>
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
      <div class="siteTitle text-body menu-text-color font-weight-bold text-truncate">
        {{ $root.siteTitle }}
      </div>
    </a>
  </div>
</template>
<script>
export default {
  data: () => ({
    iconSize: 28,
  }),
  computed: {
    tooltip() {
      return this.$root.isSitePage && this.$t('menu.pageNameTooltip', {
        0: this.$root.siteTitle,
      }) || this.$t('menu.siteNameTooltip', {
        0: this.$root.siteTitle,
      });
    },
    iconUrl() {
      if (this.$root.siteIcon?.includes?.('base64') || this.$root.siteIcon?.includes?.('/')) {
        return this.$root.siteIcon;
      } else {
        return null;
      }
    },
    icon() {
      if (this.iconUrl) {
        return null;
      } else {
        return this.$root.siteIcon;
      }
    },
  },
};
</script>
