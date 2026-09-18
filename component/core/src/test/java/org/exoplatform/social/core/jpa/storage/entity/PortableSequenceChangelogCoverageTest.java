/*
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.exoplatform.social.core.jpa.storage.entity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.meeds.common.persistence.PortableSequence;

/**
 * Every {@link PortableSequence} name declared by an entity of this module
 * must be created by a Liquibase {@code createSequence} that runs on
 * PostgreSQL. The generator uses an identity column only on MySQL-family
 * dialects; everywhere else it needs the sequence of that exact name, and the
 * changelogs' {@code autoIncrement} property is {@code false} on
 * {@code oracle,postgresql}. The HSQLDB suite cannot see a changeset filtered
 * to another dialect, which is how changeset {@code 1.0.0-123}
 * ({@code dbms="hsqldb"} only) shipped without its PostgreSQL twin and
 * disabling a user on PostgreSQL failed for five months
 * ({@code relation "seq_soc_user_bindings_queue_id" does not exist},
 * EXO-90302). This test fails on that changelog and passes once
 * {@code 1.0.0-132} creates the sequence for {@code oracle,postgresql}.
 */
public class PortableSequenceChangelogCoverageTest {

  private static final String       ENTITIES_INDEX  = "jpa-entities.idx";

  private static final List<String> CHANGELOG_FILES = List.of("db/changelog/social-rdbms.db.changelog-1.0.0.xml",
                                                              "db/changelog/metadata-rdbms.db.changelog-1.0.0.xml");

  private static final String       DIALECT         = "postgresql";

  @Test
  public void testEveryPortableSequenceHasAPostgresqlCreateSequence() throws Exception {
    Map<String, String> requiredSequences = portableSequencesByEntity();
    assertFalse("No @PortableSequence found in " + ENTITIES_INDEX + ": the index or the scan is broken",
                requiredSequences.isEmpty());

    Set<String> createdOnPostgresql = sequencesCreatedFor(DIALECT);

    List<String> missing = new ArrayList<>();
    requiredSequences.forEach((sequenceName, entityName) -> {
      if (!createdOnPostgresql.contains(sequenceName)) {
        missing.add(sequenceName + " (declared by " + entityName + ")");
      }
    });
    assertTrue("These @PortableSequence names have no <createSequence> in a changeSet that runs on " + DIALECT
        + " (dbms attribute absent or containing it). The HSQLDB suite cannot catch this; a PostgreSQL deployment"
        + " fails on the first insert. Missing: " + missing, missing.isEmpty());
  }

  private Map<String, String> portableSequencesByEntity() throws Exception {
    Map<String, String> sequences = new TreeMap<>();
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream index = classLoader.getResourceAsStream(ENTITIES_INDEX);
        BufferedReader reader = new BufferedReader(new InputStreamReader(index, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String className = line.trim();
        if (className.isEmpty() || className.startsWith("#")) {
          continue;
        }
        Class<?> entityClass = Class.forName(className, false, classLoader);
        for (Class<?> type = entityClass; type != null && type != Object.class; type = type.getSuperclass()) {
          for (Field field : type.getDeclaredFields()) {
            PortableSequence portableSequence = field.getAnnotation(PortableSequence.class);
            if (portableSequence != null) {
              sequences.put(portableSequence.name().toUpperCase(), className);
            }
          }
        }
      }
    }
    return sequences;
  }

  private Set<String> sequencesCreatedFor(String dialect) throws Exception {
    Set<String> sequences = new TreeSet<>();
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(false);
    factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // the changelogs carry no DOCTYPE
    factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    for (String changelog : CHANGELOG_FILES) {
      try (InputStream in = getClass().getClassLoader().getResourceAsStream(changelog)) {
        assertTrue("Changelog not found on the test classpath: " + changelog, in != null);
        Document document = factory.newDocumentBuilder().parse(in);
        NodeList changeSets = document.getElementsByTagName("changeSet");
        for (int i = 0; i < changeSets.getLength(); i++) {
          Element changeSet = (Element) changeSets.item(i);
          if (!runsOn(changeSet, dialect)) {
            continue;
          }
          NodeList children = changeSet.getChildNodes();
          for (int j = 0; j < children.getLength(); j++) {
            Node child = children.item(j);
            if (child.getNodeType() == Node.ELEMENT_NODE && "createSequence".equals(child.getNodeName())) {
              sequences.add(((Element) child).getAttribute("sequenceName").toUpperCase());
            }
          }
        }
      }
    }
    return sequences;
  }

  private boolean runsOn(Element changeSet, String dialect) {
    String dbms = changeSet.getAttribute("dbms"); // "" when absent (DOM contract), never null
    if (dbms.isBlank()) {
      return true;
    }
    for (String declared : dbms.split(",")) {
      if (declared.trim().equalsIgnoreCase(dialect)) {
        return true;
      }
    }
    return false;
  }
}
