<script setup>
import {onMounted, ref} from "vue";

let hasMoved = false;
let startX = 0, startY = 0;
const scale = ref(1);
const posX = ref(0);
const posY = ref(0);
let mouseDown = false;
let lastPosX = 0;
let previousCenter = {};
let lastPosY = 0;

onMounted(() => {
  const wrapper = document.getElementById("dragWrapper");

  let multiTouch = false;
  let startDistance = null;
  let startScale = 1;

  wrapper.addEventListener("mousedown", e => {
    e.preventDefault();
    startX = e.x;
    startY = e.y;
    mouseDown = true;
  }, {capture: true});

  wrapper.addEventListener("mousemove", e => {
    if (mouseDown) {
      posX.value = e.x - startX + lastPosX;
      posY.value = e.y - startY + lastPosY;
      hasMoved = true;
    }
  });

  wrapper.addEventListener("mouseup", e => {
    mouseDown = false;
    lastPosX = posX.value;
    lastPosY = posY.value;
  });

  wrapper.addEventListener("click", e => {
    if (hasMoved) {
      e.preventDefault();
      e.stopPropagation();
    }
    hasMoved = false;
  }, {capture: true});

  wrapper.addEventListener("wheel", e => {
    if (e.deltaY < 0) {
      scale.value *= 1.1;
    } else if (e.deltaY > 0) {
      scale.value /= 1.1;
    }
  })

  wrapper.addEventListener('touchstart', function (e) {
    if (e.touches.length === 2) {
      multiTouch = true;
      startDistance = getDistance(e.touches);
      startScale = scale.value;
    } else {
      multiTouch = false;
      startX = e.touches[0].pageX;
      startY = e.touches[0].pageY;
    }
  });

  wrapper.addEventListener('touchmove', function (e) {
    e.preventDefault();
    if (multiTouch && e.touches.length === 2) {
      const center = getCenter(e.touches);

      const currentDistance = getDistance(e.touches);
      const ratio = currentDistance / startDistance;
      scale.value = startScale * ratio;
      posX.value += center.x - previousCenter.x;
      posY.value += center.y - previousCenter.y;

      previousCenter = center;
    } else if (!multiTouch && e.touches.length === 1) {
      posX.value = e.touches[0].pageX - startX + lastPosX;
      posY.value = e.touches[0].pageY - startY + lastPosY;
    }
  });

  wrapper.addEventListener('touchend', function (e) {
    if (!multiTouch) {
      lastPosX = posX.value;
      lastPosY = posY.value;
    }
  });

  function getDistance(touches) {
    const diffX = touches[0].pageX - touches[1].pageX;
    const diffY = touches[0].pageY - touches[1].pageY;
    return Math.sqrt(diffX * diffX + diffY * diffY);
  }

  function getCenter(touches) {
    const diffX = touches[0].pageX - touches[1].pageX;
    const diffY = touches[0].pageY - touches[1].pageY;
    return {
      x: touches[1].pageX + 0.5 * diffX,
      y: touches[1].pageY + 0.5 * diffY
    };
  }
});
</script>

<template>
  <div id="dragWrapper" style="pointer-events: auto">
    <div id="drag" :style="{transform: `translate(${posX}px, ${posY}px) scale(${scale})`}">
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
#dragWrapper {
  background: whitesmoke;
  width: 100vw;
  height: calc(100vh - 200px);
  position: relative;
  overflow: hidden;
}

#drag {
  touch-action: none;
  height: 300px;
  width: 200px;
  position: absolute;
  left: 0;
  top: 0;
}
</style>