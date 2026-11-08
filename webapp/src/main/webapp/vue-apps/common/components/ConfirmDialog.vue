<template>
  <v-dialog
    ref="dialog"
    v-model="dialog"
    :persistent="persistent"
    :width="width"
    :content-class="`uiPopup ${isBrandingLayout && 'layout-drawer' || ''}`"
    max-width="100vw">
    <v-card
      class="elevation-12 transparent"
      role="alertdialog"
      aria-modal="true"
      :aria-labelledby="titleId"
      :aria-describedby="messageId">
      <div class="ignore-vuetify-classes popupHeader ClearFix" :class="isBrandingLayout && 'layout-drawer' || ''">
        <button
          type="button"
          class="uiIconClose pull-right no-border pa-0 transparent clickable"
          :aria-label="$t('label.close')"
          @click="close"></button>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <span
          :id="titleId"
          class="ignore-vuetify-classes text-title"
          v-html="title"></span>
      </div>
      <v-card-text :id="messageId" v-sanitized-html="message" />
      <v-card-actions v-if="!hideActions">
        <v-spacer />
        <button
          v-if="okLabel"
          ref="okButton"
          :disabled="loading"
          :loading="loading"
          class="ignore-vuetify-classes btn btn-primary me-2"
          @click="ok">
          {{ okLabel }}
        </button>
        <button
          v-if="cancelLabel"
          ref="cancelButton"
          :disabled="loading"
          :loading="loading"
          class="ignore-vuetify-classes btn ms-2"
          @click="close">
          {{ cancelLabel }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    loading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    persistent: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    title: {
      type: String,
      default: function() {
        return null;
      },
    },
    message: {
      type: String,
      default: function() {
        return null;
      },
    },
    okLabel: {
      type: String,
      default: function() {
        return null;
      },
    },
    cancelLabel: {
      type: String,
      default: function() {
        return null;
      },
    },
    hideActions: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    width: {
      type: String,
      default: function() {
        return '400px';
      },
    },
    isBrandingLayout: {
      type: Boolean,
      default: true
    }
  },
  data: () => ({
    dialog: false,
    closed: false,
    opener: null,
  }),
  computed: {
    titleId() {
      return `confirmDialogTitle${this._uid}`;
    },
    messageId() {
      return `confirmDialogMessage${this._uid}`;
    },
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.opener = document.activeElement;
        this.focusDefaultButton();
        this.closed = false;
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.restoreFocus();
        this.emitClosedEvent();
      }
    },
  },
  methods: {
    focusDefaultButton() {
      // v-dialog moves the focus to its own container after the next tick
      window.setTimeout(() => {
        if (this.dialog) {
          const button = this.$refs.cancelButton || this.$refs.okButton;
          button?.focus?.();
        }
      }, 100);
    },
    restoreFocus() {
      const opener = this.opener;
      this.opener = null;
      window.setTimeout(() => {
        // only when the focus was not moved elsewhere meanwhile (e.g. by a side panel the dialog closed),
        // and back to an opener still shown
        const active = document.activeElement;
        const focusLost = !active || active === document.body || active.closest?.('.v-dialog');
        if (focusLost && opener && document.body.contains(opener) && opener.getClientRects().length) {
          opener.focus?.();
        }
      }, 50);
    },
    ok(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.$emit('ok');
      this.close(event);
    },
    close(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.$emit('closed');
      this.$nextTick(() => {
        this.dialog = false;
        this.emitClosedEvent();
      });
    },
    open() {
      this.dialog = true;
      this.$emit('opened');
      this.$nextTick(() => this.dialog = true);
    },
    emitClosedEvent() {
      if (!this.closed && !this.dialog) {
        this.closed = true;
        this.$emit('dialog-closed');
        document.dispatchEvent(new CustomEvent('modalClosed'));
      }
    },
  },
};
</script>