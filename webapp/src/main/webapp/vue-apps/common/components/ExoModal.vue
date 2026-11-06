<template>
  <v-dialog
    v-model="dialog"
    :width="width"
    :persistent="persistent"
    content-class="uiPopup"
    max-width="100vw">
    <v-card
      ref="card"
      :aria-labelledby="titleId"
      class="elevation-12"
      role="dialog"
      aria-modal="true">
      <div class="ignore-vuetify-classes popupHeader ClearFix">
        <button
          :aria-label="$t('label.close')"
          type="button"
          class="uiIconClose pull-right"
          style="border:0;padding:0;background-color:transparent;cursor:pointer"
          @click="close"></button>
        <!-- eslint-disable-next-line vue/no-v-html -->
        <span :id="titleId" class="ignore-vuetify-classes PopupTitle popupTitle text-truncate" v-html="title"></span>
      </div>
      <slot></slot>
      <v-card-actions v-if="!hideActions">
        <v-spacer />
        <button
          v-if="okLabel"
          ref="okButton"
          :disabled="loading"
          :loading="loading"
          class="ignore-vuetify-classes btn btn-primary me-2"
          @click="close">
          {{ okLabel }}
        </button>
        <v-spacer />
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: ''
    },
    loading: {
      type: Boolean,
      default: function() {
        return false;
      },
    },
    okLabel: {
      type: String,
      default: function() {
        return 'ok';
      },
    },
    hideActions: {
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
    width: {
      type: String,
      default: function() {
        return '500px';
      },
    },
  },
  data: () => ({
    dialog: false,
    opener: null,
  }),
  computed: {
    titleId() {
      return `exoModalTitle${this._uid}`;
    },
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.opener = document.activeElement;
        this.focusFirstControl();
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        this.restoreFocus();
        this.$emit('dialog-closed');
        document.dispatchEvent(new CustomEvent('modalClosed'));
      }
    }
  },
  created() {
    document.addEventListener('keydown', this.closeOnEscape);
  },
  mounted() {
    if (this.$el.closest('.layout-sticky-application')) {
      document.querySelector('#vuetify-apps').appendChild(this.$el);
    }
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.closeOnEscape);
  },
  methods: {
    open() {
      this.dialog = true;
    },
    close() {
      this.dialog = false;
    },
    closeOnEscape(event) {
      if (event.key === 'Escape') {
        this.close();
      }
    },
    // after Vuetify has moved the focus to the dialog content
    focusFirstControl() {
      window.setTimeout(() => {
        if (this.dialog) {
          const control = this.$refs.card?.$el?.querySelector('input, select, textarea, a[href], [tabindex="0"], .v-card__actions button')
            || this.$refs.okButton;
          control?.focus();
        }
      }, 100);
    },
    restoreFocus() {
      const opener = this.opener;
      this.opener = null;
      window.setTimeout(() => {
        if (opener && document.body.contains(opener)) {
          opener.focus();
        }
      }, 50);
    },
  }
};
</script>
