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
<%
  Object suggestionsType = request.getAttribute("suggestionsType");
  if (suggestionsType == null) {
    suggestionsType = "";
  } else {
    suggestionsType = ((String[])suggestionsType)[0];
  }
%>
<div class="VuetifyApp">
  <div id="SuggestionsPeopleAndSpace">
    <script type="text/javascript">
      require(['PORTLET/social-portlet/SuggestionsPeopleAndSpace'],
        app => app.init('<%=suggestionsType%>')
      );
    </script>
    <div data-app="true" class="v-application v-application--is-ltr theme--light" id="SuggestionsPeopleAndSpace">
       <div class="v-application--wrap">
          <div class="flex d-flex xs12 sm12">
             <div class="layout row wrap mx-0">
                <div class="flex d-flex xs12">
                   <div class="flex suggestions-wrapper v-card v-card--flat v-sheet theme--light">
                      <div class="v-card__title suggestions-title subtitle-1 text-uppercase pb-0"><span class="skeleton-background skeleton-text skeleton-border-radius skeleton-header">Suggestions</span></div>
                      <div role="list" class="v-list suggestions-list people-list py-4 mx-4 v-sheet v-sheet--tile theme--light v-list--dense">
                         <div tabindex="-1" role="listitem" class="suggestions-list-item pa-0 v-list-item theme--light">
                            <div class="v-avatar v-list-item__avatar" style="height: 37px; min-width: 37px; width: 37px;">
                               <div class="v-responsive v-image skeleton-background">
                                  <div class="v-responsive__content"></div>
                               </div>
                            </div>
                            <div class="v-list-item__content pb-3">
                               <div class="v-list-item__title body-2 font-weight-bold text-color suggestions-list-item-title"><a href="/portal/meeds/profile/user118" class="text-color skeleton-background skeleton-text skeleton-list-item-title skeleton-border-radius">
                                  rglizr tvxnhr
                                  </a>
                               </div>
                               <div class="v-list-item__subtitle caption text-sub-title suggestions-list-item-subtitle"><span class="skeleton-background skeleton-text skeleton-list-item-subtitle skeleton-border-radius">0 contacts en commun</span></div>
                            </div>
                            <div class="v-list-item__action suggestions-list-item-actions">
                               <div class="transparent v-item-group theme--dark v-btn-toggle"><a text="" icon="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-accept-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconInviteUser"></i></a> <a text="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-refuse-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconCloseCircled"></i></a></div>
                            </div>
                         </div>
                         <div tabindex="-1" role="listitem" class="suggestions-list-item pa-0 v-list-item theme--light">
                            <div class="v-avatar v-list-item__avatar" style="height: 37px; min-width: 37px; width: 37px;">
                               <div class="v-responsive v-image skeleton-background">
                                  <div class="v-responsive__content"></div>
                               </div>
                            </div>
                            <div class="v-list-item__content pb-3">
                               <div class="v-list-item__title body-2 font-weight-bold text-color suggestions-list-item-title"><a href="/portal/meeds/profile/user115" class="text-color skeleton-background skeleton-text skeleton-list-item-title skeleton-border-radius">
                                  hprfpz mskiki
                                  </a>
                               </div>
                               <div class="v-list-item__subtitle caption text-sub-title suggestions-list-item-subtitle"><span class="skeleton-background skeleton-text skeleton-list-item-subtitle skeleton-border-radius">0 contacts en commun</span></div>
                            </div>
                            <div class="v-list-item__action suggestions-list-item-actions">
                               <div class="transparent v-item-group theme--dark v-btn-toggle"><a text="" icon="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-accept-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconInviteUser"></i></a> <a text="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-refuse-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconCloseCircled"></i></a></div>
                            </div>
                         </div>
                      </div>
                      <div role="list" class="v-list suggestions-list space-list py-4 mx-4 v-sheet v-sheet--tile theme--light v-list--dense">
                         <div tabindex="-1" role="listitem" class="suggestions-list-item pa-0 v-list-item theme--light">
                            <div class="v-avatar v-list-item__avatar spaceAvatar" style="height: 37px; min-width: 37px; width: 37px;">
                               <div class="v-responsive v-image skeleton-background">
                                  <div class="v-responsive__content"></div>
                               </div>
                            </div>
                            <div class="v-list-item__content pb-3">
                               <div class="v-list-item__title body-2 font-weight-bold suggestions-list-item-title"><a href="/portal/g/rnahpf/" class="text-color skeleton-background skeleton-text skeleton-list-item-title skeleton-border-radius">
                                  rnahpf
                                  </a>
                               </div>
                               <div class="v-list-item__subtitle caption text-sub-title suggestions-list-item-subtitle"><span class="skeleton-background skeleton-text skeleton-list-item-subtitle skeleton-border-radius">
                                  1 membres
                                  </span>
                               </div>
                            </div>
                            <div class="v-list-item__action suggestions-list-item-actions">
                               <div class="transparent v-item-group theme--dark v-btn-toggle"><a text="" icon="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-accept-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconPlusLight"></i></a> <a text="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-refuse-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconCloseCircled"></i></a></div>
                            </div>
                         </div>
                         <div tabindex="-1" role="listitem" class="suggestions-list-item pa-0 v-list-item theme--light">
                            <div class="v-avatar v-list-item__avatar spaceAvatar" style="height: 37px; min-width: 37px; width: 37px;">
                               <div class="v-responsive v-image skeleton-background">
                                  <div class="v-responsive__content"></div>
                               </div>
                            </div>
                            <div class="v-list-item__content pb-3">
                               <div class="v-list-item__title body-2 font-weight-bold suggestions-list-item-title"><a href="/portal/g/dkxptl/" class="text-color skeleton-background skeleton-text skeleton-list-item-title skeleton-border-radius">
                                  dkxptl
                                  </a>
                               </div>
                               <div class="v-list-item__subtitle caption text-sub-title suggestions-list-item-subtitle"><span class="skeleton-background skeleton-text skeleton-list-item-subtitle skeleton-border-radius">
                                  1 membres
                                  </span>
                               </div>
                            </div>
                            <div class="v-list-item__action suggestions-list-item-actions">
                               <div class="transparent v-item-group theme--dark v-btn-toggle"><a text="" icon="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-accept-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconPlusLight"></i></a> <a text="" small="" min-width="auto" class="px-0 suggestions-btn-action connexion-refuse-btn skeleton-background skeleton-text skeleton-border-radius"><i class="uiIconCloseCircled"></i></a></div>
                            </div>
                         </div>
                      </div>
                   </div>
                </div>
             </div>
          </div>
       </div>
    </div>
  </div>