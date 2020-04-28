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

//Array.lastIndexOf()
if (!Array.prototype.lastIndexOf) {
  Array.prototype.lastIndexOf = function(elt /*, from*/)  {
    var len = this.length;
    var from = Number(arguments[1]);
    if (isNaN(from)) {
      from = len - 1;
    }
    else {
      from = (from < 0)
           ? Math.ceil(from)
           : Math.floor(from);
      if (from < 0) {
        from += len;
      } else if (from >= len) {
        from = len - 1;
      }
    }

    for (; from > -1; from--) {
      if (from in this &&
          this[from] === elt)
        return from;
    }
    return -1;
  };
}

//Array.indexOf()
if (!Array.prototype.indexOf) {
  Array.prototype.indexOf = function(obj){
    for(var i = 0, l = this.length; i < l; i++){
      if(this[i] === obj){
        return i;
      }
    }
    return -1;
  }
}