<template>
  <v-tooltip bottom v-if="url">
    <template #activator="{ on, attrs }">
      <v-btn
        v-if="url"
        icon
        class="mx-2"
        :href="url"
        target="_blank">
        <v-icon
          v-on="on"
          v-bind="attrs"
          class="icon-default-color"
          size="22">
          fas fa-cog
        </v-icon>
      </v-btn>
    </template>
    <span> {{ $t('platformSettings.label.openAdministration') }}</span>
  </v-tooltip>
</template>

<script>
export default {
  props: {
    url: {
      type: String,
      default: null,
    }
  },
};
</script>