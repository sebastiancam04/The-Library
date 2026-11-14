# Stage 3 — Informe de auditoría final

**Proyecto:** The-Library
**Documento de referencia:** `refactor.stage-3.pdf`, versión 1.6, 40 puntos
**Fecha:** 14 de noviembre de 2026
**Último commit confirmado:** `3f48835` — `Stage 3: document gRPC API and update class diagram`
**Estado:** Auditoría preliminar; cierre sujeto a los controles finales.

## 1. Alcance y límite de evidencia

Este informe compara los 12 ítems de la rúbrica oficial con los resultados reportados durante los commits 13–19, los archivos fuente compartidos en la conversación y el código PlantUML aportado. No se obtuvo una copia descargable del árbol **actual** del repositorio para comparar automáticamente todas las líneas. Por tanto, los resultados de consola y las pruebas son evidencia válida de ejecución, pero no reemplazan la revisión directa del `build.gradle` y de `docs/prompts-stage-3.md`. No se debe declarar una nota final sin completar las verificaciones pendientes.

## 2. Rúbrica: 40 puntos

| N.º | Criterio | Máx. | Evidencia disponible | Estado |
|---|---|---:|---|---|
| 1 | Gradle: plugin Protobuf 0.10.1, gRPC 1.85.1, protoc 3.25.9, `runClient` | 2 | `clean build`, generación Protobuf y `runClient` se ejecutaron; versiones exactas sin cotejo final del archivo | Funcionamiento comprobado; cotejo de versiones pendiente |
| 2 | Contrato `member.proto` y clases generadas | 3 | Contrato de tres RPC ampliado a `SearchMembers`; tests compilan con stubs generados | Probado de forma indirecta; revisar declaración final |
| 3 | `MemberService.findById` lanza `NotFoundException` | 1 | `MemberGrpcTest` exitoso y cliente con ID inexistente | Comprobado |
| 4 | `MemberGrpcService`, tres RPC guiados, traducción de estados | 5 | Prueba de registro/listado/consulta; manejo de NOT_FOUND; código presentado incluye INVALID_ARGUMENT y FAILED_PRECONDITION | Verificación funcional parcial; comprobar los tres mapeos |
| 5 | `App`: gRPC 127.0.0.1:50051 más REST 7070, cierre del servidor | 3 | En commits anteriores se confirmó REST HTTP 200 y puerto gRPC 50051 | Comprobado anteriormente; repetir después del último commit |
| 6 | `MemberClient`: métodos guiados, deadline de cinco segundos, errores | 5 | Cliente y casos de éxito/error ejecutados; código entregado utiliza `withDeadlineAfter` y `close()` | Evidencia sólida; comprobar configuración final |
| 7 | `MemberClientApp` y salida código 1 | 1 | `runClient`, `--args="1 2"`, `--args="9999"`, argumento no numérico y servidor detenido probados anteriormente | Comprobado |
| 8 | `MemberGrpcTest`: SQLite en memoria y gRPC in-process | 3 | `test --tests MemberGrpcTest`: BUILD SUCCESSFUL | Comprobado |
| 9 | README: API gRPC, tabla y comandos | 1 | Diferencia de 96 líneas, sección y tabla añadidas; Commit 19 subido | Contenido comprobado |
| 10 | PlantUML: paquete `grpc`, clases generadas y relaciones | 1 | Archivo compartido: un `@startuml`, un `@enduml`, llaves equilibradas y clases requeridas | Contenido comprobado; renderizado visual pendiente |
| 11 | SearchMembers: 7 casos, DAO, servicio, adaptador y cliente | 10 | `test --tests MemberSearchTest`: BUILD SUCCESSFUL; implementación y pruebas compartidas | Prueba aprobada; confirmar el test final y sus aserciones |
| IA | Registro de prompts exactos en orden, modelo y archivos | 5 | Archivo modificado en los commits; últimos prompts compartidos | **Pendiente de auditoría literal del archivo completo** |
| | **TOTAL** | **40** | | |

**Interpretación:** Hay evidencia sustancial para los 35 puntos técnicos, pero no corresponde afirmar un `40/40` ni una nota numérica mientras no se verifique el registro de prompts, la imagen renderizada y los detalles de versiones.

## 3. SearchMembers — Matriz de aceptación

Los siguientes casos deben existir en `MemberSearchTest.java` y terminar correctamente con los tres miembros insertados en orden: Ada Lovelace, Grace Hopper, Alan Turing.

| Caso | Consulta | Resultado esperado |
|---|---|---|
| 1 | `ada` | Solo Ada Lovelace |
| 2 | `ADA` | Solo Ada Lovelace |
| 3 | ` ada ` | Solo Ada Lovelace |
| 4 | `a` | Ada, Grace y Alan, en ese orden por ID |
| 5 | `zzz` | Lista vacía, gRPC OK |
| 6 | `%` y `_` | Lista vacía para cada una; coincidencia literal |
| 7 | cadena vacía y espacio | `IllegalArgumentException` en cliente; `INVALID_ARGUMENT` en gRPC |

**Diseño:** la consulta parametrizada `instr(lower(name), lower(?)) > 0` con ORMLite `SelectArg` implementa la coincidencia literal insensible a mayúsculas ASCII; `orderBy("id", true)` mantiene el orden. La validación y el recorte de espacios pertenecen al servicio, y los códigos gRPC solo a cliente y adaptador.

