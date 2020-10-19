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
package org.exoplatform.social.service.rest;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.social.service.test.AbstractResourceTest;

import javax.ws.rs.core.MultivaluedMap;

/**
 * LinkShareRestServiceTest.java
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since	 Mar 4, 2010
 */
public class LinkShareRestServiceTest extends AbstractResourceTest {

  private final String RIGHT_LINK = "hTTp://google.com";
  private final String WRONG_LINK = "http://google.com/ahgo/ahgoeh";
  public void setUp() throws Exception {
    super.setUp();

    addResource(LinkShareRestService.class, null);
  }

  public void tearDown() throws Exception {
    super.tearDown();

    removeResource(LinkShareRestService.class);
  }

  public void testJsonRightLink() throws Exception {
    byte[] data = ("{\"link\":\""+ RIGHT_LINK +"\", \"lang\": \"en\"}").getBytes("UTF-8");
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("POST", "/social/linkshare/show.json", "", h, data, writer);
    assertEquals(200, response.getStatus());
    assertEquals("application/json;charset=utf-8", response.getContentType().toString());
    LinkShare linkShare = (LinkShare) response.getEntity();
    assertEquals(RIGHT_LINK, linkShare.getLink());
    assertNotNull(linkShare.getTitle());
  }

  public void testXmlRightLink() throws Exception {
    //TODO hoatle fix to work
/*    byte[] data =
      ("<xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<link>" + RIGHT_LINK + "</link>"
        + "<lang>en</lang>").getBytes("UTF-8");
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/xml");
    h.putSingle("content-length", "" + data.length);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("POST", "/social/linkshare/show.xml", "", h, data, writer);
    assertEquals(200, response.getStatus());
    assertEquals("application/xml", response.getContentType().toString());
    LinkShare linkShare = (LinkShare) response.getEntity();
    assertEquals(RIGHT_LINK, linkShare.getLink());
    assertNotNull(linkShare.getTitle());*/
  }

  public void testJsonWrongLink() throws Exception {
    byte[] data = ("{\"link\":\""+ WRONG_LINK +"\", \"lang\": \"en\"}").getBytes("UTF-8");
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("POST", "/social/linkshare/show.json", "", h, data, writer);
    assertEquals(200, response.getStatus());
    //assertEquals("text/plain", response.getContentType().toString());
  }

  public void testXmlWrongLink() throws Exception {
    //TODO hoatle fix to work
  }

  public void testBadRequest() throws Exception {
    ContainerResponse response = service("GET", "/social/linkshare/show.json", "", null, null);
    assertEquals(405, response.getStatus()); //Method Not Allowed
    response = service("POST", "/social/linkshare/show.json", "", null, null);
    assertEquals(400, response.getStatus()); //Bad Request
  }

}
