<!--

  This file is part of the Meeds project (https://meeds.io/).

  Copyright (C) 2023 Meeds Association contact@meeds.io

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

-->
<template>
  <v-card
    color="transparent"
    min-width="400px"
    width="100%"
    height="250px"
    class="confeti-animation-parent d-flex align-end justify-center pb-2"
    flat>
    <div class="confeti-animation">
      <div
        v-for="index in 400"
        :key="index"
        class="particle"></div>
    </div>
  </v-card>
</template>
<style lang="scss">
  $length: 400;
  $particles: 400;
  $width: 400;
  $height: 250;
  $timing: 1.3s;
  $repeat: 1;
  $colors: 
    #4ad66d, #ffe169, #ff8fa3, #20a8ea, #C155F4, #F7A35B, #A0C7FF, #FD6A6A, #059d98, #b7efc5,
    #dbb42c, #c9184a, #1273d4, #E65ABC, #00FF56, #B1F6FF, #FFFF46, #26a855, #f10000, #208b3a,
    #c9a227, #ffccd5, #134d9b, #E66CDC, #58D68B, #5CE6D3, #f16a27, #ac1c1e, #eda3ff, #1a7431,
    #a47e1b, #ff4d6d, #62b0de, #FF97D0, #92e03a, #f44336, #3d6d8a, #E0A5FF, #FF9DB8, #808080;

  @function random-color() {
    @return nth($colors, random(40));
  }

  @mixin create-path() {
    $end-point-w: random($length) - ($length / 2);
    $end-point-h: random($height) - ($height / 2);
    $bezier-w: random($length) - ($length / 2);
    $bezier-h: random($height) - ($height / 2);
    offset-path: path(
      "M 0, 0 " + 
      "q #{$bezier-w}, #{$bezier-h}, #{$end-point-w}, #{$end-point-h} "
    );
  }

  .confeti-animation-parent {
    transform: translateY(50%);
  }

  .confeti-animation::before {
    position: absolute;
    content: '';
    transform: translateY(-50%, -50%);
  }

  .particle {
    width: 10px;
    height: 4px;
    position: absolute;
    top: 50%;
    left: 50%;
    pointer-events: none;
    offset-distance: 0;
    opacity: 0;

    @for $i from 0 through $particles {
      &:nth-of-type(#{$i}) {
        @include create-path();
        $col-amount: 500;
        animation: $timing move ease-out $repeat backwards,
                   $timing fade ease-in-out $repeat backwards;
        background: #{random-color()};
      }
    }
  }

  @keyframes move {
    0% {
      offset-distance: 0%;
    }
    100% {
      offset-distance: 100%;
    }
  }

  @keyframes fade {
    from {
      opacity: 1;
    }
    to {
      opacity: 0;
    }
  }
</style>