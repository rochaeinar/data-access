# Data Access Layer (DAL)

Lightweight Android ORM. SQLite abstraction via annotations — no code generation, no external dependencies.

## Setup

Add JitPack to your project `build.gradle`:

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Add the dependency to your module `build.gradle`:

```groovy
dependencies {
    implementation 'com.github.rochaeinar:data-access:1.0.23'
}
```

---

## Defining an Entity

```java
@Table(name = "Marcador")          // name is optional; defaults to class simple name
public class Marcador extends Entity {

    @PrimaryKey
    @Field(name = "ID")            // name is optional; defaults to field name
    public long id;

    @Field public String description;
    @Field public char code;
    @Field public int language;
    @Field public boolean status;
    @Field public Date date;
    @Field public short myShort;
    @Field public double myDouble;
    @Field public float myFloat;

    public Marcador() { super(); }
}
```

**Supported types:** `String`, `char`/`Character`, `int`/`Integer`, `long`/`Long`,
`short`/`Short`, `boolean`/`Boolean`, `double`/`Double`, `float`/`Float`, `Date`.

---

## Initializing the Database

```java
// Default internal database (auto-named from package)
DB db = new DB(getApplicationContext());

// Custom name and version
DBConfig config = new DBConfig(context, "mydb.db", 1, null);
DB db = new DB(config);

// External path (SD card, app-specific dir, etc.)
DBConfig config = new DBConfig(context, "mydb.db", 1, "/sdcard/myapp");
DB db = new DB(config);

// With upgrade listener
config.setOnUpgradeListener(new MyUpgradeListener());

// Limit entity scan to a specific package (faster init)
config.setPackageFilter("com.myapp.model");

// Re-initialize (clears cache, re-opens)
db.initialize();
```

---

## Error Handling

All DAL operations throw `DALException` (unchecked `RuntimeException`) on failure.
Catch it wherever you need to handle DB errors gracefully:

```java
try {
    DB db = new DB(config);
    db.save(entity);
} catch (DALException e) {
    // e.getMessage() → "Failed to open database: /sdcard/mydb.db"
    // e.getCause()   → original SQLiteException
}
```

---

## CRUD

```java
// INSERT — auto-generates ID if id == 0
Marcador m = new Marcador();
m.description = "Hello";
db.save(m);

// UPDATE — detects existing ID and issues UPDATE
m.description = "Updated";
db.save(m);

// UPDATE by condition (no PK needed)
m.description = "Bulk update";
Options cond = new Options();
cond.and("language", 3);
db.save(m, cond);

// GET by ID
Marcador found = db.getById(Marcador.class, 1);

// GET ALL
ArrayList<Marcador> all = db.getAll(Marcador.class);

// REMOVE by ID
db.remove(Marcador.class, 1);
```

---

## Options — Filtering, Ordering, Pagination

### Basic AND / OR

```java
Options options = new Options();

// AND field = value  (EQUALS is default)
options.and("language", 1);
options.and("status", true);

// AND with explicit operator
options.and("ID", 5, ExpresionOperator.GREATER_THAN);
options.and("ID", 10, ExpresionOperator.LESS_THAN_OR_EQUAL_TO);

// OR
options.or("language", 2);

ArrayList<Marcador> result = db.getAll(Marcador.class, options);
```

### All ExpresionOperators

| Constant | SQL |
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
| `IN` | `IN` *(used internally by `in()`)* |

### IN

```java
Options options = new Options();

// AND id IN (1, 3, 5)
options.in("ID", new ArrayList<>(Arrays.asList(1, 3, 5)));

// OR id IN (7, 9)
options.in("ID", new ArrayList<>(Arrays.asList(7, 9)), LogicalOperator.OR);
```

### ORDER BY, DISTINCT, LIMIT, OFFSET

```java
Options options = new Options();
options.orderBy("description", true);   // true = ASC, false = DESC
options.distinct(true);
options.limit(20);
options.offset(40);                     // only effective when limit is set
```

---

## Group — Parenthesized Sub-expressions

`Group` wraps conditions inside `( ... )`, allowing complex boolean logic.

```java
// WHERE language = 1 AND ( id >= 3 AND id <= 7 )
Options options = new Options();
options.and("language", 1);

Group group = new Group(options);
group.and("ID", 3, ExpresionOperator.GREATER_THAN_OR_EQUAL_TO);
group.and("ID", 7, ExpresionOperator.LESS_THAN_OR_EQUAL_TO);
options.and(group);     // attach as AND ( ... )

// options.or(group);   // attach as OR ( ... )
```

```java
// WHERE status = true AND ( id IN (1,2) OR id IN (8,9,10) )
Options options = new Options();
options.and("status", true);

Group group = new Group(options);
group.in("ID", new ArrayList<>(Arrays.asList(1, 2)));
group.in("ID", new ArrayList<>(Arrays.asList(8, 9, 10)), LogicalOperator.OR);
options.and(group);
```

```java
// WHERE ( language = 0 OR language = 2 ) OR ( status = true AND id > 5 )
Options options = new Options();

Group g1 = new Group(options);
g1.and("language", 0);
g1.or("language", 2);
options.and(g1);

Group g2 = new Group(options);
g2.and("status", true);
g2.and("ID", 5, ExpresionOperator.GREATER_THAN);
options.or(g2);
```

