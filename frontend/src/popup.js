import {createVNode, render} from "vue";

let closeCallback = null;

export function openPopup(component, appContext, props) {
    closePopup();

    const vNode = createVNode(component, props);
    vNode.appContext = {...appContext}
    render(vNode, document.body);
    closeCallback = props.onClose;
}

export function closePopup() {
    render(null, document.body);
    if (closeCallback) {
        closeCallback();
        closeCallback = null;
    }
}

document.addEventListener("keydown", e => {
    if (e.key === "Escape" && document.activeElement === document.body) {
        closePopup();
    }
});
