<template>
  <div 
    class="exo-user-wrapper"
    :class="extraClass">
    <a 
      v-if="avatar"
      :id="id"
      :href="profileUrl"
      class="flex-nowrap flex-grow-1 d-flex text-truncate container--fluid"
      :class="avatarClass"
      @mouseover="showUserPopover"
      @mouseleave="hideUserPopover">
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
      class="pull-left d-flex flex-column align-start text-truncate"
      :class="usernameClass && 'mt-n-small' || ''"
      @mouseover="showUserPopover"
      @mouseleave="hideUserPopover">
      <span
        v-if="userFullname"
        :class="fullnameStyle"
        class="text-truncate subtitle-2 my-auto">
        {{ userFullname }}
        <span v-if="isExternal" class="muted">{{ externalTag }} </span>
      </span>
      <span v-if="$slots.subTitle" class="text-sub-title my-auto text-left">
        <slot name="subTitle"></slot>
      </span>
    </a>
    <a
      v-else
      :id="id"
      :href="profileUrl"
      class="d-flex flex-nowrap flex-grow-1 align-center text-truncate container--fluid"
      @mouseover="showUserPopover"
      @mouseleave="hideUserPopover">
      <v-avatar
        :size="size"
        class="ma-0">
        <img
          :src="avatarUrl"
          class="object-fit-cover ma-auto"
          loading="lazy"
          role="presentation">
      </v-avatar>
      <p v-if="userFullname || $slots.subTitle" class="ms-2 d-flex align-start flex-column text-truncate mb-0">
        <span
          v-if="userFullname"
          :class="fullnameStyle"
          class="text-truncate subtitle-2 my-auto">
          {{ userFullname }}
          <span v-if="isExternal" class="muted">{{ externalTag }} </span>
        </span>
        <span v-if="$slots.subTitle" class="text-sub-title my-auto text-left">
          <slot name="subTitle"></slot>
        </span>
      </p>
    </a>
    <v-menu
      v-if="popover && !isMobile"
      rounded="rounded"
      v-model="displayUserPopover"
      :attach="`#${id}`"
      :close-on-content-click="false"
      :nudge-bottom="x"
      :nudge-right="y"
      :left="popoverLeftPosition"
      content-class="white"
      max-width="270"
      min-width="270"
      offset-y
      offset-x>
      <v-card elevation="0" class="pa-2">
        <v-list-item class="px-2">
          <v-list-item-content class="py-0">
            <v-list-item-title>
              <exo-user
                :identity="identity"
                :size="46"
                bold-title
                link-style>
                <template v-if="position" slot="subTitle">
                  <span class="caption text-bold">
                    {{ position }}
                  </span>
                </template>
              </exo-user>
            </v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <div v-if="isCurrentUser" class="d-flex justify-end">
          <v-btn
            v-for="(extension, i) in enabledProfileActionExtensions"
            :key="i"
            :ripple="false"
            icon
            color="primary"
            @click="extension.click(identity)">
            <v-icon :size="18" :class="extension.additionalClass ? extension.additionalClass : ''">{{ extension.icon2 }} </v-icon>
          </v-btn>
          <div
            v-for="extension in enabledWebConferencingComponents"
            :key="extension.key"
            class="py-2 ps-2 pe-0"
            :class="`${extension.appClass} ${extension.typeClass}`"
            :ref="extension.key">
          </div>
        </div>
      </v-card>
    </v-menu>
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
    popover: {
      type: Boolean,
      default: () => false,
    },
    popoverLeftPosition: {
      type: Boolean,
      default: () => false,
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
      type: Boolean,
      default: () => false,
    },
    avatarClass: {
      type: String,
      default: () => '',
    }
  },
  data() {
    return {
      id: `userAvatar${parseInt(Math.random() * randomMax)
        .toString()
        .toString()}`,
      isExternal: false,
      displayUserPopover: false,
      profileActionExtensions: [],
      webConferencingExtensions: [],
    };
  },
  computed: {
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.identity) {
        return [];
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.identity));
    },
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
      return this.identity && this.identity.avatar || `${eXo.env.portal.context}/${eXo.env.portal.rest}/v1/social/users/${this.username}/avatar`;
    },
    profileUrl() {
      return `${eXo.env.portal.context}/${eXo.env.portal.portalName}/profile/${this.username}`;
    },
    external() {
      return this.identity && this.identity.external;
    },
    externalTag() {
      return `( ${this.$t('userAvatar.external.label')} )`;
    },
    fullnameStyle() {
      return `${this.boldTitle && 'font-weight-bold ' || ''}${!this.linkStyle && 'text-color' || ''}`;
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    isCurrentUser() {
      return eXo.env.portal.userName !== this.username;
    }
  },
  watch: {
    external() {
      this.isExternal = this.external && Boolean(this.external) || false;
    }
  },
  created() {
    if ( this.popover && !this.isMobile) {
      document.addEventListener('profile-extension-updated', this.refreshExtensions);

      // To broadcast event about current page supporting profile extensions
      document.dispatchEvent(new CustomEvent('profile-extension-init'));
      this.profileActionExtensions = [];
      this.refreshExtensions();
      this.refreshWebCOnferencingExtensions();
    }
  },
  methods: {
    showUserPopover() {
      if ( this.popover && !this.isMobile) {
        const currentUser= $(`#${this.id}`);
        if ( this.avatar && this.popoverLeftPosition) {
          this.y = currentUser[0].offsetLeft + currentUser[0].offsetWidth;
        } else {
          this.y = currentUser[0].offsetLeft;
        }
        this.x = currentUser[0].offsetTop + currentUser[0].offsetHeight;
        this.webConferencingExtensions.map((extension) => {
          if ( !this.$refs[extension.key] ) {
            this.$nextTick().then(() => {
              this.initWebConferencingActionComponent(extension);
            });
          }
        });
        this.displayUserPopover = true;
      }
    },
    hideUserPopover() {
      if ( this.popover && !this.isMobile) {
        this.displayUserPopover = false;
      }
    },
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
      this.profileActionExtensions.sort((a, b) =>  b.order - a.order);
    },
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
