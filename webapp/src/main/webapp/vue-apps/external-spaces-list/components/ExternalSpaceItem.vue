<template>
  <v-list-item
    class="pa-1 pb-1"
    :href="url">
    <v-list-item-avatar
      class="my-0"
      :href="url"
      tile>
      <v-avatar
        :size="avatarSize"
        tile>
        <v-img
          class="mx-auto spaceAvatar"
          :height="avatarSize"
          :max-height="avatarSize"
          :max-width="avatarSize"
          role="presentation"
          :src="avatarUrl"
          :width="avatarSize" />
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content
      :id="id"
      class="pa-0">
      <v-list-item-title>
        <a
          class="text-color text-truncate spaceTitle">
          {{ space.displayName }}
        </a>
      </v-list-item-title>
    </v-list-item-content>
  </v-list-item>
</template>
<script>

  export default {
    props: {
      space: {
        type: Object,
        default: () => null,
      },
      avatarSize: {
        type: Number,
        default: () => 37,
      },
    },
    computed: {
      avatarUrl () {
        return this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.space.prettyName}/avatar`;
      },
      url () {
        return this.space.id && `${eXo.env.portal.context}/s/${this.space.id}` || '#';
      },
    },

  };
</script>
