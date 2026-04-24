export function createElement(tag, options = {}) {
    const {
        className = '',
        textContent = '',
        html = '',
        attributes = {},
        children = [],
        events = {}
    } = options;

    const el = document.createElement(tag);

    if (className) el.className = className;
    if (textContent) el.textContent = textContent;
    if (!textContent && html) el.innerHTML = html;

    Object.entries(attributes).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
            el.setAttribute(key, value);
        }
    });

    children.forEach(child => {
        if (child instanceof Node) {
            el.appendChild(child);
        }
    });

    Object.entries(events).forEach(([event, handler]) => {
        if (typeof handler === 'function') {
            el.addEventListener(event, handler);
        }
    });

    return el;
}

export const createDiv = (opts = {}) => createElement('div', opts);
export const createH1 = (opts = {}) => createElement('h1', opts);
export const createH2 = (opts = {}) => createElement('h2', opts);
export const createH3 = (opts = {}) => createElement('h3', opts);
export const createH4 = (opts = {}) => createElement('h4', opts);
export const createH5 = (opts = {}) => createElement('h5', opts);
export const createP = (opts = {}) => createElement('p', opts);
export const createButton = (opts = {}) => createElement('button', opts);
export const createInput = (opts = {}) => createElement('input', opts);
export const createSelect = (opts = {}) => createElement('select', opts);
export const createOption = (opts = {}) => createElement('option', opts);
export const createForm = (opts = {}) => createElement('form', opts);
export const createA = (opts = {}) => createElement('a', opts);
export const createUl = (opts = {}) => createElement('ul', opts);
export const createLi = (opts = {}) => createElement('li', opts);
export const createLabel = (opts = {}) => createElement('label', opts);

export function createDefaultOption(text, disabled = true, selected = false) {
    return createOption({
        textContent: text,
        attributes: {
            disabled: disabled ? "true" : undefined,
            selected: selected ? "true" : undefined,
            hidden: disabled ? "true" : undefined,
            value: ""
        }
    });
}

function withEvents(events) {
    return {events};
}

export const onClick = (handler) => withEvents({click: handler});
export const onChange = (handler) => withEvents({change: handler});
export const onInput = (handler) => withEvents({input: handler});
export const onKeyPress = (handler) => withEvents({keypress: handler});
export const onSubmit = (handler) => withEvents({submit: handler});
export const onFocus = (handler) => withEvents({focus: handler});
export const onBlur = (handler) => withEvents({blur: handler});
