<template>
  <div
    v-if="$root.displayCompany"
    class="d-inline-flex">
    <a
      v-if="$root.displayCompanyLogo"
      :aria-label="$t('menu.companyNameTooltip',{0: $root.logoTitle})"
      :href="$root.portalPath">
      <v-list-item-avatar 
        v-if="$root.logoPath"
        id="UserHomePortalLink"
        class="ma-0"
        height="36"
        max-width="100"
        min-width="auto"
        tile
        width="auto">
        <img
          :alt="$t('menu.companyLogoTooltip')"
          class="object-fit-contain"
          height="36"
          :src="$root.logoPath"
          width="auto">
      </v-list-item-avatar>
    </a>
    <a
      v-if="$root.displayCompanyTitle"
      :aria-label="$t('menu.companyNameTooltip',{0: $root.logoTitle})"
      class="align-self-center brandingContainer"
      :class="$root.displayCompanyLogo && 'ms-4'"
      :href="$root.portalPath">
      <div class="logoTitle text-body menu-text-color font-weight-bold">
        {{ $root.logoTitle }}
      </div>
    </a>
  </div>
</template>
<script></script>
