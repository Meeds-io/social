<template>
  <v-dialog
    ref="dialog"
    v-model="dialog"
    :persistent="persistent"
    :width="width"
    :content-class="`uiPopup ${isBrandingLayout && 'layout-drawer' || ''} ${contentClass || ''}`"
    max-width="100vw">
    <v-card
      ref="card"
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
        <span
          :id="titleId"
          class="ignore-vuetify-classes text-title">{{ title }}</span>
      </div>
      <v-card-text :id="messageId" v-sanitized-html="message" />
      <v-card-actions v-if="!hideActions">
        <v-spacer />
        <button
          v-if="okLabel"
          ref="okButton"
          :disabled="loading"
          :loading="loading"
          class="confirmDialogOkButton ignore-vuetify-classes btn btn-primary me-2"
          @click="ok">
          {{ okLabel }}
        </button>
        <button
          v-if="cancelLabel"
          ref="cancelButton"
          :disabled="loading"
          :loading="loading"
          class="confirmDialogCancelButton ignore-vuetify-classes btn ms-2"
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
    contentClass: {
      type: String,
      default: null,
    },
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
    confirmed: false,
  }),
  computed: {
    titleId() {
      return `confirmDialogTitle${this._uid}`;
    },
    messageId() {
      return `confirmDialogMessage${this._uid}`;
    },
  },
  created() {
    // the focus contract the platform's confirmation dialogs share: see common/js/DialogFocus.js
    this.dialogFocus = this.$dialogFocus.create({
      content: () => this.$refs.dialog?.$refs?.content || this.$refs.card?.$el,
      defaultButton: () => this.$refs.cancelButton || this.$refs.okButton,
    });
  },
  beforeDestroy() {
    this.dialogFocus.destroy();
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.dialogFocus.open();
        this.confirmed = false;
        this.closed = false;
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.dialogFocus.close({confirmed: this.confirmed});
        this.emitClosedEvent();
      }
    },
  },
  methods: {
    returnFocusTo(element) {
      // once closed, the dialog gives the focus to this element instead of its opener (e.g. a side panel the
      // confirmation closes gives it to the panel's own opener, its control that asked being hidden)
      this.dialogFocus.returnFocusTo(element);
    },
    ok(event) {
      if (event) {
        event.preventDefault();
        event.stopPropagation();
      }

      this.confirmed = true;
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
      // read during the click: a menu that closes on this click resets its button's aria-expanded before the dialog watcher runs
      this.dialogFocus.capture();
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