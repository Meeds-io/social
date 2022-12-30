<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
%>
<div class="VuetifyApp">
  <div id="spaceInfosApp" class="uiBox">
    <v-cacheable-dom-app cache-id="spaceInfosApp_<%=space.getId()%>"></v-cacheable-dom-app>
  </div>
</div>
