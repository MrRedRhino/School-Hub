import {createVNode, render} from "vue";

export function openPopup(component, appContext, props) {
    closePopup();

    const vNode = createVNode(component, props);
    vNode.appContext = {...appContext}
    render(vNode, document.body);
}

export function closePopup() {
    render(null, document.body);
}