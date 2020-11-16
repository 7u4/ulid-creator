
# ULID Creator

A Java library for generating ULIDs.

* Generated in lexicographical order;
* Can be stored as a UUID/GUID;
* Can be stored as a string of 26 chars;
* String format is encoded to [Crockford's base32](https://www.crockford.com/base32.html);
* String format is URL safe, case insensitive and accepts hyphens.

How to Use
------------------------------------------------------

Create a ULID:

```java
UUID ulid = UlidCreator.getUlid(); // 01706d6c-6aad-c795-370c-98d0be881bba
```

Create a ULID string:

```java
String ulid = UlidCreator.getUlidString(); // 01E1PPRTMSQ34W7JR5YSND6B8Z
```

### Maven dependency

Add these lines to your `pom.xml`.

```xml
<!-- https://search.maven.org/artifact/com.github.f4b6a3/ulid-creator -->
<dependency>
  <groupId>com.github.f4b6a3</groupId>
  <artifactId>ulid-creator</artifactId>
  <version>2.3.3</version>
</dependency>
```
See more options in [maven.org](https://search.maven.org/artifact/com.github.f4b6a3/ulid-creator).

Implementation
------------------------------------------------------

### ULID

The GUIDs in this library are based on the [ULID specification](https://github.com/ulid/spec). The first 48 bits represent the count of milliseconds since Unix Epoch, 1 January 1970. The remaining 60 bits are generated by a secure random number generator.

Every time the timestamp changes the random part is reset to a new random value. If the current timestamp is equal to the previous one, the random bits are incremented by 1.

The default random number generator is `java.security.SecureRandom`.

```java
// GUID based on ULID spec
UUID ulid = UlidCreator.getUlid();
```

```java
// GUID based on ULID spec
// Compatible with RFC-4122 UUID v4
UUID ulid = UlidCreator.getUlid4();
```

Sequence of GUIDs based on ULID spec:

```text
01706d6c-6aac-80bd-7ff5-f660c2dd58ea
01706d6c-6aac-80bd-7ff5-f660c2dd58eb
01706d6c-6aac-80bd-7ff5-f660c2dd58ec
01706d6c-6aac-80bd-7ff5-f660c2dd58ed
01706d6c-6aac-80bd-7ff5-f660c2dd58ee
01706d6c-6aac-80bd-7ff5-f660c2dd58ef
01706d6c-6aac-80bd-7ff5-f660c2dd58f0
01706d6c-6aac-80bd-7ff5-f660c2dd58f1
01706d6c-6aad-c795-370c-98d0be881bb8 < millisecond changed
01706d6c-6aad-c795-370c-98d0be881bb9
01706d6c-6aad-c795-370c-98d0be881bba
01706d6c-6aad-c795-370c-98d0be881bbb
01706d6c-6aad-c795-370c-98d0be881bbc
01706d6c-6aad-c795-370c-98d0be881bbd
01706d6c-6aad-c795-370c-98d0be881bbe
01706d6c-6aad-c795-370c-98d0be881bbf
            ^ look                 ^ look
                                   
|------------|---------------------|
  millisecs        randomness
```

### ULID string

The ULID string is a sequence of 26 chars. See the [ULID specification](https://github.com/ulid/spec) for more information.

See the section on GUIDs to know how the 128 bits are generated in this library.

```java
// String based on ULID spec
String ulid = UlidCreator.getUlidString();
```

```java
// String based on ULID spec
// Compatible with RFC-4122 UUID v4
String ulid = UlidCreator.getUlidString4();
```

Sequence of Strings based on ULID spec:

```text
01E1PPRTMSQ34W7JR5YSND6B8T
01E1PPRTMSQ34W7JR5YSND6B8V
01E1PPRTMSQ34W7JR5YSND6B8W
01E1PPRTMSQ34W7JR5YSND6B8X
01E1PPRTMSQ34W7JR5YSND6B8Y
01E1PPRTMSQ34W7JR5YSND6B8Z
01E1PPRTMSQ34W7JR5YSND6B90
01E1PPRTMSQ34W7JR5YSND6B91
01E1PPRTMTYMX8G17TWSJJZMEE < millisecond changed
01E1PPRTMTYMX8G17TWSJJZMEF
01E1PPRTMTYMX8G17TWSJJZMEG
01E1PPRTMTYMX8G17TWSJJZMEH
01E1PPRTMTYMX8G17TWSJJZMEJ
01E1PPRTMTYMX8G17TWSJJZMEK
01E1PPRTMTYMX8G17TWSJJZMEM
01E1PPRTMTYMX8G17TWSJJZMEN
         ^ look          ^ look
                                   
|---------|--------------|
 millisecs   randomness
```

### How use the `UlidSpecCreator` directly

These are some examples of using the `UlidSpecCreator` to create ULID strings:

```java
// with your custom timestamp strategy
TimestampStrategy customStrategy = new CustomTimestampStrategy();
UlidSpecCreator creator = UlidCreator.getUlidSpecCreator()
	.withTimestampStrategy(customStrategy);
String ulid = creator.createString();
```
```java
// with your custom random strategy that wraps any random generator
RandomStrategy customStrategy = new CustomRandomStrategy();
UlidSpecCreator creator = UlidCreator.getUlidSpecCreator()
	.withRandomStrategy(customStrategy);
String ulid = creator.createString();
```
```java
// with `java.util.Random` number generator
Random random = new Random();
UlidSpecCreator creator = UlidCreator.getUlidSpecCreator()
    .withRandomGenerator(random);
String ulid = creator.createString();
```

Benchmark
------------------------------------------------------

This section shows benchmarks comparing `UlidCreator` to `java.util.UUID`.

```
================================================================================
THROUGHPUT (operations/millis)           Mode  Cnt      Score     Error   Units
================================================================================
Throughput.JDK_RandomBased              thrpt    5   2050,995 ±  21,636  ops/ms
--------------------------------------------------------------------------------
Throughput.UlidCreator_Ulid             thrpt    5  18524,721 ± 563,781  ops/ms
Throughput.UlidCreator_UlidString       thrpt    5  12223,501 ±  89,836  ops/ms
================================================================================
Total time: 00:04:00
================================================================================
```

System: JVM 8, Ubuntu 20.04, CPU i5-3330, 8G RAM.

See: [uuid-creator-benchmark](https://github.com/fabiolimace/uuid-creator-benchmark)

Links for generators
-------------------------------------------
* [UUID Creator](https://github.com/f4b6a3/uuid-creator)
* [ULID Creator](https://github.com/f4b6a3/ulid-creator)
* [TSID Creator](https://github.com/f4b6a3/tsid-creator)
