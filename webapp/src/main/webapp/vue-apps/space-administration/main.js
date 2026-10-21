/*
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2024 Meeds Association contact@meeds.io
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import './initComponents.js';
import './extensions.js';
import './services.js';

if (extensionRegistry) {
  const components = extensionRegistry.loadComponents('SpacesAdministration');
  if (components && components.length > 0) {
    components.forEach(cmp => {
      Vue.component(cmp.componentName, cmp.componentOptions);
    });
  }
}

export function init(isExternalFeatureEnabled) {
  const appId = 'spacesAdministration';
  const lang = eXo?.env?.portal?.language || 'en';
  exoi18n.loadLanguageAsync(lang, [
    `/social/i18n/locale.portlet.social.SpacesAdministrationPortlet?lang=${lang}`,
    `/social/i18n/locale.portlet.social.SpacesListApplication?lang=${lang}`,
    `/social/i18n/locale.portlet.SpaceTemplatesManagement?lang=${lang}`,
    `/social/i18n/locale.portal.webui?lang=${lang}`,
  ])
    .then(i18n => Vue.createApp({
      data: {
        isExternalFeatureEnabled,
        spaceTemplates: null,
        mainExtensions: [],
        tableColumnExtensions: [],
        itemMenuExtensions: [],
        bulkExtensions: [],
        usersPermission: '/platform/users',
        externalsPermission: '/platform/externals',
        administratorsPermission: '/platform/administrators',
        collator: new Intl.Collator(eXo.env.portal.language, {
          numeric: true,
          sensitivity: 'base'
        }),
        allSpacesSelected: false,
        selectedSpaces: [],
        selectedTemplateId: '0',
        selectedRegistration: '',
        selectedVisibility: '',
        isBulkProcessing: false,
        initialized: false,
        keyword: null,
        loadingSpaces: false,
        loadingDisplay: false,
        offset: 0,
        pageSize: 25,
        spacesSize: 0,
        processedSpaces: 0,
        bulkRun: 0,
        bulkOperationLabels: null,
        bulkOperationTotal: null,
        // The list loads, one at a time:
        // - spacesLoad: the load in flight, or null. A page load asked
        //   meanwhile joins it (Show more and the select-all walk start from
        //   a list that is not busy).
        // - nextRefresh, nextRefreshLoad: at most one refresh queued behind
        //   it, the refreshes asked meanwhile merged into it. It loads once
        //   the load in flight is over, so that it reads the list after the
        //   change that asked for it.
        // - refreshPending: a refresh asked while a bulk operation runs, made
        //   once the operation is over: it would clear the selection the
        //   operation walks.
        spacesLoad: null,
        nextRefresh: null,
        nextRefreshLoad: null,
        refreshPending: null,
        rowActionsInFlight: 0,
        sortBy: 'title',
        sortDesc: false,
        expand: 'managers,groupBinding,extendedProperties',
        spaces: [],
      },
      computed: {
        isMobile() {
          return this.$vuetify.breakpoint.mobile;
        },
        canShowMore() {
          return this.spaces?.length && this.spacesSize > this.spaces.length;
        },
        loading() {
          return this.loadingDisplay || this.loadingSpaces;
        },
        isFilteredByTemplate() {
          return this.selectedTemplateId && Number(this.selectedTemplateId);
        },
        isFilteredByRegistration() {
          return this.selectedRegistration && this.selectedRegistration !== '';
        },
        isFilteredByVisibility() {
          return this.selectedVisibility && this.selectedVisibility !== '';
        },
        rowActionInFlight() {
          return this.rowActionsInFlight > 0;
        },
        isBusy() {
          // Every control that changes the list or the selection waits for
          // the bulk operation, the page load or the row action in flight
          return this.isBulkProcessing || this.loadingSpaces || this.rowActionInFlight;
        },
        customSort() {
          // A contributed sort is applied only to the list with no keyword
          return !this.keyword?.length
            && this.tableColumnExtensions?.find(e => e.sortBy === this.sortBy)?.customSort
            || null;
        },
      },
      watch: {
        keyword() {
          if (this.initialized && !this.isBulkProcessing) {
            if (this.keyword?.length) {
              this.resetContributedSort();
            }
            // Searched once a load in flight is over
            void this.searchSpaces(true, true);
          }
        },
        selectedSpaces() {
          if (!this.isBulkProcessing) {
            this.allSpacesSelected = false;
          }
        },
        isBulkProcessing() {
          if (this.isBulkProcessing) {
            window.addEventListener('beforeunload', this.displayConfirmReload);
            document.dispatchEvent(new CustomEvent('alert-message', {detail: {
              alertComponent: 'spaces-administration-processing-alert',
              alertComponentParams: {
                options: this.$root,
              },
              alertDismissible: false,
              alertTimeout: 864000000,
              alertLinkText: this.$t('social.spaces.administration.manageSpaces.cancel'),
              alertLinkCallback: () => {
                this.isBulkProcessing = false;
                document.dispatchEvent(new CustomEvent('close-alert-message'));
              },
            }}));
          } else {
            window.removeEventListener('beforeunload', this.displayConfirmReload);
            document.dispatchEvent(new CustomEvent('close-alert-message'));
            if (this.refreshPending) {
              const {clean, silent} = this.refreshPending;
              this.refreshPending = null;
              void this.searchSpaces(true, clean, silent);
            }
          }
        },
      },
      created() {
        document.addEventListener('extension-spaces-administration-bulk-action-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-menu-action-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-main-updated', this.refreshExtensions);
        document.addEventListener('extension-spaces-administration-table-column-updated', this.refreshExtensions);
        this.$on('spaces-administration-list-refresh', this.refreshSpaces);
        this.refreshSpaceTemplates();
        this.refreshExtensions();
      },
      beforeDestroy() {
        document.removeEventListener('extension-spaces-administration-bulk-action-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-menu-action-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-main-updated', this.refreshExtensions);
        document.removeEventListener('extension-spaces-administration-table-column-updated', this.refreshExtensions);
        this.$off('spaces-administration-list-refresh', this.refreshSpaces);
      },
      methods: {
        displayLoading() {
          this.loadingDisplay = true;
        },
        hideLoading() {
          this.loadingDisplay = false;
        },
        async refreshSpaceTemplates() {
          const spaceTemplates = await this.$spaceTemplateService.getSpaceTemplates(true);
          spaceTemplates.sort((a, b) => this.$root.collator.compare(a.name.toLowerCase(), b.name.toLowerCase()));
          this.spaceTemplates = spaceTemplates;
        },
        refreshExtensions() {
          this.bulkExtensions = extensionRegistry.loadExtensions('spaces-administration', 'bulk-action') || [];
          this.itemMenuExtensions = extensionRegistry.loadExtensions('spaces-administration', 'menu-action') || [];
          this.mainExtensions = extensionRegistry.loadExtensions('spaces-administration', 'main') || [];
          this.tableColumnExtensions = extensionRegistry.loadExtensions('spaces-administration', 'table-column') || [];
        },
        resetContributedSort() {
          // A contributed sort applies to the unfiltered list with no keyword
          // only: applying a filter or a keyword returns to the default sort
          if (this.tableColumnExtensions?.find(e => e.sortBy && e.sortBy === this.sortBy)) {
            this.sortBy = 'title';
            this.sortDesc = false;
          }
        },
        refreshSpaces(clean, silent) {
          if (this.isBulkProcessing) {
            // A refresh clears the selection the running operation walks:
            // it is made once the operation is over
            this.refreshPending = {
              clean: clean || this.refreshPending?.clean,
              silent: silent && (this.refreshPending?.silent ?? true),
            };
          } else {
            void this.searchSpaces(true, clean, silent);
          }
        },
        /**
         * Loads a page of spaces, or the whole displayed range when refresh
         * is set. A load failure keeps the displayed rows and raises the
         * generic error alert, unless silent is set.
         *
         * @param {boolean} refresh reload the whole displayed range from the first space
         * @param {boolean} clean empty the list before a refresh
         * @param {boolean} silent raise no alert on a load failure
         * @returns {Promise<boolean>} resolved once the load is over, with false
         *          when it failed; never rejected
         */
        searchSpaces(refresh, clean, silent) {
          return this.loadSpaces(refresh, clean)
            .then(() => true)
            .catch(e => {
              // eslint-disable-next-line no-console
              console.error('Error loading spaces', e);
              if (!silent) {
                this.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.errorSpacesBulkOperation'), 'error');
              }
              return false;
            });
        },
        /**
         * One load at a time (see spacesLoad): a page load asked while a load
         * is in flight gets that load; a refresh is queued behind it.
         *
         * @param {boolean} refresh reload the whole displayed range from the first space
         * @param {boolean} clean empty the list before a refresh
         * @returns {Promise} rejected when the load failed
         */
        loadSpaces(refresh, clean) {
          if (this.spacesLoad) {
            if (!refresh) {
              return this.spacesLoad;
            }
            this.nextRefresh = {
              clean: !!clean || !!this.nextRefresh?.clean,
            };
            if (!this.nextRefreshLoad) {
              this.nextRefreshLoad = this.spacesLoad
                .catch(() => null)
                .then(() => {
                  const nextRefresh = this.nextRefresh;
                  this.nextRefresh = null;
                  this.nextRefreshLoad = null;
                  return this.loadSpaces(true, nextRefresh.clean);
                });
            }
            return this.nextRefreshLoad;
          }
          this.spacesLoad = this.fetchSpaces(refresh, clean)
            .finally(() => this.spacesLoad = null);
          return this.spacesLoad;
        },
        async fetchSpaces(refresh, clean) {
          if (refresh) {
            this.allSpacesSelected = false;
            this.selectedSpaces = [];
          }
          const offset = refresh ? 0 : this.spaces.length;
          const limit = refresh ? this.offset + this.pageSize : this.pageSize;

          const displayedSpaces = this.spaces;
          this.loadingSpaces = true;
          try {
            const customSort = this.customSort;
            if (customSort) {
              const spaces = await customSort({
                offset: offset,
                limit: limit,
                expand: this.expand,
                templateId: (this.selectedTemplateId && Number(this.selectedTemplateId)) ? this.selectedTemplateId : null,
                sortDesc: this.sortDesc,
                currentSpaces: this.spaces,
                currentSpacesSize: this.spacesSize,
              });
              this.spaces = offset ? this.uniqueSpaces(spaces) : spaces;
            } else {
              if (!offset && clean) {
                this.spaces = [];
              }
              const data = await this.$spaceService.getSpacesByFilter({
                query: this.keyword,
                visibility: this.selectedVisibility?.length ? this.selectedVisibility : null,
                registration: this.selectedRegistration?.length ? this.selectedRegistration : null,
                templateId: this.selectedTemplateId && Number(this.selectedTemplateId),
                filter: 'all',
                expand: this.expand,
                offset: offset,
                limit: limit,
                sortBy: 'title',
                sortDirection: this.sortDesc ? 'desc' : 'asc',
              });
              if (offset) {
                this.spaces = this.uniqueSpaces([...this.spaces, ...(data?.spaces || [])]);
              } else {
                this.spaces = data?.spaces || [];
              }
              this.spacesSize = data?.size || 0;
              this.$emit('loaded', this.spacesSize);
            }
          } catch (e) {
            // A failed load keeps the rows displayed before it
            this.spaces = displayedSpaces;
            throw e;
          } finally {
            this.loadingSpaces = false;
            this.loadingDisplay = false;
            this.initialized = true;
          }
        },
        uniqueSpaces(spaces) {
          const ids = new Set();
          return spaces.filter(space => space && !ids.has(space.id) && ids.add(space.id));
        },
        async loadNextPage() {
          const loadedSpacesCount = this.spaces.length;
          this.offset += this.pageSize;
          const loaded = await this.searchSpaces();
          if (loaded
              && !this.customSort
              && this.spaces.length === loadedSpacesCount
              && loadedSpacesCount < this.spacesSize) {
            // The page held only spaces already listed, sorted in before the
            // offset since the last load: the whole displayed range is
            // reloaded, and the selection is kept on the spaces still listed
            const allSpacesSelected = this.allSpacesSelected;
            const selectedIds = new Set(this.selectedSpaces.map(space => space.id));
            // A failed reload keeps the rows displayed, and so the selection
            await this.searchSpaces(true, false, true);
            this.selectedSpaces = allSpacesSelected
              ? this.spaces
              : this.spaces.filter(space => selectedIds.has(space.id));
            if (allSpacesSelected) {
              // After the flush: the selectedSpaces watcher resets allSpacesSelected
              await this.$nextTick();
              this.allSpacesSelected = true;
            }
          }
        },
        /**
         * Applies callback on each selected space, in display order, while
         * the page is held in progress; with allSpacesSelected, the next
         * pages are loaded until every matching space is processed.
         *
         * Each run has its own identity: a run that has been cancelled and
         * superseded by another one writes no shared state any more.
         *
         * @param {Function} callback (space, params, {run, stop}) => Promise
         * @param {Object} params passed to callback, onFinish and onCancel
         * @param {Function} onFinish called once the run is over, when not cancelled nor failed
         * @param {Function} onCancel called otherwise
         * @param {Object} options optional {labels, total, finalize}:
         *   the processing alert labels and total, and a step run once
         *   every space is processed, while the page is still held in progress
         * @returns {Promise} resolved with {status: 'done'|'cancelled'|'failed'|'superseded'}, never rejected
         */
        async applyOperationInBulk(callback, params, onFinish, onCancel, options) {
          const run = ++this.bulkRun;
          const isCurrent = () => this.bulkRun === run;
          const isActive = () => isCurrent() && this.isBulkProcessing;
          const context = {
            run,
            stop: () => {
              if (isCurrent()) {
                this.isBulkProcessing = false;
              }
            },
          };
          let status = 'done';
          this.bulkOperationLabels = options?.labels || null;
          this.bulkOperationTotal = options?.total ?? null;
          this.processedSpaces = 0;
          this.isBulkProcessing = true;
          this.$emit('spaces-administration-bulk-operation-status', null, 'disabled');
          try {
            if (this.allSpacesSelected) {
              let index = 0;
              do {
                while (index < this.spaces.length && isActive()) {
                  // eslint-disable-next-line no-await-in-loop
                  await this.applyOperationOnSpace(this.spaces[index++], params, callback, context);
                }
                if (index >= this.spaces.length && isActive()) {
                  // eslint-disable-next-line no-await-in-loop
                  const pageStatus = await this.loadNextBulkPage(isCurrent);
                  if (pageStatus !== 'loaded') {
                    if (pageStatus === 'failed') {
                      status = 'failed';
                    }
                    break;
                  }
                  if (isActive()) {
                    this.selectedSpaces = this.spaces;
                  }
                }
              } while (index < this.spaces.length && isActive());
            } else {
              for (let i = 0; i < this.spaces.length; i++) {
                if (!isActive()) {
                  break;
                }
                // Done on purpose to iterate over spaces instead of selection
                // in order to preserve display order processing
                // instead of selection order
                const space = this.spaces[i];
                if (this.selectedSpaces.find(s => s.id === space.id)) {
                  // eslint-disable-next-line no-await-in-loop
                  await this.applyOperationOnSpace(space, params, callback, context);
                }
              }
            }
            if (status === 'done' && options?.finalize && isActive()) {
              try {
                await options.finalize(context);
              } catch (e) {
                // eslint-disable-next-line no-console
                console.error('Error finalizing the bulk operation', e);
                if (isActive()) {
                  this.isBulkProcessing = false;
                  status = 'failed';
                }
              }
            }
          } finally {
            if (!isCurrent()) {
              status = 'superseded';
            } else if (!this.isBulkProcessing && status === 'done') {
              status = 'cancelled';
            }
            if (isCurrent()) {
              this.allSpacesSelected = false;
              this.selectedSpaces = [];
              this.$emit('spaces-administration-bulk-operation-status', null, null);
            }
            try {
              if (status === 'done') {
                this.isBulkProcessing = false;
                await this.$nextTick();
                onFinish?.(params);
              } else {
                onCancel?.(params);
              }
            } catch (e) {
              // eslint-disable-next-line no-console
              console.error('Error ending the bulk operation', e);
            }
          }
          return {status};
        },
        /**
         * Loads the next page of a select-all walk. A page that fails to
         * load, or that adds no new space while the list is known to hold
         * more, ends the walk as a failure. Without a contributed sort, the
         * walk ends with no request once every matching space is listed; a
         * contributed sort reports no total, and its end is an empty page.
         *
         * @param {Function} isCurrent tells whether the walking run is still the current one
         * @returns {Promise<string>} 'loaded'; 'complete' when every matching space
         *          is listed; 'failed' when the walk failed; 'stopped' when the
         *          run was cancelled meanwhile
         */
        async loadNextBulkPage(isCurrent) {
          const loadedSpacesCount = this.spaces.length;
          const customSort = this.customSort;
          if (!customSort && loadedSpacesCount >= this.spacesSize) {
            return 'complete';
          }
          try {
            this.offset += this.pageSize;
            await this.loadSpaces();
          } catch (e) {
            // eslint-disable-next-line no-console
            console.error('Error loading the next page of spaces', e);
            return this.failBulkPageLoad(isCurrent);
          }
          if (isCurrent()
              && !customSort
              && this.spaces.length === loadedSpacesCount
              && loadedSpacesCount < this.spacesSize) {
            // eslint-disable-next-line no-console
            console.error('The next page of spaces added no new space while', this.spacesSize - loadedSpacesCount, 'are expected');
            return this.failBulkPageLoad(isCurrent);
          }
          return 'loaded';
        },
        failBulkPageLoad(isCurrent) {
          // A failure that arrives after a Cancel is no failure of the run
          if (isCurrent() && this.isBulkProcessing) {
            // The processing alert closes before the error alert opens
            this.isBulkProcessing = false;
            this.$emit('alert-message', this.$t('social.spaces.administration.manageSpaces.errorSpacesBulkOperation'), 'error');
            this.$emit('spaces-administration-list-refresh', false, true);
            return 'failed';
          }
          return 'stopped';
        },
        async applyOperationOnSpace(space, params, callback, context) {
          const isCurrent = () => this.bulkRun === context.run;
          this.$emit('spaces-administration-bulk-operation-status', space.id, 'processing');
          try {
            await callback(space, params, context);
            if (isCurrent()) {
              this.$emit('spaces-administration-bulk-operation-status', space.id, 'done');
            }
          } catch (e) {
            // eslint-disable-next-line no-console
            console.error('Error processing space ', space.id, '. Error: ', e);
            if (isCurrent()) {
              this.$emit('spaces-administration-bulk-operation-status', space.id, 'error');
            }
          } finally {
            if (isCurrent()) {
              this.processedSpaces++;
            }
          }
        },
        displayConfirmReload(event) {
          event.preventDefault();
          event.returnValue = true;
        },
      },
      template: `<spaces-administration id="${appId}" />`,
      i18n,
      vuetify: Vue.prototype.vuetifyOptions,
    }, `#${appId}`, 'Spaces Administration'))
    .finally(() => Vue.prototype.$utils.includeExtensions('ManageSpacesExtension'));
}