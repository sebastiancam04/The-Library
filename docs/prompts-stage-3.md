# AI Prompt Log — Stage 3

## Commit 13 — gRPC Build Configuration and Protocol Definition

**Model:** GPT-6

**Prompt 1:**

Comencemos con el Commit 13 del Stage 3 de The-Library. Basándonos en el documento refactor.stage-3.pdf y en la implementación actual del proyecto, analiza los problemas identificados, sus consecuencias y plantea una solución para la configuración de Gradle y la definición del contrato member.proto.

Presenta la implementación completa de los archivos involucrados, explica las decisiones técnicas y establece las pruebas necesarias para verificar el cumplimiento de los requisitos.

**Files changed:**
- `build.gradle`
- `src/main/proto/member.proto`
- `docs/prompts-stage-3.md`

**Implementation:**

Se incorporó el plugin de Protocol Buffers y las dependencias gRPC, manteniendo la compatibilidad con los componentes existentes. Se definió el contrato `MemberService` con las operaciones `RegisterMember`, `ListMembers` y `GetMember`.

**Validation:**
- Gradle 9.7.1 y Java 21.
- `tasks --group application`: BUILD SUCCESSFUL.
- `generateProto`: BUILD SUCCESSFUL.



## Commit 14 - Member Service and gRPC Adapter

**Model:** GPT-6

**Exact prompt:**

Comencemos con el Commit 14 del Stage 3 de The-Library. Basándonos en el documento refactor.stage-3.pdf y en la implementación actual del proyecto, analiza los problemas arquitectónicos identificados, sus consecuencias y plantea una solución para incorporar el método findById en MemberService y desarrollar el adaptador gRPC MemberGrpcService.

Presenta la implementación completa de los archivos involucrados, respetando la arquitectura existente, las responsabilidades de cada capa y el manejo de excepciones mediante códigos de estado gRPC.

Explica las decisiones técnicas, los conceptos fundamentales de gRPC utilizados y establece las pruebas necesarias para verificar el correcto funcionamiento de los cambios antes de realizar el commit.

**Files changed:**
- src/main/java/cl/ucn/disc/arqsist/library/service/MemberService.java
- src/main/java/cl/ucn/disc/arqsist/library/grpc/MemberGrpcService.java
- docs/prompts-stage-3.md

**Objective:**
Add member retrieval by ID and implement the gRPC server adapter,
including protobuf conversion and domain exception mapping.


## Commit 15 - gRPC Server Integration

**Model:** GPT-6

**Exact prompt:**

Comencemos con el Commit 15 del Stage 3 de The-Library. Basándonos en el documento refactor.stage-3.pdf y en la arquitectura implementada en los commits anteriores, analiza los problemas y las consecuencias de no contar con un servidor gRPC integrado en la aplicación.

Desarrolla una solución para incorporar el servidor gRPC en App.java, utilizando MemberGrpcService y manteniendo el funcionamiento de la API REST existente.

Presenta la implementación completa de los archivos involucrados, explica las decisiones arquitectónicas, el manejo del ciclo de vida del servidor y los mecanismos de cierre seguro. Incluye las verificaciones necesarias para comprobar la compilación, el funcionamiento simultáneo de REST y gRPC y la correcta integración antes de registrar el commit.

**Files changed:**
- src/main/java/cl/ucn/disc/arqsist/library/App.java
- docs/prompts-stage-3.md

**Objective:**
Integrate the member gRPC server into the main application,
preserve the existing REST API, and implement graceful
server shutdown.


## Commit 16 - gRPC Client and Command Line Application

**Model:** GPT-6

**Exact prompt:**

Comencemos con el Commit 16 del Stage 3 de The-Library. Basándonos en el documento refactor.stage-3.pdf y en la implementación actual del proyecto, analiza los problemas arquitectónicos identificados, sus consecuencias y plantea una solución para desarrollar el cliente gRPC MemberClient y la aplicación de consola MemberClientApp.

Presenta la implementación completa de los archivos involucrados, respetando la arquitectura existente y el contrato definido en member.proto. Considera la administración de conexiones mediante ManagedChannel, el uso de stubs, la configuración de deadlines y la conversión de respuestas y códigos de estado gRPC a los tipos del dominio.

