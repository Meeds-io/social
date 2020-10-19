/*
 * This file is part of the Meeds project (https://meeds.io/).
 * Copyright (C) 2020 Meeds Association
 * contact@meeds.io
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.rest.suggest;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.json.JSONObject;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserACLMetaData;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.test.mock.MockHttpServletRequest;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.service.rest.BaseRestServicesTestCase;

public class TestPeopleRestServices extends BaseRestServicesTestCase {

    protected Class<?> getComponentClass() {
        return PeopleRestServices.class;
    }

    public void testSuggestions() throws Exception {
        String path = "/homepage/intranet/people/contacts/suggestions";
        EnvironmentContext envctx = new EnvironmentContext();
        HttpServletRequest httpRequest =
           new MockHttpServletRequest(path, null, 0, "GET", null);
        
        envctx.put(HttpServletRequest.class, httpRequest);

        ContainerResponse resp =
           launcher.service("GET", path, "", null, null, envctx);
        assertEquals(500, resp.getStatus());

        // No suggestion, using last users but don't propose myself
        Identity idRoot = new Identity(OrganizationIdentityProvider.NAME, "root");
        idRoot.setId("root");
        Identity idFoo = new Identity(OrganizationIdentityProvider.NAME, "foo");
        idFoo.setId("foo");
        Identity idBar = new Identity(OrganizationIdentityProvider.NAME, "bar");
        idBar.setId("bar");
        Identity idBaz = new Identity(OrganizationIdentityProvider.NAME, "baz");
        idBaz.setId("baz");
        Identity idQux = new Identity(OrganizationIdentityProvider.NAME, "qux");
        idBaz.setId("qux");
        envctx.put(SecurityContext.class, new MockSecurityContext(idFoo.getRemoteId()));

        Map<String, Object> imResults = new HashMap<String, Object>();
        imResults.put("getOrCreateIdentity", idFoo);
        imResults.put("getLastIdentities", Arrays.asList(idRoot, idFoo));
        IdentityManager im = createProxy(IdentityManager.class, imResults);
        getContainer().registerComponentInstance("IdentityManager", im);

        Map<String, Object> rmResults = new HashMap<String, Object>();
        rmResults.put("getConnections", new MockListAccess<Identity>(new Identity[]{}));

        RelationshipManager rm = createProxy(RelationshipManager.class, rmResults);
        getContainer().registerComponentInstance("RelationshipManager", rm);

        UserACLMetaData md = new UserACLMetaData();
        md.setSuperUser(idRoot.getRemoteId());
        UserACL uACL = new UserACL(md);
        getContainer().registerComponentInstance("UserACL", uACL);

        resp = launcher.service("GET", path, "", null, null, envctx);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType().toString());
        JSONObject json = new JSONObject(resp.getEntity().toString());
        assertTrue(json.has("items"));
        assertTrue(json.getJSONArray("items").length() == 0);

        // No suggestion, using last users but don't propose user with whom you have a relationship already
        imResults.put("getLastIdentities", Arrays.asList(idBar, idFoo));
        rmResults.put("get", new Relationship("x"));

        resp = launcher.service("GET", path, "", null, null, envctx);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType().toString());
        json = new JSONObject(resp.getEntity().toString());
        assertTrue(json.has("items"));
        assertTrue(json.getJSONArray("items").length() == 0);

        // The only suggestion is bar
        rmResults.put("getConnections", new MockListAccess<Identity>(new Identity[]{idBar}));
        rmResults.put("getSuggestions", Collections.singletonMap(idRoot, 1));
        rmResults.remove("get");

        resp = launcher.service("GET", path, "", null, null, envctx);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType().toString());
        json = new JSONObject(resp.getEntity().toString());
        assertTrue(json.has("items"));
        assertEquals(1, json.getJSONArray("items").length());
        assertEquals(json.getJSONArray("items").getJSONObject(0).getString("username"), idBar.getRemoteId());

        // There is one suggestion and one connection to a contact
        // bar is the suggestion since he is a connection
        // baz is a new suggestion as he is a new registered user
        Map<Identity, Integer> suggestions = new HashMap<>();
        suggestions.put(idRoot, 1);
        suggestions.put(idBar, 2);
        imResults.put("getLastIdentities", Arrays.asList(idRoot, idFoo, idBaz));
        rmResults.put("getConnections", new MockListAccess<Identity>(new Identity[]{idQux}));
        rmResults.put("getSuggestions", suggestions);
        resp = launcher.service("GET", path, "", null, null, envctx);
        assertEquals(200, resp.getStatus());
        assertEquals("application/json", resp.getContentType().toString());
        json = new JSONObject(resp.getEntity().toString());
        assertTrue(json.has("items"));
        assertEquals(2, json.getJSONArray("items").length());

        getContainer().unregisterComponent("UserACL");
        getContainer().unregisterComponent("RelationshipManager");
        getContainer().unregisterComponent("IdentityManager");
    }

}
