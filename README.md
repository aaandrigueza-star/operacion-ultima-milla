# Operacion Ultima Milla

Backend Spring Boot para gestionar pedidos con prioridades, estados e inventario.

## Ejecutar

```powershell
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints

| Metodo | Ruta | Funcion |
| --- | --- | --- |
| GET | `/productos` | Consultar productos y stock disponible |
| POST | `/pedidos` | Crear un pedido pendiente |
| GET | `/pedidos` | Listar pedidos |
| GET | `/pedidos/pendientes` | Listar pendientes |
| GET | `/pedidos/urgentes` | Listar urgentes |
| GET | `/pedidos/estado?estado=CONFIRMADO` | Filtrar por estado |
| GET | `/pedidos/resumen` | Ver resumen de estados y urgencias |
| GET | `/pedidos/siguiente` | Obtener el siguiente pedido pendiente |
| GET | `/pedidos/en-riesgo` | Detectar pendientes sin stock suficiente |
| PUT | `/pedidos/{id}/confirmar` | Confirmar y descontar stock |
| PUT | `/pedidos/{id}/cancelar` | Cancelar y devolver stock si estaba confirmado |
| PUT | `/pedidos/{id}/despachar` | Despachar un pedido confirmado |

## Reglas

- Productos iniciales: Nike Air Max (`1`, 20 unidades), Adidas Ultraboost (`2`, 5 unidades), Puma Suede (`3`, 0 unidades), Converse Chuck Taylor (`4`, 12 unidades) y New Balance 574 (`5`, 8 unidades).
- La creacion valida cliente, producto, cantidad y prioridad.
- La falta de stock no elimina la solicitud: el pedido permanece `PENDIENTE` y aparece en `/pedidos/en-riesgo`.
- Solo `PENDIENTE` puede confirmarse, solo `CONFIRMADO` puede despacharse.
- Cancelar un pedido confirmado devuelve su stock.
- La prioridad se ordena `URGENTE`, `ALTA`, `MEDIA`, `BAJA`; en empate gana el pedido con menor `id`.
- Las operaciones de inventario son sincronizadas para impedir que dos confirmaciones consuman las mismas unidades.

## Boss final

Un pedido urgente de 20 unidades para un producto con 12 unidades se conserva como `PENDIENTE`, no descuenta stock y se marca mediante `/pedidos/en-riesgo`. Asi la solicitud no se pierde y puede atenderse cuando exista inventario.

## Pruebas

```powershell
.\mvnw.cmd test
```