Explica las decisiones técnicas, las responsabilidades de cada componente y los conceptos fundamentales utilizados. Establece las pruebas necesarias para verificar la comunicación entre cliente y servidor, el manejo de errores y el correcto funcionamiento de los cambios antes de realizar el commit.

**Files changed:**

- src/main/java/cl/ucn/disc/arqsist/library/grpc/MemberClient.java
- src/main/java/cl/ucn/disc/arqsist/library/MemberClientApp.java
- docs/prompts-stage-3.md

**Objective:**

Implement the gRPC MemberClient and its command line application,
including channel management, deadlines, protobuf conversions,
status mapping, and connection lifecycle.


## Commit 17 - gRPC Round Trip Integration Test

**Model:** GPT-6

**Exact prompt:**

Comencemos con el Commit 17 del Stage 3 de The-Library. Basándonos en el documento refactor.stage-3.pdf y en la implementación actual del proyecto, analiza los problemas arquitectónicos relacionados con la ausencia de pruebas de integración entre el cliente y el servidor gRPC, sus consecuencias y plantea una solución para implementar MemberGrpcTest.

Presenta la implementación completa de los archivos involucrados, respetando la arquitectura existente y utilizando una base de datos SQLite en memoria junto con el transporte gRPC in-process.

Considera la validación de las operaciones de registro, listado y consulta de miembros por identificador, incluyendo el manejo de miembros inexistentes mediante NotFoundException.

Explica las decisiones técnicas, el funcionamiento de InProcessServerBuilder e InProcessChannelBuilder, la administración de recursos y las diferencias entre pruebas unitarias y pruebas de integración.

Establece las verificaciones necesarias para comprobar el correcto funcionamiento de la implementación y la compatibilidad con las pruebas existentes antes de realizar el commit.

**Files changed:**

- src/test/java/cl/ucn/disc/arqsist/library/MemberGrpcTest.java
- docs/prompts-stage-3.md

**Objective:**

Implement an automated gRPC integration test using
SQLite in-memory and in-process transport.

Validate member registration, listing, retrieval by ID,
and NOT_FOUND error mapping.


## Commit 18 - Member Search RPC

**Model:** GPT-6

**Exact prompt:**

Comencemos con el Commit 18 del Stage 3 de The-Library. Basándonos en el documento refactor.stage-3.pdf y en la arquitectura desarrollada en los commits anteriores, analiza los problemas arquitectónicos relacionados con la ausencia de búsqueda de miembros por nombre, sus consecuencias y plantea una solución para implementar el RPC SearchMembers.

Desarrolla una solución completa que integre las modificaciones necesarias en member.proto, MemberDao, MemberService, MemberGrpcService y MemberClient, respetando la separación de responsabilidades entre las capas de persistencia, negocio y comunicación gRPC.

La búsqueda debe cumplir los requisitos establecidos en el documento: coincidencia parcial sin distinguir mayúsculas y minúsculas ASCII, eliminación de espacios iniciales y finales, tratamiento literal de los caracteres especiales, ordenamiento ascendente por identificador, respuesta vacía cuando no existan coincidencias y manejo de consultas inválidas mediante los códigos de estado correspondientes.

Presenta la implementación completa de los archivos involucrados y desarrolla MemberSearchTest utilizando SQLite en memoria y transporte gRPC in-process. Explica las decisiones técnicas, los aspectos de seguridad de la consulta SQL y los siete casos de aceptación definidos por el profesor.

Establece las verificaciones necesarias para comprobar la funcionalidad, la compatibilidad con las pruebas anteriores y el cumplimiento de los requisitos de evaluación antes de realizar el commit.

**Files changed:**

- src/main/proto/member.proto
- src/main/java/cl/ucn/disc/arqsist/library/dao/MemberDao.java
- src/main/java/cl/ucn/disc/arqsist/library/service/MemberService.java
- src/main/java/cl/ucn/disc/arqsist/library/grpc/MemberGrpcService.java
- src/main/java/cl/ucn/disc/arqsist/library/grpc/MemberClient.java
- src/test/java/cl/ucn/disc/arqsist/library/MemberSearchTest.java
- docs/prompts-stage-3.md

**Objective:**

Implement a member search RPC across the persistence,
service, gRPC adapter and client layers.

Support literal substring matching, ASCII case-insensitive
search, query normalization, ID ordering and status mapping.

Validate the seven acceptance cases using an in-memory
database and in-process gRPC transport.
