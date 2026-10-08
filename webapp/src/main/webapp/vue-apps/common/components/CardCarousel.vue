<template>
  <div class="carousel-top-parent overflow-hidden position-relative">
    <v-card
      ref="scrollContainer"
      :class="!dense && 'px-0 pb-4 pt-2'"
      class="carousel-middle-parent scrollbar-width-none transparent d-flex overflow-x-scroll"
      flat
      @scroll="computeProperties"
      @scrollend="resetTargetChildIndex"
      @resize="computeProperties">
      <div :class="parentClass" class="carousel-last-parent d-flex ma-auto">
        <slot></slot>
      </div>
    </v-card>
    <!-- Arrows are kept after the scrollable content in the DOM: slotted cards may
         hold positioned elements at the same z-index level, and tree order is what
         keeps both arrows clickable above them -->
    <v-expand-transition>
      <v-btn
        v-show="displayLeftArrow"
        :aria-label="$t('cardCarousel.leftArrowButtonTitle')"
        :left="!$vuetify.rtl"
        :right="$vuetify.rtl"
        color="while"
        width="23px"
        height="23px"
        class="absolute-vertical-center z-index-one"
        fab
        dark
        absolute
        x-small
        @click="moveLeft">
        <v-icon size="25">{{ leftArrowIcon }}</v-icon>
      </v-btn>
    </v-expand-transition>
    <v-expand-transition>
      <v-btn
        v-show="displayRightArrow"
        :aria-label="$t('cardCarousel.rightArrowButtonTitle')"
        :left="$vuetify.rtl"
        :right="!$vuetify.rtl"
        color="while"
        width="23px"
        height="23px"
        class="absolute-vertical-center z-index-one"
        fab
        dark
        absolute
        x-small
        @click="moveRight">
        <v-icon size="25">{{ rightArrowIcon }}</v-icon>
      </v-btn>
    </v-expand-transition>
  </div>
</template>

<script>
export default {
  props: {
    parentClass: {
      type: Object,
      default: null,
    },
    hideArrows: {
      type: Boolean,
      default: false,
    },
    dense: {
      type: Boolean,
      default: false,
    },
  },
  data: () => ({
    scrollElement: null,
    displayLeftArrow: false,
    displayRightArrow: false,
    visibleChildrenPerPage: 1,
    targetChildIndex: null,
    targetChildIndexTimeout: null,
    computing: false,
  }),
  computed: {
    leftArrowIcon() {
      return this.$vuetify.rtl && 'fa-arrow-circle-right' || 'fa-arrow-circle-left';
    },
    rightArrowIcon() {
      return this.$vuetify.rtl && 'fa-arrow-circle-left' || 'fa-arrow-circle-right';
    },
  },
  mounted() {
    if (!this.hideArrows) {
      this.scrollElement = this.$refs.scrollContainer && this.$refs.scrollContainer.$el;
  
      window.setTimeout(() => {
        this.computeProperties();
      }, 500);
      window.onresize = this.computeProperties;
    }
  },
  updated() {
    this.computeProperties();
  },
  methods: {
    stopPropagation(event) {
      if (event) {
        event.stopPropagation();
      }
    },
    moveRight() {
      this.scrollToChild(this.getStartChildIndex() + this.visibleChildrenPerPage);
    },
    moveLeft() {
      this.scrollToChild(this.getStartChildIndex() - this.visibleChildrenPerPage);
    },
    scrollToChild(index) {
      const children = this.scrollElement.firstChild.children;
      this.targetChildIndex = Math.min(Math.max(index, 0), children.length - 1);
      // Browsers without the scrollend event release the target once the
      // smooth scroll has had the time to end
      window.clearTimeout(this.targetChildIndexTimeout);
      this.targetChildIndexTimeout = window.setTimeout(this.resetTargetChildIndex, 1000);
      this.scrollElement.scrollTo({
        left: this.getChildScrollPosition(children[this.targetChildIndex]),
        behavior: 'smooth'
      });
    },
    // A click while the previous arrow's smooth scroll is still running starts
    // from that scroll's target: the position being animated is between two
    // pages and would land the carousel in the middle of one
    getStartChildIndex() {
      return this.targetChildIndex === null ? this.getFirstVisibleChildIndex() : this.targetChildIndex;
    },
    resetTargetChildIndex() {
      window.clearTimeout(this.targetChildIndexTimeout);
      this.targetChildIndex = null;
    },
    // The page the user sees is read from the scroll position rather than
    // tracked across arrow clicks, so that a swipe or a drag of the content
    // never desynchronizes the arrows from what is displayed
    getFirstVisibleChildIndex() {
      const children = Array.from(this.scrollElement.firstChild.children);
      const scrollLeft = this.scrollElement.scrollLeft;
      const index = children.findIndex(child => {
        const childScrollPosition = this.getChildScrollPosition(child);
        return this.$vuetify.rtl ? childScrollPosition <= scrollLeft + 10 : childScrollPosition >= scrollLeft - 10;
      });
      return index < 0 ? children.length - 1 : index;
    },
    // The scroll position that displays a child at the start edge of the
    // viewport with an 8px peek of the previous one. In RTL the start edge
    // is the right one and the scroll position grows negative
    getChildScrollPosition(child) {
      return this.$vuetify.rtl
        ? child.offsetLeft + child.offsetWidth + 8 - this.scrollElement.clientWidth
        : child.offsetLeft - 8;
    },
    computeProperties() {
      if (!this.computing && !this.hideArrows) {
        this.computing = true;
        window.setTimeout(() => {
          const parentWidth = this.scrollElement.offsetWidth;
          const contentWidth = this.scrollElement.firstChild.offsetWidth;
          const children = this.scrollElement.firstChild.children;
          const childrenCount = children.length;
          this.visibleChildrenPerPage = Math.max(1, parseInt(parentWidth * childrenCount / contentWidth) || 0);
          this.displayLeftArrow = this.scrollElement && childrenCount && this.checkDisplayLeftArrow(children);
          this.displayRightArrow = this.scrollElement && childrenCount && this.checkDisplayRightArrow(children);
          this.computing = false;
        }, 200);
      }
    },
    checkDisplayLeftArrow(children) {
      return Math.abs(this.scrollElement.scrollLeft) - Math.abs(this.$vuetify.rtl ? 0 : children[0].offsetLeft) > 10;
    },
    checkDisplayRightArrow() {
      return parseInt(this.scrollElement.scrollWidth - this.scrollElement.offsetWidth - Math.abs(this.scrollElement.scrollLeft)) > 10;
    },
  },
};
</script>