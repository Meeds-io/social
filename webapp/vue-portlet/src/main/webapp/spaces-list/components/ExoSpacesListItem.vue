<template>
  <v-col cols="12" md="3">
    <v-card :id="spaceMenuParentId">
      <v-img
        :src="spaceBannerUrl"
        class="white--text align-start"
        height="80px">
        <div class="d-flex pa-2">
          <v-btn icon class="spaceInfoIcon">
            <v-icon small>fa-info</v-icon>
          </v-btn>
          <v-spacer />
          <v-btn icon depressed class="spaceActionIcon" @click="displayActionMenu = true">
            <v-icon>mdi-dots-vertical</v-icon>
          </v-btn>
          <v-menu
            ref="actionMenu"
            v-model="displayActionMenu"
            :attach="`#${spaceMenuParentId}`"
            transition="scale-transition"
            content-class="spaceActionMenu"
            offset-y>
            <v-list class="pa-0">
              <v-list-item @click="editSpace">
                <v-list-item-title>{{ $t('spacesList.button.edit') }}</v-list-item-title>
              </v-list-item>
              <v-list-item
                v-for="(extension, i) in spaceActionExtensions"
                :key="i"
                @click="extension.click">
                <v-list-item-title>{{ extension.title }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </div>
      </v-img>
  
      <div class="spaceAvatarImg">
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
        <v-btn class="btn mx-auto joinSpaceButton" depressed block>
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
    spaceActionExtensions: {
      type: Array,
      default: () => [],
    },
  },
  data: () => ({
    displayActionMenu: false,
    membersCount: 0,
    waitTimeUntilCloseMenu: 200,
  }),
  computed: {
    spaceAvatarUrl() {
      return this.space && (this.space.avatarUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/avatar`);
    },
    spaceBannerUrl() {
      return this.space && (this.space.bannerUrl || `/portal/rest/v1/social/spaces/${this.space.prettyName}/banner`);
    },
    spaceMenuParentId() {
      return this.space && this.space.id && `spaceMenuParent-${this.space.id}` || 'spaceMenuParent';
    },
  },
  created() {
    if (this.space && this.space.id) {
      spaceService.getSpaceMembers(this.space.id, 0, 1)
        .then(data => this.membersCount = data && data.size || 0);
    }
    $(document).on('mousedown', () => {
      if (this.displayActionMenu) {
        window.setTimeout(() => {
          this.displayActionMenu = false;
        }, this.waitTimeUntilCloseMenu);
      }
    });
  },
  methods: {
    editSpace() {
      
    },
  },
};
</script>

