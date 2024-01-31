<template>
  <v-card
    id="profileHeaderActions"
    class="profileHeaderOtherActions d-flex"
    max-height="70"
    flat
    tile>
    <div class="d-flex justify-end flex-wrap my-auto">
      <v-btn
        v-for="(extension, i) in enabledProfileActionExtensions"
        :key="i"
        class="btn ma-2 mb-0"
        @click="extension.click(user)">
        <i :class="extension.icon ? extension.icon : 'hidden'" class="uiIcon"></i>
        <span class="buttonText">
          {{ extension.title }}
        </span>
      </v-btn>
      <div v-if="invited" class="invitationButtons d-inline">
        <v-dialog
          v-model="mobileAcceptRefuseConnectionDialog"
          content-class="border-box-sizing width-auto"
          width="auto">
          <v-card color="white" class="d-flex flex-column pa-0">
            <v-btn
              :disabled="loading"
              :loading="loading"
              class="white no-border-radius success--text"
              block
              @click="acceptToConnect">
              {{ $t('profileHeader.button.acceptToConnect') }}
            </v-btn>
            <v-btn
              :disabled="loading"
              :loading="loading"
              class="white no-border-radius error--text"
              block
              outlined
              @click="refuseToConnect">
              {{ $t('profileHeader.button.refuseToConnect') }}
            </v-btn>
          </v-card>
        </v-dialog>
        <div class="acceptToConnectButtonParent">
          <v-btn
            :loading="sendingAction"
            :disabled="sendingAction"
            class="btn btn-primary mx-auto acceptToConnectButton"
            @click="acceptToConnect">
            <i class="uiIconSocConnectUser"></i>
            <span class="buttonText">
              {{ $t('profileHeader.button.acceptToConnect') }}
            </span>
          </v-btn>
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
        <v-btn
          v-show="displaySecondButton"
          :loading="sendingSecondAction"
          :disabled="sendingSecondAction"
          class="btn mx-auto refuseToConnectButton"
          @click="refuseToConnect">
          <i class="uiIconSocCancelConnectUser"></i>
          <span class="buttonText">
            {{ $t('profileHeader.button.refuseToConnect') }}
          </span>
        </v-btn>
      </div>
      <v-btn
        v-else-if="requested"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn btn-primary mx-auto cancelRequestButton"
        @click="cancelRequest">
        <i class="uiIconSocCancelConnectUser"></i>
        <span class="buttonText">
          {{ $t('profileHeader.button.cancelRequest') }}
        </span>
      </v-btn>
      <v-btn
        v-else-if="disconnected"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn ma-1 mb-0 btn-primary connectUserButton"
        @click="connect">
        <i class="uiIconSocConnectUser"></i>
        <span class="buttonText">
          {{ $t('profileHeader.button.connect') }}
        </span>
      </v-btn>
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
    isMobile() {
      return this.$vuetify && this.$vuetify.breakpoint && this.$vuetify.breakpoint.name === 'xs';
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
    document.addEventListener('profile-extension-updated', this.refreshExtensions);

    // To broadcast event about current page supporting profile extensions
    document.dispatchEvent(new CustomEvent('profile-extension-init'));

    this.refreshExtensions();

    $(document).on('mousedown', () => {
      if (this.displaySecondButton) {
        window.setTimeout(() => {
          this.displaySecondButton = false;
        }, this.waitTimeUntilCloseMenu);
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
      return this.isMobile && action.mobileClass ? `${action.appClass} ${action.typeClass} ${action.mobileClass}` : `${action.appClass} ${action.typeClass}`;
    },
  },
};
</script>
