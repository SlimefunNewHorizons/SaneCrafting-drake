<div align="center">

  <img src="https://raw.githubusercontent.com/SlimefunNewHorizons/SaneCrafting-drake/main/banner.svg" alt="SaneCrafting-drake Banner" width="920" />

# 🧪 SaneCrafting-Drake

**Addon de Slimefun para recetas limpias y mesas de crafteo con soporte nativo para Purpur 26.2 y Java 25 (Temporada 2)**

<p>
  <a href="https://github.com/SlimefunNewHorizons/SaneCrafting-drake"><img src="https://img.shields.io/badge/GitHub-SaneCrafting--Drake-181717?style=for-the-badge&logo=github" alt="GitHub"/></a>
  <img src="https://img.shields.io/badge/Purpur-26.2-8B5CF6?style=for-the-badge&logo=minecraft&logoColor=white" alt="Purpur 26.2"/>
  <img src="https://img.shields.io/badge/Java-25_LTS-F89820?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25"/>
  <img src="https://img.shields.io/badge/Rust-FFM_Bridge-FF4500?style=for-the-badge&logo=rust&logoColor=white" alt="Rust Native"/>
  <img src="https://img.shields.io/badge/License-MIT-22C55E?style=for-the-badge" alt="MIT License"/>
</p>

</div>

> ### 🏰 Comunidad Oficial de DrakesCraft
>
> * 🎮 **IP del Servidor**: `mc.drakescraft.cl` *(Java & Bedrock)*
> * 💬 **Discord Oficial**: [discord.gg/rv3vtXZTk7](https://discord.gg/rv3vtXZTk7)
> * 🌐 **Portal Web & Guía**: [web.drakescraft.cl](https://web.drakescraft.cl)
> * 🛒 **Tienda Oficial**: [drakescraft-v2.tebex.io](https://drakescraft-v2.tebex.io)

---

## Qué hace

`SaneCrafting-Drake` es el fork mantenido por **DrakesCraft Labs** del addon **SaneCrafting** (original de Idra / MetaMechanists) para Slimefun. Incorpora mejoras de crafteo, bloques y recetas accesibles directamente desde la guía estándar (`/sf guide`), portado a la línea **Temporada 2 (`port-26x`)** sobre **Purpur 26.2** y **Java 25**.

> **Nota de ecosistema (`StarSuites`):** Este addon también existe como módulo integrado (`sanecrafting`) dentro de `drakes-tech` en el monorepo [Drakes-Suites (`26.x`)](https://github.com/SlimefunNewHorizons/Drakes-Suites). Usa este repositorio si necesitas la compilación standalone o contribuir al porteo individual.

## Características del Porteo Drake

- **Compatibilidad 26.x / Java 25:** Compilado con `--release 25` contra Paper/Purpur `26.2` y `Slimefun 11.0-Universal-26.x-SNAPSHOT`.
- **Modelo Híbrido Cero-Riesgo:** Compatible con la aceleración off-heap vía Java FFM API (`Slimefun-Rust`) con fallback seguro en Java.
- **Preservación de Datos (0-Reset):** Mantiene intactos los bloques e inventarios persistidos en SQLite (`stored-blocks.db`).

## Compatibilidad

| Componente | Requisito |
| :--- | :--- |
| **Servidor** | Paper / Purpur **26.2** (rama `port-26x`) |
| **Java** | **Java 25** (LTS) |
| **Dependencia** | [Slimefun4-Drake](https://github.com/SlimefunNewHorizons/Slimefun4-Drake) (`11.0-Universal-26.x-SNAPSHOT`) |
| **Lado** | Solo servidor (el cliente no requiere mods) |
| **Versión** | `1.0` (`plugin.yml`: `0.1`) |

## Compilación e Instalación

Requisitos: **JDK 25** y **Maven 3.9+**.

```bash
mvn clean package
```

1. Coloca el `.jar` generado en `target/` dentro de la carpeta `plugins/` junto a `Slimefun4-Drake`.
2. Reinicia el servidor. Todo el contenido se registra automáticamente en `/sf guide`.

## Créditos y Licencia

- **Autor original:** Idra / MetaMechanists ([`docs/UPSTREAM_ATTRIBUTION.md`](https://raw.githubusercontent.com/SlimefunNewHorizons/SaneCrafting-drake/main/docs/UPSTREAM_ATTRIBUTION.md)).
- **Porteo y mantenimiento:** [**JackStar6677-1**](https://github.com/JackStar6677-1) · [**DrakesCraft Labs**](https://github.com/SlimefunNewHorizons).
- **Licencia:** Distribuido bajo la licencia original **MIT**. Consulta el archivo [`LICENSE`](LICENSE) para los términos completos.
