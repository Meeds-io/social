<template>
  <v-menu
    v-if="popover && !isMobile"
    rounded="rounded"
    v-model="menu"
    open-on-hover
    :close-on-content-click="false"
    :left="popoverLeftPosition"
    :offset-x="offsetX"
    :offset-y="offsetY"
    content-class="profile-popover-menu transparent"
    color="transparent"
    elevation="0"
    max-width="356"
    min-width="356"
    max-height="160" >
    <template #activator="{ on, attrs }">
      <div 
        class="profile-popover user-wrapper"
        :class="extraClass">
        <a 
          v-if="avatar"
          v-bind="attrs"
          v-on="on"
          :id="id"
          :href="profileUrl"
          class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
          :class="avatarClass">
          <v-avatar
            :size="size"
            class="ma-0 pull-left">
            <img
              :src="avatarUrl"
              class="object-fit-cover ma-auto"
              loading="lazy"
              role="presentation">
          </v-avatar>
        </a>
        <a 
          v-else-if="fullname"
          v-bind="attrs"
          v-on="on"
          :id="id"
          :href="profileUrl">
          <p
            v-if="userFullname"
            :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
            class="text-truncate subtitle-2 text-left mb-0">
            {{ userFullname }}
            <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
          </p>
          <p v-if="$slots.subTitle" class="text-sub-title text-left mb-0">
            <slot name="subTitle"></slot>
          </p>
        </a>
        <a
          v-else
          v-bind="attrs"
          v-on="on"
          :id="id"
          :href="profileUrl"
          class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
          :class="itemsAlignStyle">
          <v-avatar
            :size="size"
            class="ma-0">
            <img
              :src="avatarUrl"
              class="object-fit-cover ma-auto"
              loading="lazy"
              role="presentation">
          </v-avatar>
          <div v-if="userFullname || $slots.subTitle" class="ms-2 overflow-hidden">
            <p
              v-if="userFullname"
              :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
              class="text-truncate subtitle-2 text-left mb-0">
              {{ userFullname }}
              <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
            </p>
            <p v-if="$slots.subTitle" class="text-sub-title text-left mb-0">
              <slot name="subTitle"></slot>
            </p>
          </div>
          <template v-if="$slots.actions">
            <slot name="actions"></slot>
          </template>
        </a>
      </div>
    </template> 
    <v-card 
    elevation="1" 
    class="pa-2"
    max-width="250"
    min-width="250" >
      <v-list-item class="px-0">
        <v-list-item-content class="py-0">
          <v-list-item-title>
            <exo-user-avatar
              :identity="identity"
              :size="46"
              bold-title
              link-style>
              <template v-if="position" slot="subTitle">
                <span class="caption text-bold">
                  {{ position }}
                </span>
              </template>
            </exo-user-avatar>
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <div v-if="isCurrentUser" class="d-flex justify-end">
        <extension-registry-components
          :params="params"
          class="d-flex"
          name="UserPopover"
          type="user-popover-action"
          parent-element="div"
          element="div"
          element-class="mx-auto ma-lg-0" />
        <div
          v-for="extension in enabledWebConferencingComponents"
          :key="extension.key"
          :class="`${extension.appClass} ${extension.typeClass}`"
          :ref="extension.key">
        </div>
      </div>
    </v-card>
  </v-menu>
  <div 
    v-else
    class="profile-popover user-wrapper"
    :class="extraClass">
    <a 
      v-if="avatar"
      :id="id"
      :href="profileUrl"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      :class="avatarClass">
      <v-avatar
        :size="size"
        class="ma-0 pull-left">
        <img
          :src="avatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
    </a>
    <a 
      v-else-if="fullname"
      :id="id"
      :href="profileUrl"
      class="pull-left d-flex align-start text-truncate">
      <span
        v-if="userFullname"
        :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
        class="text-truncate subtitle-2 my-auto">
        {{ userFullname }}
        <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
      </span>
      <span v-if="$slots.subTitle" class="text-sub-title text-truncate my-auto text-left">
        <slot name="subTitle"></slot>
      </span>
    </a>
    <a
      v-else
      :id="id"
      :href="profileUrl"
      class="d-flex flex-nowrap flex-grow-1 text-truncate container--fluid"
      :class="itemsAlignStyle">
      <v-avatar
        :size="size"
        class="ma-0">
        <img
          :src="avatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
      <div v-if="userFullname || $slots.subTitle" class="ms-2 overflow-hidden">
        <p
          v-if="userFullname"
          :class="[fullnameStyle, linkStyle && 'primary--text' || '']"
          class="text-truncate subtitle-2 text-left mb-0">
          {{ userFullname }}
          <span v-if="isExternal" class="muted font-weight-regular">{{ externalTag }} </span>
        </p>
        <p v-if="$slots.subTitle" class="text-sub-title  text-truncate text-left mb-0">
          <slot name="subTitle"></slot>
        </p>
      </div>
      <template v-if="$slots.actions">
        <slot name="actions"></slot>
      </template>
    </a>
  </div>
