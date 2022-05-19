<template>
  <v-menu
    v-model="menu"
    rounded="rounded"
    :close-on-content-click="false"
    :left="popoverLeftPosition"
    :offset-x="offsetX"
    :offset-y="offsetY"
    absolute
    content-class="profile-popover-menu overflow-y-hidden white"
    max-width="250"
    min-width="250">
    <v-card elevation="0" class="pa-2">
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
      <div v-if="!isCurrentUser" class="d-flex justify-end">
        <extension-registry-components
          :params="params"
          class="d-flex"
          name="UserPopover"
          type="user-popover-action"
          parent-element="div"
          element="div"
          element-class="mx-auto ma-lg-0" />
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
    isCurrentUser() {
      return eXo.env.portal.userName === this.username;
    },
    params() {
      return {
        identityType: 'USER_PROFILE',
        identityId: this.identity && this.identity.username,
      };
    },
  },
  mounted() {
    if (!this.isMobile) {
      window.addEventListener('popover-user-display', event => {
        console.warn('popover-user-display', event);

        const userIdentity = event?.detail;
        this.id = userIdentity?.id;
        this.username = userIdentity?.username;
        this.fullname = userIdentity?.fullName;
        this.avatar = userIdentity?.avatar;
        this.position = userIdentity?.position;
        this.external = userIdentity?.external;
        this.offsetX = userIdentity?.offsetX;
        this.offsetY = userIdentity?.offsetY;

        this.menu = true;
      });
    }
  },
};
</script>
