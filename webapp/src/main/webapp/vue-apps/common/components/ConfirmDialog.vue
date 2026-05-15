<template>
  <v-dialog
    ref="dialog"
    v-model="dialog"
    :persistent="persistent"
    :width="width"
    :content-class="`uiPopup ${isBrandingLayout && 'layout-drawer' || ''}`"
    max-width="100vw"
    :aria-labelledby="titleId"
    :aria-describedby="messageId"
    role="dialog">
    <v-card class="elevation-12 transparent">
      <div class="ignore-vuetify-classes popupHeader ClearFix" :class="isBrandingLayout && 'layout-drawer' || ''">
        <v-btn
          icon
          class="uiIconClose pull-right ignore-vuetify-classes"
          :aria-label="$t('label.close')"
          @click="close">
          <v-icon size="20">fa-times</v-icon>
        </v-btn>
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
          :disabled="loading"
          :loading="loading"
          class="ignore-vuetify-classes btn btn-primary me-2"
          @click="ok">
          {{ okLabel }}
        </button>
        <button
          v-if="cancelLabel"
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
    previousFocus: null,
  }),
  computed: {
    titleId() {
      return `confirm-dialog-title-${this._uid}`;
    },
    messageId() {
      return `confirm-dialog-message-${this._uid}`;
    }
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.previousFocus = document.activeElement;
        this.closed = false;
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.emitClosedEvent();
      }
    },
  },
  methods: {
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
        if (this.previousFocus) {
          this.previousFocus.focus();
          this.previousFocus = null;
        }
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