# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Lightweight Android ORM library (`dal` module) providing SQLite abstraction through annotation-based entity mapping. Published to JitPack as `com.github.rochaeinar:data-access`. No external dependencies — pure Android/Java.

## Build Commands

```bash
# Build from the DataAccessLayer directory
cd DataAccessLayer
./gradlew build                    # Build all modules
./gradlew :dal:assembleRelease     # Build library AAR only
./gradlew connectedAndroidTest     # Run instrumented tests (requires device/emulator)
```

- Java 17 required (OpenJDK 17 for JitPack CI)
- Compile/Target SDK: 35, Min SDK: 19
- Lint is configured with `abortOnError false` on the dal module

## Module Structure

- **`DataAccessLayer/dal/`** — The library module (`com.erc.dal` package). This is the main deliverable.
- **`DataAccessLayer/app/`** — Demo app (`com.erc.dataaccesslayer` package) showing library usage patterns.

## Architecture

The library uses **reflection + annotations** at runtime to map Java objects to SQLite tables. No code generation or compile-time processing.

**Public API flow:** `DB` (facade) → `DBOperations` (synchronized CRUD coordinator) → `QueryBuilder` (SQL generation) + `SQLiteDatabaseManager` (extends `SQLiteOpenHelper`)

Key classes in `com.erc.dal`:

- **`DB`** — Public API entry point: `save()`, `getById()`, `getAll()`, `calculate()`, `remove()`, `execSQL()`, `rawQuery()`
- **`Entity`** — Abstract base class all persistent objects must extend
- **`Options`** — Fluent query builder for WHERE (AND/OR), ORDER BY, DISTINCT, LIMIT, OFFSET, IN
- **`Aggregation`** — Builder for `avg()`, `sum()`, `max()`, `min()`, `count()`
- **`DBConfig` / `upgrade/DBConfig`** — Database configuration (name, version, context, upgrade listener)

**Annotations:** `@Table(name)`, `@Field(name)`, `@PrimaryKey` — applied to Entity subclasses to define table/column mappings.

**Type mapping** (`HelperDataType`): String/char/Date → TEXT, int/long/boolean/short → INTEGER, double/float → REAL. Dates use ISO format `yyyy-MM-dd'T'HH:mm:ss`.

**Upgrade system** (`com.erc.dal.upgrade`): `UpgradeHelper` maintains a METADATA table for version tracking. Custom migrations go through the `UpgradeListener` interface.

## Conventions

- Database operations in `DBOperations` are `synchronized` for thread safety
- Log calls use the `Log` wrapper class (tag: `DATA_ACCESS`)
- SQL templates are centralized in `Constant.java`
- Reflection utilities are in `ReflectionHelper` — used for field introspection, instantiation, and annotation scanning
- Note: `Expresion` (not "Expression") is the intentional spelling used throughout the codebase