</template>

<script>
const randomMax = 10000;

export default {
  props: {
    identity: {
      type: Object,
      default: () => null,
    },
    profileId: {
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
    alignTop: {
      type: Boolean,
      default: () => false,
    },
    popover: {
      type: Boolean,
      default: () => false,
    },
    popoverLeftPosition: {
      type: Boolean,
      default: () => false,
    },
    url: {
      type: Boolean,
      default: () => true,
    },
    size: {
      type: Number,
      // eslint-disable-next-line no-magic-numbers
      default: () => 37,
    },
    extraClass: {
      type: String,
      default: () => '',
    },
    usernameClass: {
      type: String,
      default: () => '',
    },
    avatarClass: {
      type: String,
      default: () => '',
    },
    offsetX: {
      type: Boolean,
      default: () => false,
    },
    offsetY: {
      type: Boolean,
      default: () => true,
    },
  },
  data() {
    return {
      id: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      webConferencingExtensions: [],
      menu: false,
    };
  },
  watch: {
    menu () {
      if ( this.menu ) {
        this.webConferencingExtensions.map((extension) => {
          if ( !this.$refs[extension.key] ) {
            this.$nextTick().then(() => {
              this.initWebConferencingActionComponent(extension);
            });
          }
        });
      }
    }
  },
  computed: {
    enabledWebConferencingComponents() {
      return this.webConferencingExtensions.filter(extension => extension.enabled);
    },
    username() {
      return this.identity && this.identity.username;
    },
    userFullname() {
      return this.identity && this.identity.fullname;
    },
    position() {
      return this.identity && this.identity.position;
    },
    avatarUrl() {
      return this.identity && this.identity.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username ? this.username : this.profileId}/avatar`;
    },
    profileUrl() {
      if ( this.url ) {
        return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
      } else {
        return null;
      }
    },
    isExternal() {
      return this.identity && this.identity.external ;
    },
    externalTag() {
      return `( ${this.$t('userAvatar.external.label')} )`;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${this.smallFontSize && 'caption ' || ''}`;
    },
    itemsAlignStyle() {
      return `${this.alignTop && 'align-start' || 'align-center'}`;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    isCurrentUser() {
      return eXo.env.portal.userName !== this.username;
    },
    params() {
      return {
        identityType: 'USER_PROFILE',
        identityId: this.identity && this.identity.username,
      };
    },
  },
  created() {
    if (this.profileId) {
      this.$userService.getUser(this.profileId)
        .then(user => {
          this.identity = user;
        });
    }
    if ( this.popover && !this.isMobile) {
      this.refreshWebCOnferencingExtensions();
    }
  },
  methods: {
    refreshWebCOnferencingExtensions () {
      this.webConferencingExtensions = extensionRegistry.loadExtensions('user-profile-popover', 'action') || [];
    },
    initWebConferencingActionComponent(extension) {
      if (extension.enabled ) {
        let container = this.$refs[extension.key];
        if (container && container.length > 0 ) {
          container = container[0];
          extension.init(container, this.username);
        }
      }
    },
  }
};
</script>
