<template>
  <v-layout
    row
    wrap
    pa-1
    pb-1
    mx-0>
    <v-flex
      xs12>
      <v-list v-if="spacesRequests?.length">
        <v-list-item
          v-for="item in spacesRequests"
          :key="item.space.id"
          class="py-0 px-2">
          <v-list-item-avatar
            class="my-0 ps-2"
            tile>
            <v-avatar :size="avatarSize" tile>
              <v-img
                :src="item.space.avatar"
                :height="avatarSize"
                :width="avatarSize"
                :max-height="avatarSize"
                :max-width="avatarSize"
                class="mx-auto spaceAvatar"
                role="presentation" />
            </v-avatar>
          </v-list-item-avatar>
          <v-list-item-content class="py-0">
            <v-list-item-title class="text-color text-truncate requestSpaceName" v-text="item.space.displayName" />
            <v-list-item-subtitle class="caption grey-color" v-sanitized-html="item.description" />
          </v-list-item-content>
          <v-list-item-action>
            <v-btn-toggle
              class="transparent"
              dark>
              <v-btn
                :loading="saving"
                :title="$t('externalSpacesList.tooltip.AcceptToJoin')"
                text
                icon
                small
                min-width="auto"
                class="px-0"
                @click="replyInvitationToJoinSpace(item.space, 'approved')">
                <v-icon color="green" size="20">mdi-checkbox-marked-circle</v-icon>
              </v-btn>
              <v-btn
                :loading="saving"
                :title="$t('externalSpacesList.tooltip.DeclineInvitation')"
                text
                icon
                small
                min-width="auto"
                class="px-0"
                @click="replyInvitationToJoinSpace(item.space, 'ignored')">
                <v-icon color="red" size="20">mdi-close-circle</v-icon>
              </v-btn>
            </v-btn-toggle>
          </v-list-item-action>
        </v-list-item>
      </v-list>
    </v-flex>
  </v-layout>
</template>
<script>
export default {
  props: {
    avatarSize: {
      type: Number,
      default: () => 37,
    },
  },
  data: () => ({
    saving: false,
    spacesRequests: [],
  }),
  created(){
    this.getSpacesRequests();
  },
  methods: {
    getSpacesRequests() {
      return this.$spaceService.getSpaceMemberships({
        user: eXo.env.portal.userName,
        status: 'invited',
        expand: 'spaces',
        returnSize: true,
        limit: -1,
      }).then(data => this.spacesRequests = data?.spacesMemberships || []);
    },
    async replyInvitationToJoinSpace(item, reply) {
      this.saving = true;
      try {
        if (reply === 'approved') {
          await this.$spaceService.accept(item.space.id);
          this.$emit('invitationReplied', {
            id: item.id,
            displayName: item.space.displayName,
            avatarUrl: item.space.avatar,
          });
        } else if (reply === 'ignored') {
          await this.$spaceService.deny(item.space.id);
        }
        this.getSpacesRequests();
      } finally {
        this.saving = false;
      }
    },
  }
};
</script>
