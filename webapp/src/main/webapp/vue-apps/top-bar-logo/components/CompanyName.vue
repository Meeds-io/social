<template>
  <div v-if="$root.displayCompany" class="d-inline-flex">
    <a
      v-if="$root.displayCompanyLogo"
      :href="$root.portalPath"
      :aria-label="$t('menu.companyNameTooltip',{0: $root.logoTitle})">
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
          :alt="$t('menu.companyLogoTooltip')"
          height="36"
          width="auto"
          class="object-fit-contain">
      </v-list-item-avatar>
    </a>
    <a
      v-if="$root.displayCompanyTitle"
      :href="$root.portalPath"
      :class="$root.displayCompanyLogo && 'ms-4'"
      :aria-label="$t('menu.companyNameTooltip',{0: $root.logoTitle})"
      class="align-self-center brandingContainer">
      <div class="logoTitle text-body menu-text-color font-weight-bold">
        {{ $root.logoTitle }}
      </div>
    </a>
  </div>
</template>
<script></script>
