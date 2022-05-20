<template>
  <v-menu
    v-model="menu"
    rounded="rounded"
    :close-on-content-click="false"
    :left="popoverLeftPosition"
    :position-x="offsetX"
    :position-y="offsetY"
    transition="slide-x-transition"
    absolute
    content-class="profile-popover-menu ps-2 pt-2 transparent"
    elevation="0"
    max-width="350"
    min-width="350"
    max-height="160">
    <v-card 
      elevation="2"   
      class="pa-2"
      max-width="250"
      min-width="250"
      id="user-popover"
      @mouseenter="isMenuHovered = true"
      @mouseleave="closePopover()">
      <v-list-item class="px-0">
        <v-list-item-content class="py-0">
          <v-list-item-title>
            <exo-user-avatar
              :identity="identity"
              :size="46"
              :popover="false"
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
      <div v-if="!isCurrentUser" class="d-flex justify-end">
        <extension-registry-components
          :params="params"
          class="d-flex"
          name="UserPopover"
          type="user-popover-action"
          parent-element="div"
          element="div"
          element-class="mx-auto ma-lg-0" />
        <div
          v-for="extension in enabledExtensionComponents"
          :key="extension.key"
          :class="`${extension.appClass} ${extension.typeClass}`"
          :ref="extension.key">
        </div>
      </div>
    </v-card>
  </v-menu>
</template>

<script>

export default {
  data() {
    return {
      popover: false,
      menu: false,
      id: null,
      avatar: null,
      fullname: null,
      popoverLeftPosition: null,
      url: null,
      size: null,
      position: null,
      offsetX: false,
      offsetY: true,
      externalExtensions: [],
      isCurrentUser: false,
      isMenuHovered: false,
      closePopoverMenu: '',
      popoverDisplayDelay: 2000
    };
  },
  computed: {
    identity() {
      return {
        username: this.username,
        fullname: this.fullname,
        position: this.position,
        avatar: this.avatar,
        external: this.external,
      };
    },
    isMobile() {
      return this.$vuetify.breakpoint.name === 'xs' || this.$vuetify.breakpoint.name === 'sm';
    },
    params() {
      return {
        identityType: 'USER_PROFILE',
        identityId: this.identity && this.identity.username,
      };
    },
    enabledExtensionComponents() {
      return this.externalExtensions.filter(extension => extension.enabled);
    },
    current() {
      return this.isCurrentUser;
    }
  },
  watch: {
    menu() {
      clearTimeout(this.closePopoverMenu);
      // Close user popover when the user goes outside the hovered user popover after two seconds
      this.closePopoverMenu = setTimeout(() => {
        if (this.menu) {
          if (!this.isMenuHovered) {
            this.menu = false;
          }
        }
      }, this.popoverDisplayDelay);
      if (this.menu) {
        this.externalExtensions.map((extension) => {
          this.$nextTick().then(() => {
            this.initExtensionAction(extension);
          });
        });
      }
    }
  },
  mounted() {
    if (!this.isMobile) {
      window.addEventListener('popover-user-display', event => {
        const userIdentity = event?.detail;
        this.id = userIdentity?.id;
        this.username = userIdentity?.username;
        this.fullname = userIdentity?.fullName;
        this.avatar = userIdentity?.avatar;
        this.position = userIdentity?.position;
        this.external = userIdentity?.external;
        this.offsetX = userIdentity?.offsetX;
        this.offsetY = userIdentity?.offsetY;
        this.isCurrentUser = eXo.env.portal.userName === this.username;
        this.menu = true;
        this.refreshExtensions();
      });

      // Force to close user popover when clicking outside
      $(document).on('click', (event) => {
        if (event.target && !$(event.target).parents('#user-popover').length) {
          this.menu = false;
        }
      });
    }
  },
  methods: {
    closePopover() {
      this.isMenuHovered = false;
      setTimeout(() => {
        this.menu = false;
      },500);
    },
    refreshExtensions () {
      this.externalExtensions = extensionRegistry.loadExtensions('user-profile-popover', 'action') || [];
    },
    initExtensionAction(extension) {
      if (extension.enabled ) {
        let container = this.$refs[extension.key];
        if (container?.length) {
          if (container[0]?.childNodes?.length) {
            while (container[0].firstChild) {
              container[0].firstChild.remove();
            }
          }
          container = container[0];
          extension.init(container, this.username);
        }
      }
    },
  }
};
</script>
