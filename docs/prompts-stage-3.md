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
