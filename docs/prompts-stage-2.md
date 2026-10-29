# AI Prompt Log - Stage 2

| Number | Model | Exact prompt text | Purpose | Files changed |
|---|---|---|---|---|
| 1 | GPT-6 | Comencemos con el Commit 7 del Stage 2. Explícame el problema, sus consecuencias y evaluemos propuestas con todos los archivos completos, indicando las pruebas y el prompt log. espera que te envie el zip | Refactorizar la gestión de préstamos trasladando checkout() desde MemberService hacia LoanService, incorporando Clock, validación de entidades y transacciones, actualizando las dependencias y comprobando el funcionamiento mediante pruebas automatizadas. | MemberService.java, LoanService.java, LoanController.java, App.java, DueDateDuplicationTest.java, TransactionBugTest.java |
| 2 | GPT-6 | realizemos el commit 8, de la misma forma que el commit 7 | Completar LoanService con el cálculo de multas acumuladas, manejo de préstamos inexistentes, validación de devoluciones repetidas y actualización transaccional del inventario. | LoanService.java |