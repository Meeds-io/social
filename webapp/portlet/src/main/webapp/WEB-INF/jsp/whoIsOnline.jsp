<%@page import="org.exoplatform.social.core.space.model.Space"%>
<%@page import="org.exoplatform.social.core.space.SpaceUtils"%>
<%
  Space space = SpaceUtils.getSpaceByContext();
  String spaceId = space == null ? "" : space.getId();
%>
<div class="VuetifyApp">
  <div data-app="true"
    class="v-application v-application--is-ltr hiddenable-widget theme--light"
    id="OnlinePortlet">
    <v-cacheable-dom-app cache-id="OnlinePortlet_<%=spaceId%>"></v-cacheable-dom-app>
  </div>
</div>