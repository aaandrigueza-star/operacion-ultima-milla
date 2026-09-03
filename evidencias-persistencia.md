# Evidencias de persistencia

## ¿Qué información desapareció?

Los datos de los productos y pedidos registrados durante la ejecución de la aplicación desaparecieron al reiniciarla. Esto incluye los elementos que se habían agregado o actualizado en las listas de la aplicación.

## ¿Dónde estaba almacenada?

La información estaba almacenada temporalmente en listas en memoria dentro de los servicios de la aplicación, no en una base de datos ni en otro almacenamiento persistente.

## ¿Por qué reiniciar la aplicación afecta a una lista en memoria?

Al reiniciar la aplicación, la JVM finaliza y se liberan los objetos que estaban en memoria. Cuando Spring Boot vuelve a iniciar, crea nuevas instancias de los servicios y sus listas comienzan vacías, por lo que no recuperan los datos de la ejecución anterior.

## ¿Qué debería cambiar para conservarla?

La aplicación debe usar persistencia duradera: definir entidades JPA, repositorios que extiendan `JpaRepository` y una base de datos configurada. Así, los servicios guardarán y consultarán productos y pedidos desde la base de datos en lugar de depender de listas en memoria.
