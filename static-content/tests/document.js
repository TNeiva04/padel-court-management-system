const g = typeof global !== "undefined" ? global : window;

if (typeof g.document === "undefined") {
    // Node.js: mock completo
    g.document = {
        createElement(tagName) {
            const element = {
                tagName: tagName.toUpperCase(),
                textContent: "",
                children: [],
                style: {},
                attributes: {},
                _eventHandlers: {},
                _value: "",

                appendChild(child) {
                    this.children.push(child);
                },
                replaceChildren(...nodes) {
                    this.children = nodes;
                },
                setAttribute(name, value) {
                    this.attributes[name] = value;
                    if (name === "type") this.type = value;
                    if (name === "href" && this.tagName === "A") {
                        this._href = value;
                    }
                },
                getAttribute(name) {
                    return this.attributes[name];
                },
                addEventListener(event, handler) {
                    this._eventHandlers[event] = handler;
                },
                removeEventListener(event) {
                    delete this._eventHandlers[event];
                },
                dispatchEvent(event) {
                    if (this._eventHandlers[event.type]) {
                        this._eventHandlers[event.type](event);
                    }
                },

                get value() {
                    return this._value;
                },
                set value(val) {
                    this._value = val;
                }
            };

            if (tagName.toLowerCase() === "a") {
                Object.defineProperty(element, 'href', {
                    get() {
                        return this._href || "";
                    },
                    set(value) {
                        this._href = value;
                    },
                    configurable: true,
                    enumerable: true
                });
            }

            if (tagName.toLowerCase() === "input") {
                element.type = undefined;
            }

            return element;
        },
        querySelector: () => null,
        querySelectorAll: () => [],
        getElementsByTagName: () => [],
        documentElement: {},
        body: {}
    };
} else {
    // Navegador: patch createElement para adicionar href em <a> e value em <input>
    if (!g.document.createElement.__patchedForTest) {
        const originalCreateElement = g.document.createElement.bind(g.document);
        g.document.createElement = function(tagName) {
            const el = originalCreateElement(tagName);

            if (tagName.toLowerCase() === "a") {
                let _href = "";
                Object.defineProperty(el, "href", {
                    get() { return _href; },
                    set(value) { _href = value; },
                    configurable: true,
                    enumerable: true
                });
            }

            if (tagName.toLowerCase() === "input") {
                let _value = "";
                Object.defineProperty(el, "value", {
                    get() { return _value; },
                    set(val) { _value = val; },
                    configurable: true,
                    enumerable: true
                });
            }

            // Patch addEventListener to store handlers
            const originalAddEventListener = el.addEventListener;
            el._eventHandlers = {};
            el.addEventListener = function(event, handler) {
                el._eventHandlers[event] = handler;
                if (originalAddEventListener) {
                    originalAddEventListener.call(el, event, handler);
                }
            };

            return el;
        };
        g.document.createElement.__patchedForTest = true;
    }
}

g.document.querySelector = g.document.querySelector || (() => null);
g.document.querySelectorAll = g.document.querySelectorAll || (() => []);
