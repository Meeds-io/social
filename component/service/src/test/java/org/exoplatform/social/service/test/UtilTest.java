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
package org.exoplatform.social.service.test;

import java.util.Arrays;

import junit.framework.TestCase;

import org.exoplatform.social.rest.api.EntityBuilder;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.rest.Util;


/**
 * @since 1.2.0-GA
 */
public class UtilTest extends TestCase {
  /**
   * Performs testing for {@link Util#isValidURL(String)}
   * 
   * @throws Exception
   */
  public void testIsValidURL() throws Exception {
    assertTrue(Util.isValidURL("abcd.com"));
    assertTrue(Util.isValidURL("http://google.com"));
    assertTrue(Util.isValidURL("http://địachỉdoanhnghiệp.vn"));
    assertTrue(Util.isValidURL("http://www.google.com/language_tools?hl=en"));
    assertTrue(Util.isValidURL("https://mail.google.com/mail/?shva=1#inbox"));
    assertTrue(Util.isValidURL("http://a+b=sadasd.com.vn"));
    assertTrue(Util.isValidURL("mailto:abc@facebook.com"));
    assertTrue(Util.isValidURL("http://translate.google.com/#en|vi|What has changed?"));
    assertTrue(Util.isValidURL("translate.google.com/#en|vi|What has changed?"));
    assertFalse(Util.isValidURL(null));
    assertFalse(Util.isValidURL(""));
    assertFalse(Util.isValidURL("abc"));
    assertFalse(Util.isValidURL("a bc.com"));
    assertFalse(Util.isValidURL("abc.c om"));
    assertFalse(Util.isValidURL("abc : fsdfs"));
    assertFalse(Util.isValidURL("abc #$ vn"));

    assertFalse(Util.isValidURL("http://www.opensourcesummit.a/"));
    assertTrue(Util.isValidURL("http://www.opensourcesummit.abce/"));
    assertTrue(Util.isValidURL("http://www.opensourcesummit.abcdef/"));
  }
  
  /**
   * Performs testing for {@link Util#getDecodeQueryURL(String)}
   * 
   * @throws Exception
   */
  public void testGetDecodeQueryURL() throws Exception {
    String url = "http://google.com";
    assertEquals(url, Util.getDecodeQueryURL(url));
    
    url = "translate.google.com/#en|vi|What has changed?";
    assertEquals(url, Util.getDecodeQueryURL(url));

    url = "translate.google.com/?translate=abc#en";
    assertEquals(url, Util.getDecodeQueryURL(url));
    
    url = "http://google.com?%3Cscript%3Ealert(%22Link_attached%22)%3C/script%3E";
    assertEquals("http://google.com?<script>alert(\"Link_attached\")</script>", Util.getDecodeQueryURL(url));
  }
  
  /*
   * Performs testing for {@link Util#buildLinkForHeader(Object, String)}
   */
  public void testBuildLinkForHeader() throws Exception {
    CollectionEntity rc = new CollectionEntity(Arrays.asList(new DataEntity()), "key", 0, 20);
    rc.setSize(60);
    String requestPath = "https://localhost:8080/rest/private/v1/social/identities";
    
    //
    rc.setOffset(0);
    String linkForHeader = EntityBuilder.buildLinkForHeader(rc, requestPath).toString();
    assertEquals("<https://localhost:8080/rest/private/v1/social/identities?offset=20&limit=20>; rel=\"next\", <https://localhost:8080/rest/private/v1/social/identities?offset=40&limit=20>; rel=\"last\"", linkForHeader);

    //
    rc.setOffset(60);
    linkForHeader = EntityBuilder.buildLinkForHeader(rc, requestPath).toString();
    assertEquals("<https://localhost:8080/rest/private/v1/social/identities?offset=40&limit=20>; rel=\"prev\", <https://localhost:8080/rest/private/v1/social/identities?offset=0&limit=20>; rel=\"first\"", linkForHeader);
    
    //
    rc.setOffset(20);
    linkForHeader = EntityBuilder.buildLinkForHeader(rc, requestPath).toString();
    assertEquals("<https://localhost:8080/rest/private/v1/social/identities?offset=40&limit=20>; rel=\"next\", <https://localhost:8080/rest/private/v1/social/identities?offset=0&limit=20>; rel=\"prev\", <https://localhost:8080/rest/private/v1/social/identities?offset=0&limit=20>; rel=\"first\", <https://localhost:8080/rest/private/v1/social/identities?offset=40&limit=20>; rel=\"last\"", linkForHeader);
  }
}
