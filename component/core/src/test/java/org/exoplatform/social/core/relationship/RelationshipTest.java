/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.relationship;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

import junit.framework.TestCase;

public class RelationshipTest extends TestCase {
  public void testToString() {
    //invitor
    Identity sender = new Identity("organization", "root");
    //invitee
    Identity receiver = new Identity("organization", "john");

    //create relationship
    Relationship relationship = new Relationship(sender, receiver, Type.PENDING);
    assertEquals("organization:root--[PENDING]--organization:john", relationship.toString());
  }
}
