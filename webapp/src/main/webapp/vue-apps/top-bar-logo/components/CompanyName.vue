<template>
  <div v-if="$root.displayCompany" class="d-inline-flex">
    <a
      v-if="$root.displayCompanyLogo"
      :href="$root.portalPath"
      :aria-label="$t('space.avatar.href.title',{0: $root.logoTitle})">
      <v-list-item-avatar 
        v-if="$root.logoPath"
        id="UserHomePortalLink"
        height="36"
        max-width="100"
        min-width="auto"
        width="auto"
        class="ma-0"
        tile>
        <img
          :src="$root.logoPath"
          :alt="$t('space.avatar.img.alt',{0: $root.logoTitle})"
          height="36"
          width="auto"
          class="object-fit-contain">
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