## 4. Pruebas conocidas y pendientes

### Confirmadas durante el desarrollo
- `.\gradlew.bat clean test --tests MemberGrpcTest` — `BUILD SUCCESSFUL`.
- `.\gradlew.bat clean test --tests MemberSearchTest` — `BUILD SUCCESSFUL`.
- `.\gradlew.bat clean build` después del Commit 19 — `BUILD SUCCESSFUL`, 16 tareas.
- Comprobación anterior de REST, puerto gRPC y cliente, incluidos errores esperados.
- `git status` al final del Commit 19: `main` sincronizada con `origin/main`; solo `.idea/gradle.xml` y `.idea/vcs.xml` modificados localmente.

### Pendientes para cerrar el Stage 3
- [ ] Verificar literalmente en `build.gradle`: Protobuf 0.10.1, artefactos gRPC 1.85.1, protoc 3.25.9 y tarea `runClient`.
- [ ] Visualizar **sin errores** `docs/class-diagram.puml` en PlantUML.
- [ ] Revisar `docs/prompts-stage-3.md` completo: orden 13–20, nombre del modelo, texto verbatim y archivos modificados.
- [ ] Revisar la lista de aserciones del `MemberSearchTest` efectivamente versionado, incluidos `INVALID_ARGUMENT` y el orden por ID.
- [ ] Ejecutar nuevamente el servidor REST/gRPC y la CLI en dos terminales, con el último código.
- [ ] Revisar `git diff --cached --check`, los archivos preparados y `git status` antes del Commit 20.

## 5. Riesgos y observaciones

1. **No confundir compilación con corrección total.** `clean build` no verifica por sí mismo el renderizado PlantUML ni la exactitud del registro de IA.
2. **Posibles pérdidas de puntos en prompts.** La rúbrica asigna cinco puntos al archivo: debe registrar **todos** los prompts, con texto exacto, nombre de modelo y archivos, y coincidir con el código.
3. **Estados gRPC.** NOT_FOUND e INVALID_ARGUMENT tienen evidencia funcional. FAILED_PRECONDITION existe en la implementación propuesta, pero no fue sometido a un test específico documentado.
4. **Recursos.** El cliente cierra su `ManagedChannel` mediante `AutoCloseable` y la prueba detiene el servidor `in-process` en `finally`. Revisar de forma estática que el servidor principal cierre en su shutdown hook.
5. **Seguridad.** El laboratorio usa gRPC sin TLS ni autenticación y lo restringe a 127.0.0.1. Es aceptable para el contexto del ejercicio; no es una configuración para exponer a internet.
6. **README.** La sección gRPC fue agregada después del pie de créditos del README anterior. No afecta directamente el requisito funcional, pero puede reubicarse antes de los créditos como mejora editorial, si se desea.
7. **Ficheros IntelliJ.** No agregar `.idea/gradle.xml` ni `.idea/vcs.xml` al commit.

## 6. Protocolo de comprobación

Desde PowerShell, en la raíz del proyecto:

```powershell
# Compilación y pruebas
.\gradlew.bat clean build
.\gradlew.bat test --tests MemberGrpcTest
.\gradlew.bat test --tests MemberSearchTest

# Versiones y protocolo
Select-String -Path .\build.gradle -Pattern '0\.10\.1|1\.85\.1|3\.25\.9|runClient'
Select-String -Path .\src\main\proto\member.proto -Pattern 'rpc |message SearchMembersRequest'

# Comprobación del registro de prompts
Select-String -Path .\docs\prompts-stage-3.md -Pattern '^## Commit|Model:|Exact prompt:|Files changed:'

# Archivos y cambios
git log --oneline -8
git status
git diff --check
```

Prueba de ejecución manual, **dos terminales**:

```powershell
# Terminal A — dejar ejecutando
.\gradlew.bat run

# Terminal B — pruebas positivas
.\gradlew.bat runClient
.\gradlew.bat runClient --args="1 2"

# Caso negativo: se espera salida de código 1 y error Gradle
.\gradlew.bat runClient --args="9999"

# Puerto gRPC (requiere servidor activo)
Test-NetConnection -ComputerName 127.0.0.1 -Port 50051

# REST (requiere servidor activo)
Invoke-WebRequest http://127.0.0.1:7070 -UseBasicParsing
```

La prueba del puerto verifica conectividad TCP, no un RPC gRPC. Los RPC ya se comprueban mediante los tests y el cliente.

## 7. Nota y criterio de cierre

**No hay nota final suficientemente sustentada aún.** La escala de notas chilenas depende de la regla de conversión usada por el docente, y la revisión exhaustiva del registro de prompts y de las versiones del código no está finalizada.

Al terminar los controles, informar:
- puntos por criterio y total sobre 40;
- porcentaje = `(puntos / 40) × 100`;
- nota únicamente con la pauta de conversión aplicable.

**Commit de cierre propuesto:** `Stage 3: add final audit and verification report`.

Los archivos previstos son:
- `docs/stage-3-audit.md` (este informe, actualizado con resultados finales);
- `docs/prompts-stage-3.md` (registro literal del prompt de la auditoría).

No introducir cambios de código ni marcar verificaciones como completadas si no existe evidencia.