---

## Aggregation

```java
Long   count = db.calculate(Marcador.class, Aggregation.count());
Long   maxId = db.calculate(Marcador.class, Aggregation.max("ID"));
Long   minId = db.calculate(Marcador.class, Aggregation.min("ID"));
Long   sum   = db.calculate(Marcador.class, Aggregation.sum("language"));
Float  avg   = db.calculate(Marcador.class, Aggregation.avg("language"));

// Aggregation with filter
Options filter = new Options();
filter.and("status", true);
Long countActive = db.calculate(Marcador.class, Aggregation.count(), filter);
```

> `count()` returns `Long`. `avg()` and `sum()` on REAL columns return `Float`.

---

## Function — SQL Functions on Columns

`Function` builds SQL expressions applied to a column. Used as the left-hand side of an `and()`/`or()` condition.

```java
// LOWER(description) LIKE '%hello%'
Function fn = Function.lower(FieldParam.getInstance("description"));
options.and(fn, "%hello%", ExpresionOperator.LIKE);

// UPPER(description) LIKE '%WORLD%'
Function fn = Function.upper(FieldParam.getInstance("description"));
options.and(fn, "%WORLD%", ExpresionOperator.LIKE);

// REPLACE(description, ' ', '-') = 'Hello-World'
Function fn = Function.replace(FieldParam.getInstance("description"), " ", "-");
options.and(fn, "Hello-World", ExpresionOperator.EQUALS);
```

### Chaining Functions (nested calls)

```java
// REPLACE(REPLACE(LOWER(description), 'á', 'a'), 'é', 'e') LIKE '%hola%'
Function f1 = Function.lower(FieldParam.getInstance("description"));
Function f2 = Function.replace(f1, "á", "a");
Function f3 = Function.replace(f2, "é", "e");
options.and(f3, "%hola%", ExpresionOperator.LIKE);
```

---

## Complex Example — Normalized Full-text Search

```java
// SQL generated:
//   SELECT * FROM Marcador
//   WHERE REPLACE(REPLACE(REPLACE(LOWER(description),'á','a'),'é','e'),'ó','o')
//         LIKE '%hello%'
//     AND ( language >= 1 AND language <= 5
//           OR id IN (2, 4, 7) )
//   LIMIT 100;

String textNormalized = "héllo"
    .toLowerCase()
    .replace("á","a").replace("é","e").replace("í","i")
    .replace("ó","o").replace("ú","u");

Function f1 = Function.lower(FieldParam.getInstance("description"));
Function f2 = Function.replace(f1, "á", "a");
Function f3 = Function.replace(f2, "é", "e");
Function f4 = Function.replace(f3, "í", "i");
Function f5 = Function.replace(f4, "ó", "o");
Function f6 = Function.replace(f5, "ú", "u");

Options options = new Options();
options.and(f6, "%" + textNormalized + "%", ExpresionOperator.LIKE);

Group group = new Group(options);
group.and("language", 1, ExpresionOperator.GREATER_THAN_OR_EQUAL_TO);
group.and("language", 5, ExpresionOperator.LESS_THAN_OR_EQUAL_TO);
group.in("ID", new ArrayList<>(Arrays.asList(2, 4, 7)), LogicalOperator.OR);
options.and(group);

options.limit(100);

ArrayList<Marcador> results = db.getAll(Marcador.class, options);
```

---

## Raw SQL

```java
// Execute arbitrary DDL/DML
db.execSQL("ALTER TABLE Marcador ADD COLUMN extra TEXT");
db.execSQL("UPDATE Marcador SET language = 0 WHERE status = 0");

// Raw SELECT — returns Cursor (caller must close it)
Cursor cursor = db.rawQuery("SELECT ID, description FROM Marcador WHERE language = 1");
while (cursor != null && cursor.moveToNext()) {
    long id   = cursor.getLong(cursor.getColumnIndex("ID"));
    String desc = cursor.getString(cursor.getColumnIndex("description"));
}
if (cursor != null && !cursor.isClosed()) cursor.close();
```

---

## DBs — Multi-database Pool (Singleton)

`DBs` manages a `HashMap<path, DB>` so the same database file is never opened twice.

```java
DBConfig config1 = new DBConfig(context, null, 1, null);
DBConfig config2 = new DBConfig(context, "bible.db", 1, "/sdcard/bibles");

DB db1 = DBs.getInstance().getDB(config1);
DB db2 = DBs.getInstance().getDB(config2);

// Remove from pool (call before config object changes version/path)
DBs.getInstance().removeDB(config2);
```

---

## Database Upgrade

```java
public class MyUpgrade implements UpgradeListener {
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // oldVersion == -1 means first-time creation on an external path
        if (oldVersion < 0) {
            db.execSQL("ALTER TABLE SETTINGS ADD COLUMN extra TEXT");
        } else if (newVersion > oldVersion) {
            db.execSQL("ALTER TABLE SETTINGS ADD COLUMN v" + newVersion + " REAL DEFAULT 0");
        }
    }
}

DBConfig config = new DBConfig(context, "mydb.db", 2, null);
config.setOnUpgradeListener(new MyUpgrade());
DB db = new DB(config);
```

> The `UpgradeHelper` stores the current version in a `METADATA` table. `onUpgrade` is only called when `DBConfig.version != stored version`.
