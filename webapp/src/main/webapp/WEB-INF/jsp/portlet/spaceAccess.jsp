<%
/**
 * This file is part of the Meeds project (https://meeds.io/).
 *
 * Copyright (C) 2020 - 2026 Meeds Association contact@meeds.io
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
<%@page import="org.exoplatform.portal.application.PortalRequestContext"%>
<%@page import="org.exoplatform.web.application.RequestContext"%>
<%@page import="org.exoplatform.social.core.space.SpaceAccessType"%>
<%
  PortalRequestContext portalRequestContext = RequestContext.getCurrentInstance();
  HttpSession portalSession = portalRequestContext.getRequest().getSession();
  SpaceAccessType spaceAccessType = (SpaceAccessType) portalSession.getAttribute(SpaceAccessType.ACCESSED_TYPE_KEY);
  String spaceInvitationToken = (String) portalSession.getAttribute("invitation_id");
  if (spaceAccessType == null) {
    return;
  }
  String spaceAccessTypeLabel = spaceAccessType.name();
  String spaceId = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_ID_KEY);
  String spacePrettyName = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_PRETTY_NAME_KEY);
  String spaceDisplayName = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_DISPLAY_NAME_KEY);
  String originalUri = (String) portalSession.getAttribute(SpaceAccessType.ACCESSED_SPACE_REQUEST_PATH_KEY);
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr theme--light"
    id="SpaceAccess">
    <textarea id="SpaceAccessData" rows="0" class="d-none">{
      "spaceId": "<%=spaceId%>",
      "spaceAccessTypeLabel": "<%=spaceAccessTypeLabel%>",
      "spacePrettyName": "<%=spacePrettyName%>",
      "spaceDisplayName": "<%=spaceDisplayName%>",
      "originalUri": "<%=originalUri%>",
      "spaceInvitationToken": "<%=spaceInvitationToken == null ? "" : spaceInvitationToken%>"
    }</textarea>
    <script type="text/javascript">
      require(['PORTLET/social/SpaceAccessPortlet'],
        app => app.init(
          JSON.parse(document.getElementById('SpaceAccessData').value.replace(/\n/g, '')),
        )
      );
    </script>
  </div>
</div>
