<template>
  <div
    id="profileHeaderActions"
    :class="owner && 'profileHeaderOwnerActions' || 'profileHeaderOtherActions'"
    class="mt-auto mr-3">
    <template v-if="!owner">
      <template>
        <v-btn
          v-for="(extension, i) in enabledProfileActionExtensions"
          :key="i"
          class="btn mx-2"
          @click="extension.click(user)">
          <i :class="extension.icon ? extension.icon : 'hidden'" class="uiIcon" />
          <span class="buttonText">
            {{ extension.title }}
          </span>
        </v-btn>
      </template>
      <div v-if="invited" class="invitationButtons d-inline">
        <v-dialog
          v-model="mobileAcceptRefuseConnectionDialog"
          width="200"
          max-width="100vw">
          <v-card class="pa-0">
            <v-btn
              :disabled="loading"
              :loading="loading"
              block
              class="white no-border-radius"
              @click="acceptToConnect">
              {{ $t('profileHeader.button.acceptToConnect') }}
            </v-btn>
            <v-btn
              :disabled="loading"
              :loading="loading"
              block
              class="white no-border-radius"
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
            <i class="uiIconSocConnectUser"/>
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
          <i class="uiIconSocCancelConnectUser"/>
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
        <i class="uiIconSocCancelConnectUser"/>
        <span class="buttonText">
          {{ $t('profileHeader.button.cancelRequest') }}
        </span>
      </v-btn>
      <v-btn
        v-else-if="disconnected"
        :loading="sendingAction"
        :disabled="sendingAction"
        class="btn btn-primary mx-auto connectUserButton"
        @click="connect">
        <i class="uiIconSocConnectUser"/>
        <span class="buttonText">
          {{ $t('profileHeader.button.connect') }}
        </span>
      </v-btn>
      <div class="profileHeaderActionComponents">
        <div v-for="action in profileHeaderActionComponents" v-if="action.enabled" :key="action.key"
             :class="`${action.appClass} ${action.typeClass}`" :ref="action.key">
          <div v-if="action.component">
            <component v-dynamic-events="action.component.events"
                       v-bind="action.component.props ? action.component.props : {}"
                       :is="action.component.name"></component>
          </div>
          <div v-else-if="action.element" v-html="action.element.outerHTML">
          </div>
          <div v-else-if="action.html" v-html="action.html">
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import {profileHeaderActionComponents} from '../extension.js';

export default {
  props: {
    user: {
      type: Object,
      default: () => null,
    },
    owner: {
      type: Boolean,
      default: () => true,
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
    profileHeaderActionComponents: profileHeaderActionComponents
  }),
  computed: {
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
  },
  mounted() {
    this.initProfileHeaderActionComponents();
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
    initProfileHeaderActionComponents() {
      for (const action of this.profileHeaderActionComponents) {
        if (action.init && action.enabled) {
          let container = this.$refs[action.key];
          if(container && container.length > 0) {
            container = container[0];
          }
          action.init(container, this.user.username);
        }
      }
    }
  },
};
</script>