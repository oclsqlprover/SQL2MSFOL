# SQL2MSFOL
SQL2MSFOL is an implementation of mapping from SQL statements to Many-Sorted First-Order Logic (MSFOL). This Java implementation based on the mapping definition described in the manuscript [Proving correctness for SQL implementations of OCL constraints]() submitted to MoDELS conference 2022.

In a nutshell, given a datamodel, SQL2MSFOL maps an SQL statement into a complete FOL formulae written in [SMT-LIB2](https://smtlib.cs.uiowa.edu/papers/smt-lib-reference-v2.6-r2021-05-12.pdf) language.

## Supported language and features:

Although SQL2MSFOL covers a large subset of SQL statements and expressions, 
the implementation is an on-going project. 
The following items highlight the supported subset of SQL select statement:
```
SELECT selectitems
SELECT selectitems FROM fromitem
SELECT selectitems FROM fromitem WHERE whereExpr
SELECT selectitems FROM fromitem JOIN fromitem
SELECT selectitems FROM fromitem JOIN fromitem ON onExpr
SELECT selectitems FROM fromitem JOIN fromitem WHERE whereExpr
SELECT selectitems FROM fromitem JOIN fromitem ON onExpr WHERE whereExpr
```
where
```
selectitems := expr (, expr)*
fromitem    := class AS alias
             | association-class AS alias
             | subselect AS alias
whereExpr   := boolexpr
onExpr      := boolexpr
```
and
```
expr := TRUE | FALSE | NULL    (boolean literals)
      | ... | -1 | 0 | 1 | ... (integer literals)
      | var                    (variables)
      | 'a string'             (string literals)
      | alias.id               (class-id)
      | alias.attribute        (class-attributes)
      | alias.association-end  (association-ends)
      | NOT boolexpr           (unary operations)
      | boolexpr AND boolexpr  (binary operations, i.e., AND, OR)
      | expr = expr            (binary comparisons, i.e., =, <>, >, <, >=, <=)
      | expr IS NULL           (IS NULL expression)
      | CASE WHEN boolexpr THEN expr ELSE expr END
                               (CASE expression)
      | EXISTS subselect       (EXISTS expression)
```

## How to use

### Requirements:
- (required) `Maven 3` and `Java 1.8` (or higher).
- (submodule) [`datamodel`](https://github.com/models22-submission54/dm2schema).
- The project delegates the SQL text-to-object transformation the [`JSQLParser`](https://github.com/JSQLParser/JSqlParser) library (version 4.2.x).

**Note**: These dependecies are imported in Maven.

The submodules will be updated using `Git` commands as in the guideline.

### Quick guideline:
```
git clone https://github.com/models22-submission54/SQL2MSFOL.git
```
and run the file `scripts.sh` to install the aforementioned packages locally:

```
.\scripts.sh
```
To execute the tool, have a look at the example main class `Runner.java` for a quick guideline.

Have a quick look at the `Runner.java` class for a quick guideline.

### Some SQL examples:
```
/* A boolean literal */
"SELECT TRUE"
, "SELECT FALSE"
, "SELECT NULL"
/* A integer literal */
, "SELECT 0"
, "SELECT -1"
, "SELECT 1"
/* A string literal */
, "SELECT 'a string'"
/* logical operations */
, "SELECT NOT TRUE"
, "SELECT NOT FALSE"
, "SELECT NOT NOT TRUE"
, "SELECT TRUE & TRUE"
, "SELECT FALSE | FALSE"
/* comparison operations */
, "SELECT 1 = 1"
, "SELECT 1 >= 1"
, "SELECT 1 > 1"
, "SELECT 1 <= 1"
, "SELECT 1 <> 1
, "SELECT 1 < 1"
/* CASE */
, "SELECT CASE WHEN TRUE THEN 1 ELSE 0 END"
, "SELECT CASE WHEN TRUE THEN CASE WHEN FALSE THEN 0 ELSE 1 END ELSE 2 END"
/* IS NULL */
, "SELECT 1 IS NULL"
, "SELECT 'a string' IS NULL"
, "SELECT NULL IS NULL"
, "SELECT 1 IS NOT NULL"
/* EXISTS subselect */
, "SELECT EXISTS (SELECT 1)"
/* single-valued subselect*/
, "SELECT 2 = (SELECT 1)"
/* Context dependent */
, "SELECT 1 FROM Student"
, "SELECT name FROM Student"
, "SELECT name, age FROM Student"
, "SELECT s.name FROM Student AS s"
, "SELECT age = 18 FROM Student"
, "SELECT EXISTS (SELECT age FROM Student)"
, "SELECT age FROM (SELECT age, name FROM Student)"
, "SELECT Student_id FROM Student"
, "SELECT students FROM Enrolment"
/* WHERE */
, "SELECT age FROM Student WHERE Student_id = 1 AND name = 'someone'"
, "SELECT name FROM (SELECT name, age FROM Student) AS temp WHERE age > 19"
, "SELECT name FROM (SELECT name, age FROM Student WHERE age > 19) AS temp WHERE age > 19"
/* JOIN */
, "SELECT e.lecturers, s.name FROM Student AS s JOIN Enrolment AS e"
, "SELECT 1, s1.name, s2.age FROM Student AS s1 JOIN Student AS s2"
, "SELECT temp2.age FROM (SELECT 1 AS res) AS temp1 JOIN (SELECT age FROM Student) AS temp2"
/* JOIN ON */
, "SELECT e.lecturers, s.name FROM Student AS s JOIN Enrolment AS e ON e.students = s.Student_id"
/* JOIN WHERE */
, "SELECT e.lecturers, s.name FROM Student AS s JOIN Enrolment AS e WHERE e.students = s.Student_id"
/* JOIN ON WHERE */
, "SELECT e.lecturers, s.name FROM Student AS s JOIN Enrolment AS e ON e.students = s.Student_id WHERE s.age > 19"
/* Examples in the manuscript */
, "SELECT TRUE"
, "SELECT NOT EXISTS (SELECT students FROM Enrolment WHERE lecturers = caller)"
, "SELECT age >= 18 FROM Student WHERE Student_id = self"
, "SELECT NOT EXISTS (SELECT 1 FROM (SELECT s.age, e.lecturers FROM Student AS s JOIN Enrolment AS e ON e.students = s.Student_id) AS temp JOIN Lecturer AS l WHERE temp.age >= l.age AND l.Lecturer_id = temp.lecturers)"
, "SELECT (SELECT age FROM Student WHERE Student_id = self) = (SELECT age FROM Lecturer WHERE Lecturer_id = caller)"
, "SELECT (SELECT name FROM Student WHERE Student_id = self) = user"
, "SELECT name = user FROM Student WHERE Student_id = self"
, "SELECT CASE WHEN name IS NULL THEN user IS NULL ELSE CASE WHEN user IS NULL THEN FALSE ELSE name = user END END FROM Student WHERE Student_id = self"
```
