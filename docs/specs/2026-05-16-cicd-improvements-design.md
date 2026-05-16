# CI/CD Improvements — Design Spec

**Date:** 2026-05-16
**Status:** Approved

## Objetivo

Añadir controles de calidad automáticos al pipeline CI/CD del proyecto HuertoApp antes de iniciar nuevos desarrollos. El objetivo es que cada PR a `develop` o `main` pase por verificaciones de calidad de código y mensajes de commit, y que semanalmente se generen reportes de cobertura y seguridad de dependencias.

## Contexto

Estado actual del CI/CD:
- `ci.yml`: tests Maven (backend) + build Vite (frontend) en PRs a `develop` y `main`
- `release.yml`: auto-tag semver al mergear a `main`
- Sin ESLint, sin validación de commits, sin cobertura, sin escaneo de seguridad, sin Dependabot

Restricciones:
- El backend **no tiene tests** aún — JaCoCo se configura sin umbral mínimo
- Los checks lentos (OWASP) no deben bloquear el feedback rápido de PRs

## Arquitectura: tres workflows + dependabot

```
ci.yml          → rápido, cada PR a develop/main      (bloquea merge)
quality.yml     → lento, cada lunes 08:00             (informativo)
dependabot.yml  → PRs automáticos semanales a develop
```

---

## Sección 1 — ci.yml ampliado

### Nuevos jobs añadidos al workflow existente

#### Job: `frontend-lint`

**Setup frontend (archivos nuevos/modificados):**

- `frontend/package.json` — añadir devDependencies y script:
  ```json
  "scripts": {
    "lint": "eslint src/"
  },
  "devDependencies": {
    "eslint": "^9.x",
    "@eslint/js": "^9.x",
    "eslint-plugin-vue": "^9.x"
  }
  ```

- `frontend/eslint.config.js` — configuración ESLint para Vue 3:
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
  > La regla `multi-word-component-names` se desactiva porque las vistas (`HomeView`, `LoginView`) ya siguen esa convención pero algunos componentes de una sola palabra son válidos en este proyecto.

- Job en `ci.yml`:
  ```yaml
  frontend-lint:
    name: Frontend — ESLint
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json
      - run: npm ci
        working-directory: frontend
      - run: npm run lint
        working-directory: frontend
  ```

#### Job: `commitlint`

Valida que todos los commits del PR sigan el formato Conventional Commits (`feat:`, `fix:`, `chore:`, `docs:`, `ci:`, `refactor:`, `test:`).

- Usa `wagoid/commitlint-github-action@v6`
- Configuración en `commitlint.config.js` en la raíz:
  ```js
  export default {
    extends: ['@commitlint/config-conventional']
  }
  ```
- Job en `ci.yml`:
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

---

## Sección 2 — quality.yml (nuevo, semanal)

Workflow separado que corre cada lunes a las 08:00 UTC. No bloquea PRs — genera artefactos descargables en GitHub Actions.

```yaml
on:
  schedule:
    - cron: '0 8 * * 1'
  workflow_dispatch:  # permite lanzarlo manualmente
```

#### Job: `coverage` (JaCoCo)

- Añadir `jacoco-maven-plugin` al `backend/pom.xml`:
  ```xml
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
  ```
  > Sin `<rules>` de umbral mínimo. Se añadirá cuando el proyecto tenga tests implementados.

- Job en `quality.yml`:
  ```yaml
  coverage:
    name: Backend — JaCoCo coverage
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - run: mvn verify -B
        working-directory: backend
      - uses: actions/upload-artifact@v4
        with:
          name: jacoco-report
          path: backend/target/site/jacoco/
  ```

#### Job: `owasp`

- Añadir `dependency-check-maven` al `backend/pom.xml`:
  ```xml
  <plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
      <failBuildOnCVSS>11</failBuildOnCVSS>
    </configuration>
  </plugin>
  ```
  > `failBuildOnCVSS: 11` significa que nunca falla (escala CVSS va de 0 a 10). Solo informa.

- Job en `quality.yml`:
  ```yaml
  owasp:
    name: Backend — OWASP dependency check
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - run: mvn dependency-check:check -B
        working-directory: backend
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: owasp-report
          path: backend/target/dependency-check-report.html
  ```

---

## Sección 3 — .github/dependabot.yml (nuevo)

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

Los PRs de Dependabot van a `develop` para pasar por el CI antes de llegar a `main`.

---

## Archivos a crear / modificar

| Archivo | Acción |
|---|---|
| `.github/workflows/ci.yml` | Modificar: añadir jobs `frontend-lint` y `commitlint` |
| `.github/workflows/quality.yml` | Crear: jobs `coverage` y `owasp` con schedule semanal |
| `.github/dependabot.yml` | Crear: Maven + npm + Actions, semanal a develop |
| `frontend/eslint.config.js` | Crear: config ESLint Vue 3 flat config |
| `frontend/package.json` | Modificar: añadir devDependencies ESLint y script `lint` |
| `commitlint.config.js` | Crear: extends @commitlint/config-conventional |
| `backend/pom.xml` | Modificar: añadir plugins JaCoCo y OWASP dependency-check |

## Verificación

1. Abrir PR de prueba → verificar que aparecen 4 jobs en CI: `Backend — Maven tests`, `Frontend — Vite build`, `Frontend — ESLint`, `Commitlint`
2. Hacer un commit con mensaje incorrecto (ej. `arreglo bug`) → verificar que Commitlint falla
3. Ir a Actions → ejecutar `quality.yml` manualmente (`workflow_dispatch`) → descargar artefactos `jacoco-report` y `owasp-report`
4. Verificar que Dependabot abre PRs automáticos la semana siguiente
