<template>
  <div v-if="user.urls && user.urls.length>0">
    <v-divider class="my-4" />
    <v-flex class="d-flex">
      <div
        :class="skeleton && 'skeleton-text skeleton-text-width skeleton-background skeleton-text-height skeleton-border-radius'"
        class="align-start text-no-wrap font-weight-bold mr-3">
        {{ $t('profileContactInformation.url') }}
      </div>
      <div
        v-if="skeleton"
        class="align-end flex-grow-1 text-truncate text-end skeleton-text skeleton-text-width skeleton-background skeleton-text-height-fine skeleton-border-radius">
        &nbsp;&nbsp;&nbsp;&nbsp;
      </div>
      <div v-else-if="user.urls && user.urls.length" class="align-end flex-grow-1 text-truncate text-end">
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
    skeleton: {
      type: Boolean,
      default: false,
    },
  },
};
</script>