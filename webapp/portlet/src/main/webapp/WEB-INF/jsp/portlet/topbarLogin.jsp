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
%>
<div class="VuetifyApp">
  <div
   class="v-application border-box-sizing v-application--is-ltr theme--light"
   id="topbarLogin">
   <script type="text/javascript">
     require(['PORTLET/social-portlet/TopBarLogin'], app => app.init(<%=canRegister%>));
   </script>
  </div>
</div>
