<template>
  <div v-if="$root.displayCompany" class="d-inline-flex">
    <a
      v-if="$root.displayCompanyLogo"
      :href="$root.portalPath"
      :aria-label="$t('space.avatar.href.title',{0: $root.logoTitle})">
      <v-list-item-avatar 
        v-if="$root.logoPath"
        id="UserHomePortalLink"
        size="36"
        class="ma-0"
        tile>
        <v-img :src="$root.logoPath" :alt="$t('space.avatar.img.alt',{0: $root.logoTitle})" />
      </v-list-item-avatar>
    </a>
    <a
      v-if="$root.displayCompanyTitle"
      :href="$root.portalPath"
      :class="$root.displayCompanyLogo && 'ms-4'"
      class="align-self-center brandingContainer">
      <div class="logoTitle text-body menu-text-color font-weight-bold text-truncate">
        {{ $root.logoTitle }}
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
