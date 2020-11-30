<!--
This file is part of the Meeds project (https://meeds.io/).
Copyright (C) 2020 Meeds Association
contact@meeds.io
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.
You should have received a copy of the GNU Lesser General Public License
along with this program; if not, write to the Free Software Foundation,
Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
-->

<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="ExternalSpacesListPortlet">
    <v-cacheable-dom-app cache-id="ExternalSpacesListPortlet"></v-cacheable-dom-app>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/ExternalSpacesList'], app => app.init());
    </script>
     <div class="v-application--wrap">
          <div class="v-card v-card--flat v-sheet theme--light">
            <div
              class="v-card__title text-sub-title subtitle-1 text-uppercase pb-2">
              YOUR SPACE</div>
            <div role="list"
              class="v-list mx-3 pt-0 v-sheet v-sheet--tile theme--light v-list--two-line">
              <div tabindex="-1" role="listitem"
                class="pa-0 spaceItem v-list-item theme--light">
                <div
                  class="v-avatar v-list-item__avatar my-0 v-avatar--tile"
                  href="#"
                  style="height: 40px; min-width: 40px; width: 40px;">
                  <div class="v-avatar v-avatar--tile"
                    style="height: 37px; min-width: 37px; width: 37px;">
                    <div
                      class="v-responsive v-image mx-auto spaceAvatar skeleton-background"
                      style="height: 37px; max-height: 37px; max-width: 37px; width: 37px;">
                      <div class="v-responsive__content"></div>
                    </div>
                  </div>
                </div>
                <div href="#"
                  class="v-list-item__content pa-0">
                  <div class="v-list-item__title">
                    <div
                      class="v-skeleton-loader mt-3 mr-3 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                      style="height: 11px;">
                      <div
                        class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                    </div>
                  </div>
                  <div class="v-list-item__subtitle">
                    <div
                      class="v-skeleton-loader mb-2 mt-1 skeleton-background v-skeleton-loader--boilerplate v-skeleton-loader--is-loading theme--light"
                      style="height: 8px; width: 70px;">
                      <div
                        class="v-skeleton-loader__text v-skeleton-loader__bone"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
     </div>
  </div>
</div>
