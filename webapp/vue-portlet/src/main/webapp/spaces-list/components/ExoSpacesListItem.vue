<template>
  <v-col cols="12" md="3">
    <v-card>
      <v-img
        :src="spaceBannerUrl"
        class="white--text align-end"
        height="80px">
      </v-img>
  
      <div style="margin-top: -42px;">
        <v-img
          :src="spaceAvatarUrl"
          class="mx-auto mt-3"
          height="50px"
          width="50px"
          max-height="50px"
          max-width="50px">
        </v-img>
      </div>

      <v-card-text class="align-center">
        <div>{{ space.displayName }}</div>
        <v-card-subtitle class="pb-0">{{ $t('spacesList.label.members', {0: membersCount}) }}</v-card-subtitle>
      </v-card-text>
  
      <v-card-actions>
        <v-btn class="btn mx-auto" block>
          <v-icon>mdi-plus</v-icon>
          {{ $t('spacesList.button.join') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-col>
</template>

<script>
import * as spaceService from '../js/SpaceService.js'; 

export default {
  props: {
    space: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    membersCount: 0,
  }),
  computed: {
    spaceAvatarUrl() {
      return this.space && (this.space.avatarUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/avatar`);
    },
    spaceBannerUrl() {
      return this.space && (this.space.bannerUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/banner`);
    },
  },
  created() {
    if (this.space && this.space.id) {
      spaceService.getSpaceMembers(this.space.id, 0, 1)
        .then(data => this.membersCount = data && data.size || 0);
    }
  },
};
</script>

