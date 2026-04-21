# Culiatum RP

`Culiatum RP` es un mod server-side para Fabric `1.21.11` enfocado en las reglas y sistemas PvP del servidor de Culiatum. Esta primera entrega es una `alpha` orientada a sentar la base de combate, escape controlado y misiones de cazarecompensas.

## Estado del proyecto

- Version actual: `0.1.0-alpha`
- Loader objetivo: `Fabric Loader 0.18.4`
- Java objetivo: `21`
- Compatible con `Mod Menu` mediante metadata en `fabric.mod.json`

## Que incluye la alpha

- Bloqueo de comandos de teleport configurados durante combate
- Bloqueo total de vuelo con elytra
- `Recall Potion` bebible para volver al spawn
- `Radar` configurable por comandos para asignar objetivos de cazarecompensas
- Cooldown de uso del radar por action bar
- Configuracion basica en `config/culiatum-pvp.properties`

## Vision del mod

Este mod esta pensado para cubrir necesidades concretas del servidor de Culiatum. Su enfoque no es ser un framework generico de PvP, sino ofrecer reglas claras, herramientas administrables y items con comportamiento definido para el gameplay del servidor.

Muchas propiedades de los items y sistemas pueden editarse mediante comandos y configuracion, pero sus funciones base no estan pensadas para ser reemplazadas por completo desde datapacks o configs externas.

## Relacion con Culiatum Economy

`Culiatum RP` esta pensado para convivir con `culiatum-economy`, aunque no es estrictamente necesario instalar ambos mods juntos. La idea es que a futuro compartan progresion, misiones, herramientas administrativas y sistemas de servidor sin forzar una dependencia dura entre ellos.

## Compatibilidad

- Funciona sin `culiatum-economy`
- No esta pensado para ser compatible con mods como `aicheye combat tagging`, porque cubren una funcion muy similar
- Al usar otro mod que haga combat tagging o bloquee comandos en combate, pueden producirse solapamientos de comportamiento

## Comandos de admin

Los sistemas editables por comandos arrancan con:

```mcfunction
/culiatumpvp radar give <jugador> [cantidad]
/culiatumpvp radar set <hunter> <target> <minutes> [label]
/culiatumpvp radar clear <hunter>
/culiatumpvp radar status <hunter>
```

## Desarrollo y flujo de ramas

La rama principal debe mantenerse legible y auditable. La convencion del repositorio es:

- `main` para la linea estable del proyecto
- `feature/*` para nuevas funcionalidades
- otros prefijos claros cuando aplique, como `fix/*` o `docs/*`
- evitar prefijos generados automaticamente como `codex/*`

Para cambios nuevos, la idea es trabajar desde `worktrees` en ramas dedicadas y luego integrar a `main`.

## Build local

```powershell
.\gradlew.bat build
```

El jar remapeado queda en:

```text
build/libs/culiatum-pvp-0.1.0.jar
```
