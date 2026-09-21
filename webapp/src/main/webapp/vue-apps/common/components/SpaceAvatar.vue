<template>
  <div
    :class="[extraClass, popover && 'profile-popover' || '']"
    v-identity-popover="popoverIdentity"
    class="space-avatar-wrapper">
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
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy">
      </v-avatar>
      <div
        v-if="fullname || !isMobile"
        :class="[subtitleNewLineClass, textTruncateClass]"
        class="pull-left ms-2">
        <p
          :class="textTruncateClass"
          class="my-auto hidden-space">
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
      :target="linkTarget"
      :aria-label="$t('space.avatar.href.title',{0:displayName})"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <v-avatar
        :size="size"
        :class="pullLeft"
        tile
        class="my-auto">
        <img
          :src="displayedAvatarUrl"
          :class="avatarClass"
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy"
          @error="avatarFailed = true">
      </v-avatar>
    </a>
    <a
      v-else-if="fullname"
      v-bind="attrs"
      v-on="on"
      :id="id"
      :href="url"
      :target="linkTarget"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <div
        v-if="displayName || $slots.subTitle"
        :class="[subtitleNewLineClass, textTruncateClass]"
        class="ms-2">
        <p
          v-if="displayName"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '', textTruncateClass]"
          class="my-auto">
          {{ displayName }}
        </p>
        <p v-if="$slots.subTitle" class="text-subtitle my-auto">
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
      :target="linkTarget"
      :aria-label="$t('space.avatar.href.title',{0:displayName})"
      class="flex-nowrap flex-shrink-0 d-flex spaceAvatar">
      <v-avatar
        :size="size"
        :class="pullLeft"
        tile
        class="my-auto">
        <img
          :src="displayedAvatarUrl"
          :class="avatarClass"
          alt=""
          class="object-fit-cover ma-auto"
          loading="lazy"
          @error="avatarFailed = true">
      </v-avatar>
      <div
        v-if="displayName || $slots.subTitle"
        :class="[subtitleNewLineClass, textTruncateClass]"
        class="ms-2">
        <p
          v-if="displayName"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '', textTruncateClass]"
          class="my-auto text-color">
          {{ displayName }}
        </p>
        <p v-if="$slots.subTitle" class="text-subtitle my-auto">
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
    spaceId: {
      type: String,
      default: () => null,
    },
    spacePrettyName: {
      type: String,
      default: () => null,
    },
    spaceGroupId: {
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
    linkTarget: {
      type: String,
      default: () => '_self',
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
    /**
     * Renders a HIDDEN space the viewer is not a member of as its normal
     * link (avatar, name) instead of the anonymous "Hidden space"
     * placeholder. The link leads to /s/{id}; where that lands (the space
     * access page, or page-not-found for a closed hidden space the viewer is
     * not invited to) is the platform's decision. Only for lists whose server
     * already decided the space may be shown to this viewer, e.g. sub-spaces
     * listed under 'show hidden subspaces'.
     */
    linkHiddenSpace: {
      type: Boolean,
      default: () => false,
    },
    popoverLeftPosition: {
      type: Boolean,
      default: () => false,
    },
    extraClass: {
      type: String,
      default: () => '',
    },
    textTruncateClass: {
      type: String,
      default: () => 'text-truncate',
    },
  },
  data() {
    return {
      id: `spaceAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      avatarFailed: false,
    };
  },
  watch: {
    /**
     * The same instance may display several spaces over its life (popover
     * menu, administration drawers): a failed load must not pin the default
     * image onto the next space. Keyed on the URL so a re-render of the same
     * space does not retry a known-failing load. No shipped path reaches a
     * failure today (the popover is off for the rows that could 404); this
     * is a guard for the next link-hidden-space consumer.
     *
     * @returns {void}
     */
    avatarUrl() {
      this.avatarFailed = false;
    },
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
    /**
     * What the linked branches actually display. The avatar endpoint answers
     * 404 for a HIDDEN and CLOSED space to a viewer who is neither member nor
     * invited (SpaceRest.getSpaceAvatarById): rather than predicting that
     * rule here, the image falls back to the default one when it fails to
     * load, so the real avatar shows whenever the server serves it.
     *
     * @returns {string} the avatar URL, or the default image after a failure
     */
    displayedAvatarUrl() {
      return this.avatarFailed && this.defaultAvatarUrl || this.avatarUrl;
    },
    url() {
      if (!this.space?.id) {
        return '#';
      }
      return `${eXo.env.portal.context}/s/${this.space?.id}`;
    },
    spaceMembersCount() {
      return this.space && this.space.membersCount;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${this.smallFontSize && 'caption ' || ''}`;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    canAccessSpace() {
      return eXo.env.portal.isAdministrator
        || this.space.isMember
        || this.space.canEdit
        || this.space?.members?.includes(eXo.env.portal.userName)
        || this.space?.managers?.includes(eXo.env.portal.userName);
    },
    /**
     * The identity bound to the hover popover: none when the popover is off,
     * which the identity-popover directive treats as "no popover" — the
     * avatar and its link still render.
     *
     * @returns {object|null} the space, or null when the popover is disabled
     */
    popoverIdentity() {
      return this.popover && this.space || null;
    },
    hiddenToViewer() {
      return !!this.space && this.space.visibility === 'hidden' && !this.canAccessSpace;
    },
    notAccessibleSpace() {
      return this.hiddenToViewer && !this.linkHiddenSpace;
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
      if (this.spaceId) {
        this.$spaceService.getSpaceById(this.spaceId)
          .then(space => this.space = space);
      } else if (this.spacePrettyName) {
        this.$spaceService.getSpaceByPrettyName(this.spacePrettyName)
          .then(space => this.space = space);
      } else if (this.spaceGroupId) {
        this.$spaceService.getSpaceByGroupId(this.spaceGroupId)
          .then(space => this.space = space);
      }
    }
  },
};
</script>
