<template>
  <v-card
    id="profileHeaderActions"
    class="profileHeaderOtherActions d-flex"
    max-height="70"
    flat
    tile>
    <div class="d-flex align-center justify-end flex-wrap my-auto">
      <span
        v-for="(extension, i) in enabledProfileActionExtensions"
        :key="i">
        <v-btn
          v-if="!extension.init"
          :icon="iconButton"
          :class="{'no-border': iconButton}"
          class="btn my-auto ma-1 mb-0"
          @click="extension.click(user)">
          <v-icon
            v-if="extension.icon"
            class="ma-auto"
            color="primary"
            size="16">
            {{ extension.icon }}
          </v-icon>
          <span
            v-if="!iconButton"
            class="ms-1">
            {{ extension.title }}
          </span>
        </v-btn>
      </span>
      <div v-if="invited" class="invitationButtons d-inline">
        <v-dialog
          v-model="mobileAcceptRefuseConnectionDialog"
          content-class="border-box-sizing width-auto"
          width="auto">
          <v-card color="white" class="d-flex flex-column pa-0">
            <v-btn
              :disabled="sendingAction"
              :loading="sendingAction"
              class="white no-border-radius success--text"
              block
              @click="acceptToConnect">
              {{ $t('profileHeader.button.acceptToConnect') }}
            </v-btn>
            <v-btn
              :disabled="sendingAction"
              :loading="sendingAction"
              class="white no-border-radius error--text"
              block
              outlined
              @click="refuseToConnect">
              {{ $t('profileHeader.button.refuseToConnect') }}
            </v-btn>
          </v-card>
        </v-dialog>
        <div class="acceptToConnectButtonParent">
          <profile-header-relation-button
            :loading="sendingAction"
            :disabled="sendingAction"
            :icon-button="iconButton"
            :extra-button-class="`${!iconButton && 'btn-primary'} btn no-border-radius`"
            :label="$t('profileHeader.button.acceptToConnect')"
            icon="fas fa-user-plus"
            @click="acceptToConnect" />
          <v-btn
            class="btn btn-primary peopleButtonMenu dropdownButton"
            @click="openSecondButton">
            <v-icon>mdi-menu-down</v-icon>
          </v-btn>
          <v-btn
            class="btn btn-primary peopleButtonMenu dialogButton"
            @click="openSecondButton(true)">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </div>
        <profile-header-relation-button
          v-show="displaySecondButton"
          :loading="sendingSecondAction"
          :disabled="sendingSecondAction"
          :icon-button="iconButton"
          extra-button-class="btn refuseToConnectButton no-border-radius"
          :label="$t('profileHeader.button.refuseToConnect')"
          icon="fas fa-user-minus"
          @click="refuseToConnect" />
      </div>
      <profile-header-relation-button
        v-else-if="requested"
        :loading="sendingAction"
        :disabled="sendingAction"
        :icon-button="iconButton"
        :extra-button-class="`${!iconButton && 'btn-primary'} btn cancelRequestButton`"
        :label="$t('profileHeader.button.cancelRequest')"
        icon="fas fa-user-minus"
        @click="cancelRequest" />
      <profile-header-relation-button
        v-else-if="connected"
        :loading="sendingAction"
        :disabled="sendingAction"
        :icon-button="iconButton"
        extra-button-class="error-color border-color disconnectButton no-border-radius"
        extra-icon-class="error-color"
        extra-text-class="error-color"
        :label="$t('profileHeader.button.disconnect')"
        icon="fas fa-user-minus"
        outlined
        @click="disconnect" />
      <profile-header-relation-button
        v-else-if="disconnected"
        :loading="sendingAction"
        :disabled="sendingAction"
        :icon-button="iconButton"
        :extra-button-class="`${!iconButton && 'btn-primary'} btn connectUserButton`"
        :label="$t('profileHeader.button.connect')"
        icon="fas fa-user-plus"
        @click="connect" />
      <div class="profileHeaderActionComponents order-first mb-0">
        <div
          v-for="action in enabledProfileHeaderActionComponents"
          :key="action.key"
          :class="actionClass(action)"
          :ref="action.key">
          <div v-if="action.component">
            <component
              v-dynamic-events="action.component.events"
              v-bind="action.component.props ? action.component.props : {}"
              :is="action.component.name" />
          </div>
          <div v-else-if="action.element" v-html="action.element.outerHTML">
          </div>
          <div v-else-if="action.html" v-html="action.html">
          </div>
          {{ initTitleActionComponent(action) }}
        </div>
      </div>
    </div>
  </v-card>
