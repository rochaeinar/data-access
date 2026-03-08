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
- **`DALException`** — RuntimeException thrown by all DAL operations on failure (never swallowed)

**Annotations:** `@Table(name)`, `@Field(name)`, `@PrimaryKey` — applied to Entity subclasses to define table/column mappings.

**Type mapping** (`HelperDataType`): String/char/Date → TEXT, int/long/boolean/short → INTEGER, double/float → REAL. Dates use ISO format `yyyy-MM-dd'T'HH:mm:ss`.

**Upgrade system** (`com.erc.dal.upgrade`): `UpgradeHelper` maintains a METADATA table for version tracking. Custom migrations go through the `UpgradeListener` interface.

## Conventions

- Database operations in `DBOperations` are `synchronized` for thread safety
- Log calls use the `Log` wrapper class (tag: `DATA_ACCESS`)
- SQL templates are centralized in `Constant.java`
- Reflection utilities are in `ReflectionHelper` — used for field introspection, instantiation, and annotation scanning
- Note: `Expresion` (not "Expression") is the intentional spelling used throughout the codebase

## Error Handling

All DAL operations throw `DALException` (unchecked `RuntimeException`) — never swallowed internally.
- `SQLiteDatabaseManager.open()` / `openReadOnly()` → wraps `SQLiteException` in `DALException`
- `DBOperations.execSQL()` / `rawQuery()` → throw `DALException` on failure
- `QueryBuilder` methods → throw `DALException` on missing annotations or reflection errors
- Callers must wrap in `try { } catch (DALException e)` when recovery is needed

---

## Full DAL API Reference

### Entity definition

```java
@Table(name = "OptionalName")      // omit name → uses class simple name
public class MyEntity extends Entity {

    @PrimaryKey
    @Field(name = "ID")            // omit name → uses field name
    public long id;

    @Field public String text;
    @Field public int number;
    @Field public long bigNumber;
    @Field public short smallNumber;
    @Field public boolean flag;
    @Field public double decimal;
    @Field public float smallDecimal;
    @Field public char character;
    @Field public Date date;       // stored as ISO TEXT yyyy-MM-dd'T'HH:mm:ss

    public MyEntity() { super(); }
}
```

### DB — initialization

```java
// Internal DB (name derived from package)
DB db = new DB(context);

// Custom config
DBConfig config = new DBConfig(context, "name.db", version, urlOrNull);
config.setOnUpgradeListener(listener);
config.setPackageFilter("com.myapp.model"); // limits entity class scan
DB db = new DB(config);

db.initialize(); // re-open and clear cache (call after config changes)
```

### DB — CRUD

```java
// save() = INSERT when id==0 or not found, UPDATE when id exists
T result = db.save(entity);

// save() with Options as WHERE clause (UPDATE only, no auto-ID)
T result = db.save(entity, options);

// getById
MyEntity e = db.getById(MyEntity.class, id);

// getAll (no filter)
ArrayList<MyEntity> all = db.getAll(MyEntity.class);

// getAll with Options
ArrayList<MyEntity> filtered = db.getAll(MyEntity.class, options);

// remove by id
db.remove(MyEntity.class, id);

// calculate
Long   count = db.calculate(MyEntity.class, Aggregation.count());
Long   max   = db.calculate(MyEntity.class, Aggregation.max("ID"));
Long   min   = db.calculate(MyEntity.class, Aggregation.min("ID"));
Long   sum   = db.calculate(MyEntity.class, Aggregation.sum("field"));
Float  avg   = db.calculate(MyEntity.class, Aggregation.avg("field"));
// with filter:
Long   count = db.calculate(MyEntity.class, Aggregation.count(), options);

// raw SQL
db.execSQL("ALTER TABLE X ADD COLUMN y TEXT");
Cursor c = db.rawQuery("SELECT * FROM X WHERE id = 1");
```

### Options — all methods

```java
Options options = new Options();

// AND field op value  (EQUALS is default when op omitted)
options.and("field", value);
options.and("field", value, ExpresionOperator.LIKE);

// OR field op value
options.or("field", value);
options.or("field", value, ExpresionOperator.GREATER_THAN);

// field can also be a Function (see Function section)
options.and(function, value, ExpresionOperator.LIKE);
options.or(function, value, ExpresionOperator.EQUALS);

// IN  — LogicalOperator defaults to AND
options.in("field", listOfValues);
options.in("field", listOfValues, LogicalOperator.OR);

// GROUP (parenthesized sub-expression)
options.and(group);   // AND ( ... )
options.or(group);    // OR  ( ... )

// Modifiers
options.orderBy("field", true);   // true=ASC, false=DESC
options.distinct(true);
options.limit(100);
options.offset(50);               // requires limit() to be set
```

### ExpresionOperator — all values

