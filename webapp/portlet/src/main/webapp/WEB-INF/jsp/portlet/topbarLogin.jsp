<%@page import="org.exoplatform.social.core.identity.model.Identity"%>
<%@page import="org.exoplatform.social.webui.Utils"%>
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
  Identity viewerIdentity = Utils.getViewerIdentity();
  String avatarUrl = viewerIdentity == null ? "" : viewerIdentity.getProfile().getAvatarUrl();
%>
<div class="VuetifyApp">
  <div
   class="v-application border-box-sizing v-application--is-ltr theme--light"
   id="topbarLogin">
   <script type="text/javascript">
     require(['PORTLET/social-portlet/TopBarLogin'], app => app.init('<%=avatarUrl%>'));
   </script>
  </div>
</div>