</template>

<script>
import {profileHeaderActionComponents} from '../extension.js';

export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    hover: {
      type: Boolean,
      default: () => false,
    },
  },
  data: () => ({
    profileActionExtensions: [],
    mobileAcceptRefuseConnectionDialog: false,
    sendingAction: false,
    sendingSecondAction: false,
    displaySecondButton: false,
    waitTimeUntilCloseMenu: 200,
    profileHeaderActionComponents: profileHeaderActionComponents,
    isMounted: null,
    resolveMounting: null
  }),
  computed: {
    iconButton() {
      return this.$vuetify.breakpoint.width < this.$vuetify.breakpoint.thresholds.lg;
    },
    isMobile() {
      return this.$vuetify?.breakpoint?.mobile;
    },
    enabledProfileHeaderActionComponents() {
      return this.profileHeaderActionComponents && this.profileHeaderActionComponents.filter(act => act.enabled) || [];
    },
    relationshipStatus() {
      return this.user && this.user.relationshipStatus;
    },
    connected() {
      return this.relationshipStatus === 'CONFIRMED';
    },
    disconnected() {
      return !this.relationshipStatus || this.relationshipStatus === 'IGNORED';
    },
    invited() {
      return this.relationshipStatus === 'INCOMING';
    },
    requested() {
      return this.relationshipStatus === 'OUTGOING';
    },
    enabledProfileActionExtensions() {
      if (!this.profileActionExtensions || !this.user) {
        return [];
      }
      return this.profileActionExtensions.slice().filter(extension => extension.enabled(this.user));
    },
  },
  created() {
    // To refresh menu when a new extension is ready to be used
    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.refreshExtensions();
    document.addEventListener('mousedown', () => {
      if (window.displaySecondButton) {
        setTimeout(() => {
          window.displaySecondButton = false;
        }, window.waitTimeUntilCloseMenu);
      }
    });

    const thevue = this;
    this.isMounted = new Promise(function(resolve) {
      thevue.resolveMounting = resolve;
    });
  },
  mounted() {
    this.resolveMounting();
  },
  methods: {
    refreshExtensions() {
      this.profileActionExtensions = extensionRegistry.loadExtensions('profile-extension', 'action') || [];
    },
    openSecondButton(openDialog) {
      if (openDialog) {
        this.mobileAcceptRefuseConnectionDialog = true;
      } else {
        this.displaySecondButton = !this.displaySecondButton;
      }
    },
    connect() {
      this.sendingAction = true;
      this.$userService.connect(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    acceptToConnect() {
      this.sendingAction = true;
      this.$userService.confirm(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    refuseToConnect() {
      this.sendingSecondAction = true;
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingSecondAction = false;
        });
    },
    cancelRequest() {
      this.sendingAction = true;
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    disconnect() {
      this.sendingAction = true;
      this.$userService.deleteRelationship(this.user.username)
        .then(() => this.$emit('refresh'))
        .catch((e) => {
          // eslint-disable-next-line no-console
          console.error('Error processing action', e);
        })
        .finally(() => {
          this.sendingAction = false;
        });
    },
    initTitleActionComponent(action) {
      const thevue = this;
      if (action.init && !action.isStartedInit && action.enabled && this.user) {
        action.isStartedInit = true;
        this.isMounted.then(() => {
          let container = this.$refs[action.key];
          if (container && container.length > 0) {
            container = container[0];
            action.init(container, thevue.user.username);
          } else {
            // eslint-disable-next-line no-console
            console.error(`Error initialization of the ${action.key} action component: empty container`);
          }
        });
      }
    },
    actionClass(action) {
      return this.iconButton && action.mobileClass ? `${action.appClass} ${action.typeClass} ${action.mobileClass}` : `${action.appClass} ${action.typeClass}`;
    },
  },
};
</script>
