<template>
  <v-card
    id="profileHeaderActions"
    class="profileHeaderOtherActions d-flex"
    max-height="70"
    flat
    tile>
    <div class="d-flex justify-end flex-wrap">
      <span
        v-for="(extension, i) in enabledProfileActionExtensions"
        :key="i"
        :rank="extension.rank">
        <component
          v-if="extension.vueComponent"
          :is="extension.vueComponent"
          :user="user"
          compact-display />
        <v-btn
          v-else-if="extension?.isLink"
          :href="extension.href(user)"
          :aria-label="$t(extension.titleKey)"
          :title="extension.title || $t(extension.titleKey)"
          :class="{'ms-2': lgAndUp, 'ms-0': !lgAndUp}"
          class="my-auto mb-0"
          icon>
          <v-icon
            :size="iconSize"
            class="primary--text ma-1">
            {{ extension.icon }}
          </v-icon>
        </v-btn>
        <v-btn
          v-else-if="!extension.init"
          :class="{'ms-2': lgAndUp, 'ms-0': !lgAndUp}"
          class="no-border my-auto mb-0"
          icon
          @click="extension.click(user)">
          <v-icon
            v-if="extension.icon"
            :size="iconSize"
            class="ma-1"
            color="primary">
            {{ extension.icon }}
          </v-icon>
        </v-btn>
      </span>
      <div v-if="invited" class="invitationButtons d-inline">
        <v-dialog
          v-model="dialog"
          content-class="border-box-sizing width-auto"
          width="auto"
          @click:outside="dialog = false">
          <v-card color="white" class="d-flex flex-column card-border-radius pa-0">
            <v-btn
              :disabled="sendingAction"
              :loading="sendingAction"
              class="white no-border-radius"
              block
              @click="acceptToConnect">
              <v-icon
                size="14"
                class="success--text me-2">
                fas fa-check
              </v-icon>
              {{ $t('profileHeader.button.acceptToConnect') }}
            </v-btn>
            <v-btn
              :disabled="sendingAction"
              :loading="sendingAction"
              class="white no-border-radius"
              block
              outlined
              @click="refuseToConnect">
              <v-icon
                size="14"
                class="error--text me-2">
                fas fa-times
              </v-icon>
              {{ $t('profileHeader.button.refuseToConnect') }}
            </v-btn>
          </v-card>
        </v-dialog>
        <div class="acceptToConnectButtonParent">
          <profile-header-relation-button
            :loading="sendingAction"
            :disabled="sendingAction"
            :icon-size="iconSize"
            extra-icon-class="orange--text"
            extra-button-class="btn no-border-radius"
            :label="$t('profileHeader.button.acceptToConnect')"
            icon="fas fa-question"
            icon-button
            @click="dialog = true" />
        </div>
      </div>
      <profile-header-relation-button
        v-else-if="requested"
        :loading="sendingAction"
        :disabled="sendingAction"
        :icon-size="iconSize"
        extra-icon-class="error-color"
        extra-button-class="btn cancelRequestButton"
        :label="$t('profileHeader.button.cancelRequest')"
        icon="fas fa-user-minus"
        icon-button
        @click="cancelRequest" />
      <profile-header-relation-button
        v-else-if="connected"
        :loading="sendingAction"
        :disabled="sendingAction"
        :icon-size="iconSize"
        extra-button-class="error-color disconnectButton no-border-radius"
        extra-icon-class="error-color"
        :label="$t('profileHeader.button.disconnect')"
        icon="fas fa-user-minus"
        icon-button
        outlined
        @click="disconnect" />
      <profile-header-relation-button
        v-else-if="disconnected"
        :loading="sendingAction"
        :disabled="sendingAction"
        :icon-size="iconSize"
        extra-icon-class="primary--text"
        extra-button-class="btn connectUserButton"
        :label="$t('profileHeader.button.connect')"
        icon="fas fa-user-plus"
        icon-button
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
    dialog: false,
    sendingAction: false,
    sendingSecondAction: false,
    waitTimeUntilCloseMenu: 200,
    profileHeaderActionComponents: profileHeaderActionComponents,
    isMounted: null,
    resolveMounting: null
  }),
  computed: {
    lgAndUp () {
      return this.$vuetify.breakpoint.width >= this.$vuetify.breakpoint.thresholds.lg;
    },
    iconSize() {
      return this.$vuetify.breakpoint.width < this.$vuetify.breakpoint.thresholds.lg && 16 || 20;
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
      return  this.profileActionExtensions.filter(extension => extension.enabled(this.user))
        .sort((a, b) => (a.order ?? 100) - (b.order ?? 100));

    },
  },
  created() {
    // To refresh menu when a new extension is ready to be used
    document.addEventListener('extension-profile-extension-action-updated', this.refreshExtensions);
    this.refreshExtensions();
    document.addEventListener('mousedown', () => {
      if (this.dialog) {
        setTimeout(() => {
          this.dialog = false;
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
      return `${action.appClass} ${action.typeClass} ${action.mobileClass} mx-0`;
    },
  },
};
</script>
