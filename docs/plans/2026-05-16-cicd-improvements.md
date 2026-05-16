# CI/CD Improvements — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Añadir ESLint, commitlint, JaCoCo, OWASP dependency-check y Dependabot al pipeline CI/CD de HuertoApp.

**Architecture:** Tres cambios ortogonales: (1) ci.yml amplía dos jobs nuevos que bloquean PRs; (2) quality.yml nuevo con schedule semanal para reportes lentos; (3) dependabot.yml nuevo para PRs automáticas de dependencias. Los plugins Maven se añaden a pom.xml para que los jobs de calidad puedan invocarlos.

**Tech Stack:** GitHub Actions, ESLint 9 flat config + eslint-plugin-vue@9, wagoid/commitlint-github-action@v6 + @commitlint/config-conventional, jacoco-maven-plugin 0.8.11, dependency-check-maven 9.0.9, Dependabot.

---

## Archivos a crear / modificar

| Archivo | Acción |
|---|---|
| `frontend/package.json` | Modificar: añadir devDependencies ESLint y script `lint` |
| `frontend/eslint.config.js` | Crear: configuración ESLint Vue 3 flat config |
| `commitlint.config.cjs` | Crear: extends @commitlint/config-conventional (CJS para root sin package.json) |
| `.github/workflows/ci.yml` | Modificar: añadir jobs `frontend-lint` y `commitlint` |
| `backend/pom.xml` | Modificar: añadir plugins JaCoCo y OWASP dependency-check |
| `.github/workflows/quality.yml` | Crear: jobs `coverage` (JaCoCo) y `owasp` con schedule semanal |
| `.github/dependabot.yml` | Crear: Maven + npm + Actions, semanal a develop |

---

## Task 1: ESLint setup en frontend

**Files:**
- Modify: `frontend/package.json`
- Create: `frontend/eslint.config.js`

- [ ] **Step 1: Añadir ESLint a package.json**

Editar `frontend/package.json` para quedar exactamente así:

```json
{
  "name": "huertoapp-frontend",
  "version": "0.0.1",
  "private": true,
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint src/"
  },
  "dependencies": {
    "axios": "^1.7.2",
    "bootstrap": "^5.3.3",
    "bootstrap-icons": "^1.11.3",
    "pinia": "^2.1.7",
    "vue": "^3.4.27",
    "vue-router": "^4.3.2"
  },
  "devDependencies": {
    "@eslint/js": "^9.0.0",
    "@vitejs/plugin-vue": "^6.0.0",
    "eslint": "^9.0.0",
    "eslint-plugin-vue": "^9.0.0",
    "vite": "^8.0.10"
  }
}
```

- [ ] **Step 2: Instalar las nuevas dependencias**

```bash
cd frontend
npm install
```

Resultado esperado: `package-lock.json` actualizado con `eslint`, `@eslint/js`, `eslint-plugin-vue`.

- [ ] **Step 3: Crear eslint.config.js**

Crear `frontend/eslint.config.js` con este contenido exacto:

```js
import js from '@eslint/js'
import pluginVue from 'eslint-plugin-vue'

export default [
  js.configs.recommended,
  ...pluginVue.configs['flat/recommended'],
  {
    rules: {
      'vue/multi-word-component-names': 'off'
    }
  }
]
```

> La regla `multi-word-component-names` se desactiva porque vistas como `HomeView` o `LoginView` son multi-palabra (ok) pero algunos componentes internos son de una sola palabra (válido en este proyecto).

- [ ] **Step 4: Verificar que ESLint corre sin errores**

```bash
cd frontend
npm run lint
```

Resultado esperado: sin errores (puede haber warnings, eso no falla el build). Si hay errores de ESLint en el código Vue existente, corregirlos antes de continuar.

> Si ESLint reporta errores de tipo `no-unused-vars` o `no-undef` en archivos Vue, revisar cada uno. Los más comunes son variables de Pinia stores o composables que ESLint no reconoce como usadas — pueden requerir un comentario `/* eslint-disable no-unused-vars */` puntual o ajuste de reglas.

- [ ] **Step 5: Commit**

```bash
git add frontend/package.json frontend/package-lock.json frontend/eslint.config.js
git commit -m "ci: add ESLint config for Vue 3 frontend"
```

---

## Task 2: Añadir job `frontend-lint` a ci.yml

**Files:**
- Modify: `.github/workflows/ci.yml`

- [ ] **Step 1: Añadir el job frontend-lint**

El archivo `.github/workflows/ci.yml` actual tiene dos jobs: `backend` y `frontend`. Añadir un tercer job `frontend-lint` al final del archivo:

```yaml
  frontend-lint:
    name: Frontend — ESLint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: npm ci
        working-directory: frontend

      - name: Lint
        run: npm run lint
        working-directory: frontend
```

