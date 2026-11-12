# De los diagramas de la catedra al codigo

Estructura de paquetes (sin el prefijo `md.` de la catedra; en Eclipse se puede agregar con *Refactor > Rename*):

| Paquete | Clases | Responsabilidad |
|---|---|---|
| `aplicacion` | `AplicacionConsultas`, `ContextoAplicacion` | arranque; datos, motor vial, escenario, sesion y conductores simulados (hilos) |
| `datos` / `datos.factory` | `Datos`, `ConexionBD`, `CargarParametros`, sub-factories | leer usuarios, vehiculos y servicios de la BD o de archivos |
| `logica` | `Movilidad`, `PlanificacionViajeService`, `ValidacionUbicaciones`, `Asignacion` | reglas de dominio y aplicacion |
| `rutas.api` | `PlanificadorRutas`, `RutaCalculada`, `PuntoRuta`, `RutasViaje`, `RutaNoDisponibleException` | contrato del modulo de rutas |
| `rutas.impl` | `GraphHopperPlanificador`, `PlanificadorRutasFactory` | adaptador de GraphHopper y fabrica (`rutas.motor`) |
| `simulacion` | `GeneradorUbicacionesViales`, `InicializadorEscenario` | posiciones aleatorias sobre calle; estado inicial de conductores |
| `modelo` | `GestorViajes` | cola de viajes y un hilo por conductor simulado (no se modifico) |
| `usuario`, `vehiculo`, `servicio`, `ubicacion`, `viajes` | dominio | `Vehiculo.ubicacion`, `Conductor.vehiculoCompatible` (nuevos) |
| `ui.api` | `UIEngine`, `VisualizadorMapa`, `ReceptorPuntoMapa` | contratos de la interfaz |
| `ui.impl.javafx` | `MainApp`, `JavaFXUserInterface`, `VistasFX` | arranque JavaFX y carga de FXML |
| `ui.impl.javafx.controller` | `LoginController`, `PanelClienteController`, `PanelConductorController` | eventos de las pantallas |
| `ui.impl.javafx.map` | `VisualizadorRutasLeaflet`, `PuenteSeleccionMapa` | WebView + Leaflet y el puente JavaScript -> Java |

Recursos (carpeta `src`, que Maven copia al classpath): `ui/javafx/view/*.fxml`, `ui/map/mapa.html`, `ui/map/leaflet/`.
Datos de entrada: `mapas/puerto_madryn_ampliado.osm` (copiarlo de la carpeta de Drive); la cache del grafo se genera sola.

## Diagramas de secuencia

| Diagrama | Donde esta en el codigo |
|---|---|
| 01 inicio de sesion | `LoginController.handleIngresar` (activa el rol, `contexto.setUsuarioLogueado`, `VistasFX.mostrar` -> `FXMLLoader.load` -> `initialize()`) |
| 02 enlace JavaFX-Leaflet | `PanelClienteController.initialize` -> `new VisualizadorRutasLeaflet(mapaWeb)` + `configurarSeleccion(this::recibirPuntoMapa)`; al llegar a `SUCCEEDED` se publica `javaBridge` |
| 03 marcar origen / 04 marcar destino | `handleCambiarModoUbicaciones` -> `iniciarSeleccionMapa` -> `activarSeleccion` (ida); `mapa.html` clic -> `PuenteSeleccionMapa.seleccionarPunto` -> `recibirPuntoMapa` (vuelta); `ValidacionUbicaciones.validarYAjustar` + `mostrarMarcadorSeleccion` (respuesta) |
| 05 cotizar | `handleCotizar`: `validarCoordenadas`, `validarYAjustar`, Haversine, `Servicio.calcularCosto` |
| 06 planificar viaje | `handleSolicitarViaje`: `Movilidad.seleccionarConductor`, `PlanificacionViajeService.planificar`, `Viaje.solicitar`, cola de `GestorViajes` |
| 07 dibujar y finalizar | `dibujar` -> `VisualizadorMapa.mostrar` -> `window.mostrarRutas`; los estados `ACEPTADO/INICIADO/FINALIZADO` los avanzan los hilos y llegan por `alCambiarViaje` |

## Diferencias con los diagramas (a proposito)
- Las transiciones del viaje no son inmediatas: las simulan los hilos de los conductores (`GestorViajes`).
- Si el viaje lo toma un conductor distinto del elegido por cercania, la ruta se recalcula con su vehiculo.
- El perfil de circulacion implementado es AUTO; los servicios de MOTO avisan que esta pendiente.
