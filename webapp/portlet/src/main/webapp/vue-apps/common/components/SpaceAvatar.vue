<template>
  <div
    class="profile-popover space-avatar-wrapper"
    v-if="popover"
    :class="extraClass"
    v-identity-popover="space">
    <a
      v-if="notAccessibleSpace"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar not-clickable-link">
      <v-avatar
        v-if="avatar || !isMobile"
        :size="size"
        :class="pullLeft"
        tile
        class="my-auto">
        <img
          :src="defaultAvatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
      </v-avatar>
      <div
        v-if="fullname || !isMobile"
        :class="subtitleNewLineClass"
        class="pull-left text-truncate ms-2">
        <p
          class="text-truncate subtitle-2 my-auto hidden-space">
          {{ $t('spacesList.label.hiddenSpace') }}
        </p>
      </div>
    </a>
    <a
      v-else-if="avatar"
      v-bind="attrs"
      v-on="on"
      :id="id"
      :href="url"
      :aria-label="$t('space.avatar.href.title',{0:displayName})"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <v-avatar
        :size="size"
        :class="pullLeft"
        tile
        class="my-auto">
        <img
          :src="avatarUrl"
          :class="avatarClass"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
      </v-avatar>
    </a>
    <a
      v-else-if="fullname"
      v-bind="attrs"
      v-on="on"
      :id="id"
      :href="url"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <div
        v-if="displayName || $slots.subTitle"
        :class="subtitleNewLineClass"
        class="text-truncate ms-2">
        <p
          v-if="displayName"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
          class="text-truncate subtitle-2 my-auto">
          {{ displayName }}
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title my-auto">
          <slot name="subTitle">
          </slot>
        </p>
      </div>
    </a>
    <a
      v-else
      v-bind="attrs"
      v-on="on"
      :id="id"
      :href="url"
      :aria-label="$t('space.avatar.href.title',{0:displayName})"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <v-avatar
        :size="size"
        :class="pullLeft"
        tile
        class="my-auto">
        <img
          :src="avatarUrl"
          :class="avatarClass"
          class="object-fit-cover ma-auto"
          loading="lazy"
          alt="">
      </v-avatar>
      <div
        v-if="displayName || $slots.subTitle"
        :class="subtitleNewLineClass"
        class="text-truncate ms-2">
        <p
          v-if="displayName"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
          class="text-truncate subtitle-2 my-auto">
          {{ displayName }}
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title my-auto">
          <slot name="subTitle">
          </slot>
        </p>
      </div>
    </a>
  </div>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    space: {
      type: Object,
      default: () => null,
    },
    spacePrettyName: {
      type: String,
      default: () => null,
    },
    spaceId: {
      type: String,
      default: () => null,
    },
    avatar: {
      type: Boolean,
      default: () => false,
    },
    fullname: {
      type: Boolean,
      default: () => false,
    },
    boldTitle: {
      type: Boolean,
      default: () => false,
    },
    linkStyle: {
      type: Boolean,
      default: () => false,
    },
    smallFontSize: {
      type: Boolean,
      default: () => false,
    },
    subtitleNewLine: {
      type: Boolean,
      default: () => false,
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    popover: {
      type: Boolean,
      default: () => true,
    },
    popoverLeftPosition: {
      type: Boolean,
      default: () => false,
    },
    extraClass: {
      type: String,
      default: () => '',
    },
      
  },
  data() {
    return {
      id: `spaceAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
    };
  },
  computed: {
    displayName() {
      return this.space && this.space.displayName;
    },
    prettyName() {
      return this.space && this.space.prettyName;
    },
    groupId() {
      return this.space && this.space.groupId;
    },
    avatarUrl() {
      return this.space?.avatarUrl || (this.prettyName && `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/${this.prettyName}/avatar`);
    },
    url() {
      if (!this.groupId) {
        return '#';
      }
      const uri = this.groupId.replace(/\//g, ':');
      return `${eXo.env.portal.context}/g/${uri}/`;
    },
    spaceMembersCount() {
      return this.space && this.space.membersCount;
    },
    spaceDescription() {
      return this.space && this.space.description;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${this.smallFontSize && 'caption ' || ''}`;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    isSpaceMember() {
      return eXo.env.portal.isSpacesManager
        || this.space.isMember
        || this.space.canEdit
        || this.space?.members?.includes(eXo.env.portal.userName)
        || this.space?.managers?.includes(eXo.env.portal.userName);
    },
    notAccessibleSpace() {
      return this.space && this.space.visibility === 'hidden' && !this.isSpaceMember;
    },
    defaultAvatarUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/spaces/default-image/avatar`;
    },
    pullLeft() {
      return this.isMobile && ' ' || 'pull-left';
    },
    subtitleNewLineClass() {
      return !this.subtitleNewLine && `d-flex ${this.pullLeft}` || this.pullLeft;
    },
  },
  created() {
    if (!this.space) {
      if (this.spacePrettyName && !this.spaceId){
        this.$spaceService.getSpaceByPrettyName(this.spacePrettyName)
          .then(space => {
            this.space = space;
          });
      } else {
        if (this.spaceId) {
          this.$spaceService.getSpaceById(this.spaceId)
            .then(space => {
              this.space = space;
            });
        }
      }
    }
  },
};
</script>