El archivo completo resultante debe tener la estructura:

```yaml
name: CI

on:
  pull_request:
    branches:
      - main
      - develop

jobs:
  backend:
    name: Backend — Maven tests
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Run tests
        run: mvn test -B
        working-directory: backend

  frontend:
    name: Frontend — Vite build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: npm ci
        working-directory: frontend

      - name: Build
        run: npm run build
        working-directory: frontend

  frontend-lint:
    name: Frontend — ESLint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Node 20
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: npm ci
        working-directory: frontend

      - name: Lint
        run: npm run lint
        working-directory: frontend
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: add frontend ESLint job to CI workflow"
```

---

## Task 3: Commitlint — config y job en ci.yml

**Files:**
- Create: `commitlint.config.cjs`
- Modify: `.github/workflows/ci.yml`

> Se usa extensión `.cjs` (CommonJS) porque el archivo vive en la raíz del repo, donde no hay `package.json` con `"type": "module"`. La acción `wagoid/commitlint-github-action@v6` lo detecta automáticamente.

- [ ] **Step 1: Crear commitlint.config.cjs**

Crear `commitlint.config.cjs` en la raíz del repositorio (junto a `README.md`):

```js
module.exports = {
  extends: ['@commitlint/config-conventional']
}
```

- [ ] **Step 2: Añadir job commitlint a ci.yml**

Añadir al final de `.github/workflows/ci.yml` (después del job `frontend-lint`):

```yaml
  commitlint:
    name: Commitlint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - uses: wagoid/commitlint-github-action@v6
```

- [ ] **Step 3: Verificar YAML válido**

Revisar que la indentación sea correcta: el job `commitlint` tiene el mismo nivel de indentación (2 espacios) que `backend`, `frontend`, `frontend-lint`.

- [ ] **Step 4: Commit**

```bash
git add commitlint.config.cjs .github/workflows/ci.yml
git commit -m "ci: add commitlint job to CI workflow"
```

---

## Task 4: Añadir plugins Maven JaCoCo y OWASP a pom.xml

**Files:**
- Modify: `backend/pom.xml`

- [ ] **Step 1: Añadir los plugins en la sección `<build><plugins>`**

En `backend/pom.xml`, dentro del bloque `<build><plugins>...</plugins></build>`, añadir los dos plugins nuevos después del plugin existente `spring-boot-maven-plugin`. El bloque `<build>` completo queda así:

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.11</version>
                <executions>
                    <execution>
                        <goals><goal>prepare-agent</goal></goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>verify</phase>
                        <goals><goal>report</goal></goals>
                    </execution>
                </executions>
            </plugin>

            <plugin>
                <groupId>org.owasp</groupId>
                <artifactId>dependency-check-maven</artifactId>
                <version>9.0.9</version>
                <configuration>
                    <failBuildOnCVSS>11</failBuildOnCVSS>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

> `failBuildOnCVSS: 11` significa que nunca falla el build (escala CVSS va de 0 a 10). El plugin solo genera el informe.

- [ ] **Step 2: Verificar que los tests siguen pasando**

```bash
cd backend
mvn test -B
```

Resultado esperado: `BUILD SUCCESS`. Los plugins JaCoCo y OWASP no afectan a `mvn test` (JaCoCo se activa en `mvn verify`, OWASP con `mvn dependency-check:check`).

- [ ] **Step 3: Verificar que JaCoCo genera el informe**

```bash
cd backend
mvn verify -B
```

Resultado esperado: `BUILD SUCCESS`. Debe existir el directorio `backend/target/site/jacoco/` con `index.html`.

- [ ] **Step 4: Commit**

```bash
git add backend/pom.xml
git commit -m "ci: add JaCoCo and OWASP dependency-check Maven plugins"
```

---

## Task 5: Crear quality.yml

**Files:**
- Create: `.github/workflows/quality.yml`

- [ ] **Step 1: Crear el archivo**

Crear `.github/workflows/quality.yml` con este contenido exacto:

```yaml
name: Quality

on:
  schedule:
    - cron: '0 8 * * 1'
  workflow_dispatch:

jobs:
  coverage:
    name: Backend — JaCoCo coverage
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Run verify (generates JaCoCo report)
        run: mvn verify -B
        working-directory: backend

      - name: Upload JaCoCo report
        uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: backend/target/site/jacoco/

  owasp:
    name: Backend — OWASP dependency check
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Run OWASP dependency check
        run: mvn dependency-check:check -B
        working-directory: backend

      - name: Upload OWASP report
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: owasp-report
          path: backend/target/dependency-check-report.html
```

> `if: always()` en el upload del informe OWASP garantiza que el artefacto se suba aunque el step anterior falle. Esto es importante para poder revisar qué vulnerabilidades se encontraron incluso si el build fallara (aunque con `failBuildOnCVSS: 11` esto no ocurrirá).

