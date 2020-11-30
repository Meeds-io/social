<template>
  <v-list-item class="pa-0 pb-4">
    <v-list-item-avatar
      :href="url"
      class="my-0 ps-4"
      tile>
      <v-avatar :size="avatarSize" tile>
        <v-img
          :src="avatarUrl"
          :height="avatarSize"
          :width="avatarSize"
          :max-height="avatarSize"
          :max-width="avatarSize"
          class="mx-auto spaceAvatar" />
      </v-avatar>
    </v-list-item-avatar>
    <v-list-item-content
      :id="id"
      :href="url"
      class="pa-0">
      <v-list-item-title>
        <a
          :href="url"
          class="text-color text-truncate spaceTitle">
          {{ space.displayName }}
        </a>
      </v-list-item-title>
      <v-list-item-subtitle>
        <template>
          <span class="alreadyMemberClass">{{ $t('externalSpacesList.label.alreadyMember') }}</span>
        </template>
      </v-list-item-subtitle>
    </v-list-item-content>
  </v-list-item>
</template>
<script>
const randomMax = 10000;

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
    avatarUrl() {
      return this.space.avatarUrl || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.space.prettyName}/avatar`;
    },
    url() {
      if (!this.space || !this.space.groupId) {
        return '#';
      }
      const uri = this.space.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
  },

};
</script>
