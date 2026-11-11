<template>
  <v-dialog
    ref="dialog"
    v-model="dialog"
    :persistent="persistent"
    :width="width"
    :content-class="`uiPopup ${isBrandingLayout && 'layout-drawer' || ''}`"
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
const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), input:not([disabled]):not([type="hidden"]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';
const LIST_ITEM_SELECTOR = 'li, [role="listitem"], article, [role="article"]';
const LIST_SELECTOR = 'ul, ol, [role="list"], [role="feed"]';
const OPENER_FOLLOW_DELAY = 10000;

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
    openerContext: null,
  }),
  computed: {
    titleId() {
      return `confirmDialogTitle${this._uid}`;
    },
    messageId() {
      return `confirmDialogMessage${this._uid}`;
    },
  },
  beforeDestroy() {
    window.removeEventListener('focusin', this.redirectContainerFocus, true);
  },
  watch: {
    dialog() {
      if (this.dialog) {
        this.opener = this.focusOrigin(document.activeElement);
        this.openerContext = this.captureOpenerContext(this.opener);
        window.addEventListener('focusin', this.redirectContainerFocus, true);
        this.focusDefaultButton();
        this.closed = false;
        this.$emit('dialog-opened');
        document.dispatchEvent(new CustomEvent('modalOpened'));
      } else {
        window.removeEventListener('focusin', this.redirectContainerFocus, true);
        this.restoreFocus();
        this.emitClosedEvent();
      }
    },
  },
  methods: {
    focusDefaultButton() {
      // fallback when v-dialog does not focus its own container
      window.setTimeout(() => {
        if (this.dialog && !this.$refs.card?.$el?.contains(document.activeElement)) {
          this.defaultButton()?.focus?.();
        }
      }, 100);
    },
    defaultButton() {
      return this.$refs.cancelButton || this.$refs.okButton;
    },
    redirectContainerFocus(event) {
      // v-dialog focuses its container first: a screen reader would read the dialog twice before its button
      const card = this.$refs.card?.$el;
      const button = this.defaultButton();
      if (card && button && event.target !== card && event.target?.contains?.(card)) {
        window.removeEventListener('focusin', this.redirectContainerFocus, true);
        button.focus();
      }
    },
    focusOrigin(element) {
      // an action of a menu opened the dialog: the menu is gone once the dialog closes, its button stays.
      // Only a menu button is taken: a tooltip activator also has an expanded popup while it is hovered or focused
      const menu = element?.closest?.('.v-menu__content');
      if (!menu) {
        return element;
      }
      // an attached menu sits next to its button
      let ancestor = menu.parentElement;
      for (let level = 0; ancestor && ancestor !== document.body && level < 4; level++) {
        const button = ancestor.querySelector(':scope > [aria-haspopup][aria-expanded="true"], :scope > * > [aria-haspopup][aria-expanded="true"]');
        if (button) {
          return button;
        }
        ancestor = ancestor.parentElement;
      }
      return document.querySelector('[aria-haspopup="menu"][aria-expanded="true"]') || element;
    },
    captureOpenerContext(opener) {
      if (!opener || opener === document.body) {
        return null;
      }
      const item = opener.closest(LIST_ITEM_SELECTOR);
      // the list entry: the item, or the wrapper it is the only content of
      let entry = item;
      while (entry?.parentElement
          && entry.parentElement.childElementCount === 1
          && !entry.parentElement.matches(LIST_SELECTOR)
          && entry.parentElement !== document.body) {
        entry = entry.parentElement;
      }
      return {
        href: window.location.href,
        item,
        parent: opener.parentElement,
        followingEntries: entry && this.siblings(entry, 'nextElementSibling') || [],
        precedingEntries: entry && this.siblings(entry, 'previousElementSibling') || [],
      };
    },
    siblings(element, direction) {
      const siblings = [];
      let sibling = element[direction];
      while (sibling) {
        siblings.push(sibling);
        sibling = sibling[direction];
      }
      return siblings;
    },
    shownFocusables(root) {
      return Array.from(root.querySelectorAll(FOCUSABLE_SELECTOR))
        .filter(element => this.isShown(element) && !this.isInDialog(element));
    },
    isInDialog(element) {
      const content = this.$refs.dialog?.$refs?.content || this.$refs.card?.$el;
      return !!content?.contains?.(element);
    },
    isFocusLost(active) {
      // the focus left on this closing dialog's button is lost as well
      return !active || active === document.body || this.isInDialog(active);
    },
    isShown(element) {
      // a closed side panel keeps its size but is hidden: it cannot take the focus
      return element?.isConnected
        && element.getClientRects().length
        && window.getComputedStyle(element).visibility !== 'hidden'
        && !element.closest('[aria-hidden="true"]');
    },
    restoreFocus() {
      const opener = this.opener;
      const context = this.openerContext;
      this.opener = null;
      this.openerContext = null;
      window.setTimeout(() => {
        // only when the focus was not moved elsewhere meanwhile (e.g. by a side panel the dialog closed)
        if (!this.isFocusLost(document.activeElement)) {
          return;
        }
        this.placeFocus(opener, context);
        // the confirmed action may remove, replace or disable the opener, or change the page, once its request ends
        this.followOpener(opener, context);
      }, 50);
    },
    placeFocus(opener, context) {
      if (context?.href && window.location.href !== context.href) {
        // the page changed in place (a deleted note opens its parent note): the start of the page content
        this.focusMainContent();
      } else if (!this.isShown(opener)) {
        this.focusInPlaceOf(context);
      } else if (!opener.disabled) {
        opener.focus?.();
      }
      // a disabled opener (its action is running) takes the focus back once enabled again
    },
    followOpener(opener, context) {
      // only an opener disabled while its action runs takes the focus back once enabled: a click on the page leaves it on the body too
      let openerWaits = opener?.disabled;
      let timeout = null;
      const observer = new MutationObserver(() => {
        const active = document.activeElement;
        // only while the focus is still on the opener, or lost: never take it from where the user moved it
        const focusLost = this.isFocusLost(active);
        openerWaits = openerWaits || opener?.disabled;
        if (!focusLost && active !== opener) {
          stop();
        } else if (window.location.href !== context?.href) {
          this.placeFocus(opener, context);
          stop();
        } else if (!this.isShown(opener)
            || openerWaits && focusLost && !opener.disabled) {
          this.placeFocus(opener, context);
        }
      });
      // the user acting on the page (a click on its text leaves the focus on the body as well) ends the following
      function stop() {
        observer.disconnect();
        window.clearTimeout(timeout);
        document.removeEventListener('pointerdown', stop, true);
        document.removeEventListener('keydown', stop, true);
      }
      document.addEventListener('pointerdown', stop, true);
      document.addEventListener('keydown', stop, true);
      timeout = window.setTimeout(stop, OPENER_FOLLOW_DELAY);
      observer.observe(document.body, {
        childList: true,
        subtree: true,
        attributes: true,
        attributeFilter: ['disabled', 'style', 'class'],
      });
    },
    focusInPlaceOf(context) {
      // replaced in place, else the next entry of its list, else the previous one, else the start of the page content
      const target = (context?.item?.isConnected || !context?.item) && this.firstShownFocusable(context?.parent)
        || this.firstShownFocusable(context?.item)
        || this.firstShownIn(context?.followingEntries, entry => this.firstShownFocusable(entry))
        || this.firstShownIn(context?.precedingEntries, entry => this.shownFocusables(entry).pop());
      if (target) {
        target.focus();
      } else {
        this.focusMainContent();
      }
    },
    firstShownFocusable(root) {
      return root?.isConnected && this.shownFocusables(root)[0];
    },
    firstShownIn(entries, find) {
      for (const entry of entries || []) {
        const found = entry.isConnected && find(entry);
        if (found) {
          return found;
        }
      }
      return null;
    },
    focusMainContent() {
      // the skip link's target, focusable on every portal page, made so here as well when a page lacks it
      const main = document.getElementById('UISiteBody');
      if (main && !main.hasAttribute('tabindex')) {
        main.setAttribute('tabindex', '-1');
      }
      main?.focus?.();
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