import {ref} from "vue";

export const activeElement = ref(null);
export const hoveredElement = ref(null);

function updateActiveElement() {
    const currentElement = document.activeElement;
    activeElement.value = currentElement === document.body ? null : currentElement;
}

function updateHoveredElement(event) {
    hoveredElement.value = event.target;
}

addEventListener("focusin", updateActiveElement);
addEventListener("focusout", updateActiveElement);
addEventListener("mouseover", updateHoveredElement);
