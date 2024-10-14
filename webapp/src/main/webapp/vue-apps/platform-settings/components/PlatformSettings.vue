<template>
  <v-app>
    <v-tooltip bottom>
      <template #activator="{ on, attrs }">
        <v-btn
          :href="$root.url"
          :aria-label="$t('platformSettings.label.openAdministration')"
          target="_blank"
          class="mx-2 d-none d-sm-block"
          icon
          v-on="on"
          v-bind="attrs">
          <v-icon
            class="icon-default-color"
            size="22">
            fas fa-cog
          </v-icon>
        </v-btn>
      </template>
      <span>{{ $t('platformSettings.label.openAdministration') }}</span>
    </v-tooltip>
  </v-app>
</template>