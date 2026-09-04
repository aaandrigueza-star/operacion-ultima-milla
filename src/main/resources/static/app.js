const productsElement = document.querySelector('#products');
const productSelect = document.querySelector('#productoId');
const ordersElement = document.querySelector('#orders');
const messageElement = document.querySelector('#message');
const productCountElement = document.querySelector('#product-count');
let products = [];

async function request(url, options = {}) {
    const response = await fetch(url, options);
    const contentType = response.headers.get('content-type') || '';
    const data = contentType.includes('json') ? await response.json() : null;
    if (!response.ok) {
        throw new Error(data?.message || data?.error || `Error ${response.status}`);
    }
    return data;
}

async function loadProducts() {
    try {
        products = await request('/productos');
        productCountElement.textContent = products.length;
        productsElement.innerHTML = products.map(product => `
            <article class="product-card">
                <div class="product-art">${brandInitials(product.nombre)}</div>
                <p class="product-name">${product.nombre}</p>
                <p class="stock ${product.stock === 0 ? 'out' : ''}">${product.stock === 0 ? 'Agotado' : `${product.stock} unidades disponibles`}</p>
            </article>
        `).join('');
        productSelect.innerHTML = '<option value="">Selecciona un modelo</option>' + products
            .filter(product => product.stock > 0)
            .map(product => `<option value="${product.id}">${product.nombre} · ${product.stock} disponibles</option>`)
            .join('');
    } catch (error) {
        productsElement.innerHTML = `<p class="loading">No se pudo cargar el catálogo: ${error.message}</p>`;
    }
}

function brandInitials(name) {
    return name.split(' ').slice(0, 2).map(word => word[0]).join('');
}

async function loadOrders() {
    try {
        const orders = await request('/pedidos');
        if (!orders.length) {
            ordersElement.innerHTML = '<p class="loading">Todavía no hay pedidos.</p>';
            return;
        }
        ordersElement.innerHTML = orders.map(order => {
            const product = products.find(item => item.id === order.productoId);
            const state = order.estado.toLowerCase();
            return `
                <article class="order-row">
                    <span class="order-id">#${order.id}</span>
                    <div class="order-info"><strong>${product?.nombre || `Producto ${order.productoId}`}</strong><span>${order.cliente} · ${order.cantidad} unidad(es) · ${order.prioridad}</span></div>
                    <span class="badge ${state}">${order.estado}</span>
                    <div class="actions">${actionsFor(order)}</div>
                </article>
            `;
        }).join('');
        ordersElement.querySelectorAll('[data-action]').forEach(button => button.addEventListener('click', changeOrder));
    } catch (error) {
        ordersElement.innerHTML = `<p class="loading">No se pudieron cargar los pedidos: ${error.message}</p>`;
    }
}

function actionsFor(order) {
    if (order.estado === 'PENDIENTE') {
        return `<button class="action" data-action="confirmar" data-id="${order.id}">Confirmar</button><button class="action" data-action="cancelar" data-id="${order.id}">Cancelar</button>`;
    }
    if (order.estado === 'CONFIRMADO') {
        return `<button class="action" data-action="despachar" data-id="${order.id}">Despachar</button><button class="action" data-action="cancelar" data-id="${order.id}">Cancelar</button>`;
    }
    return '';
}

async function changeOrder(event) {
    const button = event.currentTarget;
    try {
        await request(`/pedidos/${button.dataset.id}/${button.dataset.action}`, { method: 'PUT' });
        showMessage('Pedido actualizado correctamente.');
        await Promise.all([loadProducts(), loadOrders()]);
    } catch (error) {
        showMessage(error.message, true);
    }
}

document.querySelector('#order-form').addEventListener('submit', async event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const payload = {
        cliente: form.get('cliente'),
        productoId: Number(form.get('productoId')),
        cantidad: Number(form.get('cantidad')),
        prioridad: form.get('prioridad')
    };
    try {
        await request('/pedidos', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
        event.currentTarget.reset();
        showMessage('Pedido creado. Ya aparece en seguimiento.');
        await Promise.all([loadProducts(), loadOrders()]);
        document.querySelector('.orders-panel').scrollIntoView({ behavior: 'smooth', block: 'start' });
    } catch (error) {
        showMessage(error.message, true);
    }
});

function showMessage(message, isError = false) {
    messageElement.textContent = message;
    messageElement.className = `message${isError ? ' error' : ''}`;
}

document.querySelector('#refresh-products').addEventListener('click', loadProducts);
document.querySelector('#refresh-orders').addEventListener('click', loadOrders);
loadProducts().then(loadOrders);
