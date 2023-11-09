<%@page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@page import="org.exoplatform.social.webui.Utils"%>
<%@page import="io.meeds.portal.security.constant.UserRegistrationType"%>
<%@page import="org.exoplatform.container.ExoContainerContext"%>
<%@page import="io.meeds.portal.security.service.SecuritySettingService"%>
<%
/**
 * This file is part of the Meeds project (https://meeds.io/).
 * 
 * Copyright (C) 2023 Meeds Association contact@meeds.io
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
%>
<%
  SecuritySettingService securitySettingService = ExoContainerContext.getService(SecuritySettingService.class);
  boolean canRegister = securitySettingService.getRegistrationType() == UserRegistrationType.OPEN;
  Identity viewerIdentity = Utils.getViewerIdentity();
  String avatarUrl = viewerIdentity == null ? "" : viewerIdentity.getProfile().getAvatarUrl();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="topbarLogin">
    <% if (request.getRemoteUser() == null) { %>
    <div class="v-application--wrap">
      <div class="d-flex">
        <a href="/portal/login"
          class="primary me-1 v-btn v-btn--depressed v-btn--flat v-btn--outlined theme--light v-size--default"
          id="topBarLoginButton"><span class="v-btn__content"><span
            class="text-none">&nbsp;</span></span></a>
      </div>
    </div>
    <% } %>
    <script type="text/javascript">
      require(['PORTLET/social-portlet/TopBarLogin'], app => app.init('<%=avatarUrl%>', <%=canRegister%>));
    </script>
  </div>
</div>