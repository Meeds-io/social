<template>
  <v-card
    color="transparent"
    min-width="250px"
    width="100%"
    height="250px"
    class="d-flex align-end justify-end"
    flat>
    <div class="confeti-animation"></div>
  </v-card>
</template>
<style lang="scss">
  $particles: 180;
  $width: 250;
  $height: 250;

  // Array of colours to randomly populate the particle colours.
  $colors: 
    #4ad66d, #ffe169, #ff8fa3, #20a8ea, #C155F4, #F7A35B, #A0C7FF, #FD6A6A, #059d98, #b7efc5,
    #dbb42c, #c9184a, #1273d4, #E65ABC, #00FF56, #B1F6FF, #FFFF46, #26a855, #f10000, #208b3a,
    #c9a227, #ffccd5, #134d9b, #E66CDC, #58D68B, #5CE6D3, #f16a27, #ac1c1e, #eda3ff, #1a7431,
    #a47e1b, #ff4d6d, #62b0de, #FF97D0, #92e03a, #f44336, #3d6d8a, #E0A5FF, #FF9DB8, #808080;

  // Get the random colour
  @function random-color() {
    @return nth($colors, random(40));
  }

  // Create the explosion...
  $box-shadow: ();
  @for $i from 0 through $particles {
    $box-shadow: $box-shadow,
      random($width)-$width /
        2 +
        px
        random($height)-$height /
        2 +
        px
        #{random-color()};
  }

  .confeti-animation::before {
    position: absolute;
    content: '';
    width: 4px;
    height: 7px;
    animation: 1.5s explode ease-out 120 backwards,
      1.5s gravity ease-in 120 backwards;
  }

  @keyframes explode {
    to {
      box-shadow: $box-shadow;
    }
  }

  @keyframes gravity {
    to {
      transform: translateY(-75px);
      opacity: 0;
    }
  }
</style>