> El cron `0 8 * * 1` ejecuta cada lunes a las 08:00 UTC. `workflow_dispatch` permite lanzarlo manualmente desde GitHub Actions → Quality → Run workflow.

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/quality.yml
git commit -m "ci: add quality workflow with JaCoCo and OWASP (weekly)"
```

---

## Task 6: Crear dependabot.yml

**Files:**
- Create: `.github/dependabot.yml`

- [ ] **Step 1: Crear el archivo**

Crear `.github/dependabot.yml` con este contenido exacto:

```yaml
version: 2
updates:
  - package-ecosystem: maven
    directory: /backend
    schedule:
      interval: weekly
      day: monday
    target-branch: develop
    open-pull-requests-limit: 5

  - package-ecosystem: npm
    directory: /frontend
    schedule:
      interval: weekly
      day: monday
    target-branch: develop
    open-pull-requests-limit: 5

  - package-ecosystem: github-actions
    directory: /
    schedule:
      interval: weekly
      day: monday
    target-branch: develop
    open-pull-requests-limit: 5
```

> Los PRs de Dependabot van a `develop` (no a `main`) para que pasen por el CI antes de llegar a producción.

- [ ] **Step 2: Commit**

```bash
git add .github/dependabot.yml
git commit -m "ci: add Dependabot for Maven, npm and GitHub Actions (weekly to develop)"
```

---

## Task 7: Abrir PR, verificar CI y mergear

Esta tarea no genera código — verifica que todo lo implementado funciona en GitHub.

- [ ] **Step 1: Push de la rama**

```bash
git push -u origin chore/cicd-improvements-spec
```

- [ ] **Step 2: Abrir PR en GitHub**

Ir a: `https://github.com/isasan/app-huerto-urbano/compare/develop...chore/cicd-improvements-spec`

Título de PR sugerido: `ci: add ESLint, commitlint, JaCoCo, OWASP and Dependabot`

Descripción sugerida:
```
## Mejoras CI/CD

- ESLint Vue 3 (flat config) en frontend — nuevo job `Frontend — ESLint` en CI
- Commitlint (Conventional Commits) — nuevo job `Commitlint` en CI
- JaCoCo (cobertura backend) — plugin Maven + job en `quality.yml` semanal
- OWASP dependency-check — plugin Maven + job en `quality.yml` semanal
- Dependabot — PRs automáticas semanales de Maven, npm y GitHub Actions a `develop`
```

- [ ] **Step 3: Verificar que CI corre con 4 jobs**

En el PR abierto, esperar a que GitHub Actions lance el CI. Verificar que aparecen los 4 jobs:
- `Backend — Maven tests` ✅
- `Frontend — Vite build` ✅
- `Frontend — ESLint` ✅
- `Commitlint` ✅

Si algún job falla, investigar el log y corregir antes de continuar.

- [ ] **Step 4: Verificar que quality.yml aparece en Actions**

Ir a `https://github.com/isasan/app-huerto-urbano/actions` — debe aparecer el workflow **Quality** en la lista. Ejecutarlo manualmente con el botón **Run workflow** (sobre la rama `chore/cicd-improvements-spec`). Verificar que los dos jobs terminan y generan artefactos descargables (`jacoco-report`, `owasp-report`).

> El job OWASP tarda varios minutos la primera vez porque descarga la base de datos NVD de vulnerabilidades.

- [ ] **Step 5: Mergear PR chore/cicd-improvements-spec → develop**

Desde GitHub UI, mergear el PR.

- [ ] **Step 6: Abrir PR develop → main**

Ir a: `https://github.com/isasan/app-huerto-urbano/compare/main...develop`

Crear el PR, verificar que CI pasa, mergear.

- [ ] **Step 7: Verificar tag nuevo**

En `https://github.com/isasan/app-huerto-urbano/tags` debe aparecer un nuevo tag (el release workflow lo crea automáticamente al mergear a main).

- [ ] **Step 8: Limpiar rama**

```bash
git checkout develop
git pull
git branch -d chore/cicd-improvements-spec
git push origin --delete chore/cicd-improvements-spec
```

---

## Verificación final

Tras completar todas las tareas:

1. **CI con 4 jobs** — cualquier PR a `develop` o `main` ejecuta: `Backend — Maven tests`, `Frontend — Vite build`, `Frontend — ESLint`, `Commitlint`
2. **Commitlint funciona** — abrir un PR con un commit de mensaje incorrecto (ej. `arreglo bug` sin prefijo) y verificar que el job `Commitlint` falla
3. **Quality manual** — desde Actions → Quality → Run workflow, descargar y abrir `jacoco-report/index.html` y `owasp-report`
4. **Dependabot activo** — en la semana siguiente, Dependabot abrirá PRs automáticos en `develop`
