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

package org.exoplatform.social.core.storage.cache;

import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.services.cache.ExoCache;

import java.io.Serializable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class FutureExoCache<K extends Serializable, V, C> extends FutureCache<K, V, C>
{

   /** . */
   private final ExoCache<K, V> cache;

   public FutureExoCache(Loader<K, V, C> loader, ExoCache<K, V> cache)
   {
      super(loader);

      //
      this.cache = cache;
   }

   public void clear()
   {
      cache.clearCache();
   }

   public void remove(K key)
   {
      cache.remove(key);
   }

   @Override
   protected V get(K key)
   {
      return cache.get(key);
   }

   @Override
   protected void put(K key, V entry)
   {
      cache.put(key, entry);
   }

   @Override
   protected void putOnly(K key, V value)
   {
      cache.putLocal(key, value);
   }
}
