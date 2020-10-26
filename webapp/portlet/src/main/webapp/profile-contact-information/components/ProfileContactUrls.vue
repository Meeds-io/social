<template>
  <div v-if="user.urls && user.urls.length">
    <v-divider class="my-4" />
    <v-flex class="d-flex">
      <div class="align-start text-no-wrap font-weight-bold mr-3">
        {{ $t('profileContactInformation.url') }}
      </div>
      <div v-if="user.urls && user.urls.length" class="align-end flex-grow-1 text-truncate text-end">
        <div v-for="(url, i) in user.urls" :key="i" class="text-no-wrap text-truncate">
          <a :href="url.url" :title="url.url" target="_blank" rel="noopener noreferrer">
            {{ url.url }}
          </a>
        </div>
      </div>
    </v-flex>
  </div>
</template>

<script>
export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
  },
};
</script>