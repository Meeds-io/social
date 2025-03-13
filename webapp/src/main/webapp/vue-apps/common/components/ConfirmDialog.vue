<template>
  <v-dialog
    ref="dialog"
    v-model="dialog"
    :content-class="`uiPopup ${isBrandingLayout && 'layout-drawer' || ''}`"
    max-width="100vw"
    :persistent="persistent"
    :width="width">
    <v-card class="elevation-12 transparent">
      <div
        class="ignore-vuetify-classes popupHeader ClearFix"
        :class="isBrandingLayout && 'layout-drawer' || ''">
        <a
          aria-hidden="true"
          class="uiIconClose pull-right"
          @click="close"></a>
        <span
          class="ignore-vuetify-classes text-title"
          v-html="title"></span><!-- eslint-disable vue/no-v-text-v-html-on-component -->
      </div>
      <v-card-text v-html="message" /><!-- eslint-disable vue/no-v-text-v-html-on-component -->
      <v-card-actions v-if="!hideActions">
        <v-spacer />
        <button
          v-if="okLabel"
          class="ignore-vuetify-classes btn btn-primary me-2"
          :disabled="loading"
          :loading="loading"
          @click="ok">
          {{ okLabel }}
        </button>
        <button
          v-if="cancelLabel"
          class="ignore-vuetify-classes btn ms-2"
          :disabled="loading"
          :loading="loading"
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
        default () {
          return false;
        },
      },
      persistent: {
        type: Boolean,
        default () {
          return false;
        },
      },
      title: {
        type: String,
        default () {
          return null;
        },
      },
      message: {
        type: String,
        default () {
          return null;
        },
      },
      okLabel: {
        type: String,
        default () {
          return null;
        },
      },
      cancelLabel: {
        type: String,
        default () {
          return null;
        },
      },
      hideActions: {
        type: Boolean,
        default () {
          return false;
        },
      },
      width: {
        type: String,
        default () {
          return '400px';
        },
      },
      isBrandingLayout: {
        type: Boolean,
        default: true,
      },
    },
    data: () => ({
      dialog: false,
      closed: false,
    }),
    watch: {
      dialog () {
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
      ok (event) {
        if (event) {
          event.preventDefault();
          event.stopPropagation();
        }

        this.$emit('ok');
        this.close(event);
      },
      close (event) {
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
      open () {
        this.dialog = true;
        this.$emit('opened');
        this.$nextTick(() => this.dialog = true);
      },
      emitClosedEvent () {
        if (!this.closed && !this.dialog) {
          this.closed = true;
          this.$emit('dialog-closed');
          document.dispatchEvent(new CustomEvent('modalClosed'));
        }
      },
    },
  };
</script>