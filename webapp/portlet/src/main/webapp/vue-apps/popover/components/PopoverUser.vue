<template>
  <v-menu
    v-model="menu"
    rounded="rounded"
    :close-on-content-click="false"
    :position-x="offsetX"
    :position-y="offsetY"
    transition="slide-x-transition"
    absolute
    top
    content-class="profile-popover-menu pa-1 transparent"
    elevation="0"
    max-width="350"
    min-width="350">
    <v-card 
      elevation="2"   
      class="pa-2"
      max-width="250"
      min-width="250"
      id="user-popover"
      @mouseenter="setPopoverHovered()"
      @mouseleave="setPopoverNotHovered()">
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
      popoverLeftPosition: null,
      url: null,
      size: null,
      offsetX: false,
      offsetY: true,
      externalExtensions: [],
      isMenuHovered: false,
      isActivatorHovered: false,
      element: null,
      popoverCloseDelay: 1000,
      popoverOpenDelay: 300,
      menuCloseTimer: null,
      menuOpenTimer: null,
      identity: null,
    };
  },
  computed: {
    params() {
      return {
        identityType: 'USER_PROFILE',
        identityId: this.identity && this.identity.username,
        identityEnabled: this.identity && this.identity.enabled,
        identityDeleted: this.identity && this.identity.deleted,        
      };
    },
    enabledExtensionComponents() {
      return this.externalExtensions.filter(extension => extension.enabled);
    },
    username() {
      return this.identity?.username;
    },
    fullname() {
      return this.identity?.fullname;
    },
    avatar() {
      return this.identity?.avatar;
    },
    position() {
      return this.identity?.position;
    },
    external() {
      return this.identity?.external;
    },
    isCurrentUser() {
      return eXo.env.portal.userName === this.username;
    },
  },
  watch: {
    menu() {
      if (this.menu) {
        this.clearCloseTime();
        this.registerActivatorElementEvents();
        document.dispatchEvent(new CustomEvent('UserPopoverOpened'));
      } else {
        this.unregisterActivatorElementEvents();
      }
    },
    username(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.refreshExtensions();
      }
    },
  },
  created() {
    window.addEventListener('popover-user-display', event => {
      const data = event?.detail;
      this.identity = {
        id: data?.id,
        enabled: data?.enabled,
        deleted: data?.deleted,        
        username: data?.username,
        fullname: data?.fullName,
        avatar: data?.avatar,
        position: data?.position,
        external: data?.external,
      };
      if (this.menu && this.element === data?.element) {
        this.clearCloseTime();
      } else {
        this.unregisterActivatorElementEvents();
        this.$nextTick().then(() => {
          this.offsetX = data?.offsetX;
          this.offsetY = data?.offsetY;
          this.element = data?.element;
          this.setActivatorHovered();
          this.registerActivatorElementEvents();
          this.setPopoverNotHovered();
          this.openPopover();
        });
      }
    });
    document.addEventListener('drawerOpened', () => this.closePopover(true));
    document.addEventListener('drawerClosed', () => this.closePopover(true));
    document.addEventListener('modalOpened', () => this.closePopover(true));
    document.addEventListener('modalClosed', () => this.closePopover(true));
    document.addEventListener('SpacePopoverOpened', () => this.closePopover(true));
  },
  mounted() {
    // Force to close user popover when clicking outside
    $(document).on('click', (event) => {
      if (this.menu && event.target && !$(event.target).parents('#user-popover').length) {
        this.closePopover(true);
      }
      if (this.menu) {
        // workaround for menu v-select absolute content that must be displayed inside the menu
        $('.profile-popover-menu').css('height', '160px');
      }
    });
  },
  methods: {
    refreshExtensions() {
      this.externalExtensions = [];
      this.$nextTick(() => {
        this.externalExtensions = extensionRegistry.loadExtensions('user-profile-popover', 'action') || [];
        this.$nextTick().then(() => this.externalExtensions.forEach(this.initExtensionAction));
      });
      // workaround for menu v-select absolute content that must be displayed inside the menu
      $('.profile-popover-menu').css('height', 'auto');
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
    registerActivatorElementEvents() {
      if (this.element) {
        $(this.element)
          .on('mouseenter', this.setActivatorHovered)
          .on('mouseleave', this.setActivatorNotHovered);
      }
    },
    unregisterActivatorElementEvents() {
      if (this.element) {
        $(this.element)
          .off('mouseenter', this.setActivatorHovered)
          .off('mouseleave', this.setActivatorNotHovered);
        this.setActivatorNotHovered();
        this.element = null;
      }
    },
    setActivatorHovered() {
      this.isActivatorHovered = true;
    },
    setActivatorNotHovered() {
      this.isActivatorHovered = false;
      this.closePopover();
    },
    setPopoverHovered() {
      this.isMenuHovered = true;
    },
    setPopoverNotHovered() {
      this.isMenuHovered = false;
      this.closePopover();
    },
    openPopover(immediatly) {
      if (!this.menu) {
        if (immediatly) {
          this.menu = true;
        } else {
          this.menuOpenTimer = window.setTimeout(() => {
            if (this.isActivatorHovered) {
              this.menu = true;
            }
          }, this.popoverOpenDelay);
        }
      }
    },
    closePopover(immediatly) {
      if (this.menu) {
        if (immediatly) {
          this.menu = false;
        } else {
          this.menuCloseTimer = window.setTimeout(() => {
            if (!this.isMenuHovered && !this.isActivatorHovered) {
              this.menu = false;
            }
          }, this.popoverCloseDelay);
        }
      }
    },
    clearCloseTime() {
      if (this.menuCloseTimer) {
        window.clearTimeout(this.menuCloseTimer);
        this.menuCloseTimer = null;
      }
    },
  }
};
</script>
