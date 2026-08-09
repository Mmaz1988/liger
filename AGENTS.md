    # AGENTS.md

## Scope
- LiGER is a Java 17 / Maven Spring Boot repo; trust `pom.xml` over `README.md` when they differ.
- The real executable entrypoint is `de.ukon.liger.main.DbaMain`; `de.ukon.liger.webservice.WebApplication` is the Spring Boot web app that `DbaMain` launches with `-web`.
- Use `./mvnw`, not a system Maven install.

## Build And Verify
- `./mvnw test` is the default verification.
- `./mvnw -Dtest=DemoApplicationTests test` runs the only actual Maven test under `src/test/java`.
- `*Test.java` files under `src/main/java` can contain JUnit tests, but they are outside the default Surefire test source set.

## Runtime Layout
- `-res <path>` is required for local runs; `PathVariables` expects `testFiles/` and `dicts/` under that resource root.
- Without `-res`, the app looks for `<cwd>/liger_resources` first, then `~/liger_resources`.
- The Maven build copies `liger_resources/` into `target/liger_resources/`.
- Do not edit generated output under `target/`, `liger_resources/tmp/`, or grammar `fileindexdir/` folders unless the task is specifically about generation.

## Main Workflows
- Core graph expansion and rewriting lives in `de.ukon.liger.analysis.RuleParser`, `de.ukon.liger.syntax.xle`, and `de.ukon.liger.semantics.GlueSemantics`.
- `de.ukon.liger.analysis.graphParser` is present, but it is not wired into the main CLI or web paths; treat it as standalone unless the task is specifically about graph-query parsing.
- `-i` parses XLE `.pl` output; `-rf` applies rewrite rules; `-o` writes output; `-mc` extracts meaning constructors; `-multi` enables multistage semantics; `-gf glue|prolog` selects glue grammar format; `-glue2lfg <file>` converts a glue grammar file.
- `GlueSemanticsParser` rewrites `.lfg.glue` files to `.lfg` in place by walking the containing directory. Treat that as a generator, not a pure parser.
- `-web` starts the Spring Boot service on port 8080 after initializing paths.
- `XLEoperator` shells out to XLE and uses WSL-specific command formatting on Windows; preserve that path handling when touching parser code.

## Analysis Package
- `de.ukon.liger.analysis.QueryParser` is the query language implementation; `src/main/java/de/ukon/liger/test/QueryParserTest.java` is the best executable spec for what it accepts.
- `QueryParser` tokenizes on whitespace. The tested syntax includes `#` node variables, `%` value variables, quoted literals, `&`, `==`, `!=`, `strip(...)`, functional uncertainty with `!()` and `^()`, and `*` on relation chains.
- A `QueryParser` sees both `fs.constraints` and `fs.annotation`, so queries can match previously added annotation as well as the base structure.
- `QueryParserResult.isSuccess` is only `!result.keySet().isEmpty()`; the nested `result` map is the real payload.
- `de.ukon.liger.analysis.RuleParser.RuleParser` consumes the same query syntax on the left side of a rule. `Rule.splitGoal()` breaks the right side on `&`, `replace=true` can come from the rule file directive or `setReplace(true)`, and `=->` enables rewrite mode while `+->` marks branching.
- `RuleParser.addAnnotation2(...)` mutates the passed `LinguisticStructure` in place, and the tests assert on `annotation.size()`. Do not treat it as a pure function.
- When a rule file is involved, remember that `RuleParser` parses the whole file format, not just one rule string.

## Editing Notes
- Prefer small changes in the specific workflow you are touching: rewrite rules, XLE parsing, glue conversion, or web DTO/controller code.
- If you change anything around startup, resource lookup, or semantics extraction, check both CLI and `-web` paths.
