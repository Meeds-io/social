<template>
  <div class="d-flex">
    <v-avatar
      id="profileAvatar"
      class="align-start flex-grow-0 border-color profileHeaderAvatar"
      :class="owner && hover && 'profileHeaderAvatarHoverEdit'"
      max-height="165"
      max-width="165"
      min-height="44"
      min-width="44"
      :size="size">
      <v-img
        id="profileAvatarImg"
        eager
        :lazy-src="userAvatarUrl"
        max-height="165"
        max-width="165"
        min-height="44"
        min-width="44"
        role="presentation"
        :src="userAvatarUrl"
        transition="none" />
      <v-btn
        v-if="owner"
        v-show="hover"
        id="profileAvatarEditButton"
        ref="avatarInput"
        class="changeAvatarButton"
        dark
        icon
        outlined
        :title="$t('UIChangeAvatarContainer.label.ChangeAvatar')"
        @click="$emit('edit')">
        <v-icon size="18">
          fas fa-camera
        </v-icon>
      </v-btn>
    </v-avatar>
    <slot></slot>
  </div>
</template>

<script>
  export default {
    props: {
      user: {
        type: Object,
        default: () => null,
      },
      avatarData: {
        type: Array,
        default: null,
      },
      owner: {
        type: Boolean,
        default: () => true,
      },
      hover: {
        type: Boolean,
        default: () => false,
      },
      size: {
        type: Number,
        default: () => 165,
      },
    },
    computed: {
      userAvatarUrl () {
        const defaultAvatarUrl = `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/default-image/avatar`;
        return this.user?.enabled? (this.avatarData || `${this.user.avatar}${this.user.avatar.includes('?')? '&size=165x165' : '?size=165x165'}` || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.user.username}/avatar?size=165x165`) : `${defaultAvatarUrl}?size=165x165`;
      },
    },
  };
</script>