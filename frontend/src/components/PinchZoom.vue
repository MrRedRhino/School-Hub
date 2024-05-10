<script setup>
import {onMounted, ref} from "vue";

const zoomSpeed = 0.8;
let hasMoved = false;
const scale = ref(1);
const posX = ref(0);
const posY = ref(0);
let mouseDown = false;
let lastPosX = 0;
let lastPosY = 0;
let lastTouches = null;

onMounted(() => {
  const wrapper = document.getElementById("dragWrapper");
  posX.value = lastPosX = (wrapper.clientWidth) / 2;
  posY.value = lastPosY = (wrapper.clientHeight) / 2;
  scale.value = Math.min(0.7, wrapper.clientWidth / 1400);

  wrapper.addEventListener("mousedown", e => {
    e.preventDefault();
    mouseDown = true;
  }, {capture: true});

  wrapper.addEventListener("mousemove", e => {
    if (mouseDown) {
      posX.value += e.x - lastPosX;
      posY.value += e.y - lastPosY;
      hasMoved = true;
    }
    lastPosX = e.x;
    lastPosY = e.y;
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
    const pointer = {
      x: e.pageX - wrapper.offsetLeft,
      y: e.pageY - wrapper.offsetTop
    };
    const target = {
      x: (pointer.x - posX.value) / scale.value,
      y: (pointer.y - posY.value) / scale.value
    };

    scale.value *= e.deltaY > 0 ? zoomSpeed : 1 / zoomSpeed;

    posX.value = -target.x * scale.value + pointer.x;
    posY.value = -target.y * scale.value + pointer.y;
  });

  wrapper.addEventListener('touchstart', function (e) {
    lastTouches = e.touches;
  });

  wrapper.addEventListener('touchmove', function (e) {
    e.preventDefault();

    if (lastTouches.length === 1) {
      posX.value += (e.touches[0].pageX - lastTouches[0].pageX);
      posY.value += (e.touches[0].pageY - lastTouches[0].pageY);

    } else if (lastTouches.length === 2) {
      const pointer = getCenter(e.touches);
      const target = {
        x: (pointer.x - posX.value) / scale.value,
        y: (pointer.y - posY.value) / scale.value
      };

      scale.value *= getDistance(e.touches) / getDistance(lastTouches);

      const lastCenter = getCenter(lastTouches);
      posX.value = -target.x * scale.value + pointer.x + pointer.x - lastCenter.x;
      posY.value = -target.y * scale.value + pointer.y + pointer.y - lastCenter.y;
    }
    lastTouches = e.touches;
  });

  function getDistance(touches) {
    const diffX = touches[0].pageX - touches[1].pageX;
    const diffY = touches[0].pageY - touches[1].pageY;
    return Math.sqrt(diffX * diffX + diffY * diffY);
  }

  function getCenter(touches) {
    const pointer1 = {
      x: touches[0].pageX - wrapper.offsetLeft,
      y: touches[0].pageY - wrapper.offsetTop
    };
    const pointer2 = {
      x: touches[1].pageX - wrapper.offsetLeft,
      y: touches[1].pageY - wrapper.offsetTop
    };

    const diffX = pointer1.x - pointer2.x;
    const diffY = pointer1.y - pointer2.y;
    return {
      x: pointer2.x + 0.5 * diffX,
      y: pointer2.y + 0.5 * diffY
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
  position: relative;
  overflow: hidden;
  height: 100%;
}

#drag {
  touch-action: none;
  position: absolute;
  left: 0;
  top: 0;
}
</style>