| Value | SQL operator |
|---|---|
| `EQUALS` | `=` |
| `NOT_EQUAL_TO` | `<>` |
| `NOT_EQUAL_TO_DIFF` | `!=` |
| `GREATER_THAN` | `>` |
| `LESS_THAN` | `<` |
| `GREATER_THAN_OR_EQUAL_TO` | `>=` |
| `LESS_THAN_OR_EQUAL_TO` | `<=` |
| `NOT_GREATER_THAN` | `!>` |
| `NOT_LESS_THAN` | `!<` |
| `LIKE` | `LIKE` |
| `IN` | `IN` *(used internally by `options.in()`)* |

### LogicalOperator

```java
LogicalOperator.AND   // " AND "
LogicalOperator.OR    // " OR "
```

Used in `options.in(field, list, LogicalOperator.OR)` and `group.in(field, list, LogicalOperator.OR)`.

### Group — parenthesized sub-expressions

```java
Group group = new Group(options);   // parent options required for table/entity context

// Same API as Options (and/or/in), but no orderBy/limit/offset/distinct
group.and("field", value);
group.and("field", value, ExpresionOperator.GREATER_THAN);
group.or("field", value);
group.or(function, value, ExpresionOperator.LIKE);
group.in("field", list);
group.in("field", list, LogicalOperator.OR);

// Attach to parent
options.and(group);   // AND ( group expressions )
options.or(group);    // OR  ( group expressions )
```

### Function — SQL functions as expression left-hand side

All functions implement `ExpresionSide` — pass them directly to `options.and()` / `options.or()`.

```java
// LOWER(field)
Function fn = Function.lower(FieldParam.getInstance("description"));

// UPPER(field)
Function fn = Function.upper(FieldParam.getInstance("description"));

// REPLACE(source, pattern, replacement)
// source can be a field, a string literal, or another Function
Function fn = Function.replace(FieldParam.getInstance("description"), "á", "a");

// Chaining — each Function wraps the previous:
// REPLACE(REPLACE(LOWER(description), 'á', 'a'), 'é', 'e')
Function f1 = Function.lower(FieldParam.getInstance("description"));
Function f2 = Function.replace(f1, "á", "a");
Function f3 = Function.replace(f2, "é", "e");
options.and(f3, "%hola%", ExpresionOperator.LIKE);
```

`FieldParam.getInstance("fieldName")` wraps a plain field name into the `ExpresionSide` interface required by `Function` and `options.and(ExpresionSide, ...)`.

### Aggregation — all methods

```java
Aggregation.count()          // COUNT(*)
Aggregation.max("FIELD")     // MAX(FIELD)  — field is the DB column name
Aggregation.min("FIELD")     // MIN(FIELD)
Aggregation.sum("FIELD")     // SUM(FIELD)
Aggregation.avg("FIELD")     // AVG(FIELD)
```

Return types from `db.calculate()`:
- `count`, `max`, `min`, `sum` → `Long`
- `avg`, `sum` on REAL columns → `Float`

### DBs — multi-database pool singleton

```java
DBs.getInstance().getDB(dbConfig)       // returns cached DB or creates new one
DBs.getInstance().removeDB(dbConfig)    // removes from pool (does not close)
```

Pool key is `url + "/" + databaseName`. Same config always returns same `DB` instance.

### UpgradeListener

```java
public class MyUpgrade implements UpgradeListener {
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // oldVersion == -1 → first use of this DB on an external path (no METADATA yet)
        // oldVersion >= 0  → normal version bump
        if (oldVersion < 0) {
            db.execSQL("ALTER TABLE T ADD COLUMN extra TEXT");
        } else {
            db.execSQL("ALTER TABLE T ADD COLUMN v" + newVersion + " REAL DEFAULT 0");
        }
    }
}
```

Version is stored in a `METADATA` table (managed by `UpgradeHelper`). `onUpgrade` fires only when `DBConfig.version != stored_version`.

---

## Complex Query Pattern (normalized full-text search)

```java
// SQL equivalent:
//   SELECT * FROM Marcador
//   WHERE REPLACE(REPLACE(LOWER(description),'á','a'),'é','e') LIKE '%hola%'
//     AND ( language >= 1 AND language <= 5
//           OR id IN (2, 4, 7) )
//   LIMIT 100

Function f1 = Function.lower(FieldParam.getInstance("description"));
Function f2 = Function.replace(f1, "á", "a");
Function f3 = Function.replace(f2, "é", "e");

Options options = new Options();
options.and(f3, "%hola%", ExpresionOperator.LIKE);

Group group = new Group(options);
group.and("language", 1, ExpresionOperator.GREATER_THAN_OR_EQUAL_TO);
group.and("language", 5, ExpresionOperator.LESS_THAN_OR_EQUAL_TO);
group.in("ID", new ArrayList<>(Arrays.asList(2, 4, 7)), LogicalOperator.OR);
options.and(group);

options.limit(100);

ArrayList<Marcador> results = db.getAll(Marcador.class, options);
```
