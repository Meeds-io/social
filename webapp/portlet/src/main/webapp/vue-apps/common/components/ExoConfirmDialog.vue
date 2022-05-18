<template>
  <v-dialog
    ref="dialog"
    v-model="dialog"
    :persistent="persistent"
    :width="width"
    content-class="uiPopup"
    max-width="100vw">
    <v-card class="elevation-12">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <a
          class="uiIconClose pull-right"
          aria-hidden="true"
          @click="close"></a>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <span class="ignore-vuetify-classes PopupTitle popupTitle text-truncate" v-html="title"></span>
      </div>
      <!-- eslint-disable-next-line vue/no-v-html -->
      <v-card-text v-html="message" />
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
  },
  data: () => ({
    dialog: false,
    closed: false,
  }),
  watch: {
    dialog() {
      if (this.dialog) {
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