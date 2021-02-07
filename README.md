# About

This repository contains a system for expanding linguistic annotations. Inspired by a proposal in Ide & Bunt (2010), linguistic annotations are translated into "abstract" graph representations that may be expanded using the present system (The graph structure proposed here is more simple than the one proposed in Ide & Bunt). Presently, the system provides native support for the output produced by XLE grammars and for the output produced by the dependency parser of the Stanford CoreNLP (i.e. universal dependencies). Furthermore, the system makes use of the Glue Semantics Workbench (GSWB) making it particularly useful for formal semantic analysis. The system is embedded into a micro-service architecture based on Spring Boot. This component is currently under development (see https://github.com/Mmaz1988/abstract-syntax-annotator-client for a small Demo). 

# Licensing
The abstract-syntax-annotator
Copyright 2021 Mark-Matthias Zymla.
This file is part of the abstract-syntax-annotator.
The abstract-syntax-annotator is free software and distributed under the conditions of the GNU General Public License,
without any warranty.
You should have received a copy of the GNU General Public License along with the source code.
If not, please visit http://www.gnu.org/licenses/ for more information.

# Requirements:

- This system uses Java. [OpenJDK](https://jdk.java.net/13/) is used for development. Support for other versions is not guaranteed, but please contact me if there are issues. 

# Installation:
1. To install this repository, simply import it as a maven project into your IDE. To use the -sem function for semantic analysis, install the GlueSemanticsWorkbench.jar to your local maven repository. To do this navigate to the project folder and execute the following command, or execute it as a maven goal in your IDE:

```
mvn install:install-file -Dfile=resources\glueSemWorkbench2.jar -DgroupId=uni.kn.zymla -DartifactId=gswb -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
```

# Running from Jar file

- Download the Jar file from: 
- Download the resources folder from this repository

Now you're ready to execute the jar file by navigating to the folder containing the jar file in the shell/terminal and executing the following command(replace path\to\resources with the path to the copy of resources stored on your computer):
```
java -jar syntax-annotator-glue-0.0.1-SNAPSHOT.jar -res path/to/resources/
```

To use the micro-service version with the web interface use the "-web" command argument like so: 

```
java -jar syntax-annotator-glue-0.0.1-SNAPSHOT.jar -res path/to/resources/ -web
```

# Command arguments 

The following table presents the possible command line arguments: 

| `command line argument` | `effect` |
| ------------- | ------------- | 
| `-res` | `obligatory argument pointing to the resource folder in the repository` |
| `-web` | `used to run the system as a micro-service` |
| `-i [path/to/file]`  | `used to specify an input file (XLE output; .pl file)` |
| `-o [path/to/file]` | `specify an output file` |
| `-rf [path/to/file]` | `specify a rule file (see "resources/testFiles" for examples)` |
| `-dep/-lfg` | run interactive mode with dependency or XLE parser, when no input file is specified |
| `-sem` | `Collect meaning constructors and run glue prover after rewriting is complete` |

# Using the system

The system can be used to rewrite and expand linguistic annotations. Input needs to be translated into a fact-based graph representation consisting of a (mother) node reference, a relation label, and either a (daugther) node reference or a string value. This is done natively for XLE putput and for the output of the Universal Dependency parser contained in the Stanford CoreNLP. 

The rewrite rules are specified by means of the `-rf [path/to/file]` argument. They are applied one after another, given the left-hand side matches the input structure combined with the previously added structures. The following example illustrates how the rules can be used to produce a universal quantifier based on a universal dependency parse. The input is a list of facts provided by a universal dependency parse.

## Input
`[ #2 det #1,  #1 TOKEN every,  #1 POS DT,  #1 LEMMA every,  #3 nsubj #2,  #2 TOKEN man,  #2 POS NN,  #2 LEMMA man,  #0 root #3,  #3 TOKEN loves,  #3 POS VBZ,  #3 LEMMA love,  #5 det #4,  #4 TOKEN a,  #4 POS DT,  #4 LEMMA a,  #3 obj #5,  #5 TOKEN woman,  #5 POS NN,  #5 LEMMA woman]`

This input can be expanded by adding partial semantic representations. The following rules illustrate, how a universal quantifier can be added in several steps, based on the treatment of universal quantifiers in Glue semantics, in particular, in Dalrymple (1999). First, the additional structures `VAR` and `RESTR` are introduced. Using these additional nodes the restrictor of the quantifier is defined based on the element that the quantifier attaches to (in this case "man"). Finally, the quantifier is derived from the semantics of the noun phrase and the semantics of the rest of the sentence, or more concretely, the semantics of the element that dominates the noun phrase. 

## Rewrite rules
```
//Setup for Quantifier
#a ^(det) #b ==> #b SEM #c & #c VAR #d & #c RESTR #e.

//predicative NP (restrictor of quantifier)
#a SEM #b VAR #c & #b RESTR #d & #a TOKEN %a ==> #a GLUE [/x_e.%a(x)] : (#c -o #d).

//universal quantifier
#a ^(det) #b SEM #c VAR #d & #c RESTR #e &
#a TOKEN %a & %a == every &
#b ^(%) #f SEM #g
==> #c GLUE [/P_<e,t>.[/Q_<e,t>.Ax_e[P(x) -> Q(x)]]] : ((#d -o #e) -o ((#c -o #g) -o #g)).
```
The resulting partial semantic representations are shown in (note that the rules above add further additional structures omitted in the example below): 

```
 #2 GLUE [/x_e.man(x)] : (10 -o 12)
 #8 GLUE [/P_<e,t>.[/Q_<e,t>.Ax_e[P(x) -> Q(x)]]] : ((10 -o 12) -o ((8 -o 6) -o 6))
```
If the rules are used to produce Glue semantics output(by using the `-sem` argument), all GLUE nodes are collected and a proof is derived. For this, the Glue semantics workbench is used (see [GSWB](https://github.com/Mmaz1988/GlueSemWorkbench_v2/tree/pure%2Bdrt)). It is included in the jar file, but needs to be installed explicitly when trying to build new versions of the project (see section "Installation"). 

```
Sequent:
(10 ⊸ 12) : [λx_e.man(x)]
(11 ⊸ 13) : [λx_e.woman(x)]
((7 ⊸ 6) ⊸ (8 ⊸ (9 ⊸ 6))) : [λR_<v,t>.[λx_e.[λy_e.∃e[R(e) ∧ agent(e,x) ∧ theme(e,y)]]]]
(7 ⊸ 6) : [λe_v.love(e)]
((10 ⊸ 12) ⊸ ((8 ⊸ 6) ⊸ 6)) : [λP_<e,t>.[λQ_<e,t>.∀x[P(x) → Q(x)]]]
((11 ⊸ 13) ⊸ ((9 ⊸ 6) ⊸ 6)) : [λP_<e,t>.[λQ_<e,t>.∃x[P(x) ∧ Q(x)]]]


Agenda:
(10 ⊸ 12) : [λx_e.man(x)]
(11 ⊸ 13) : [λx_e.woman(x)]
(6 ⊸ (8 ⊸ (9 ⊸ 6))) : [λR_<v,t>.[λx_e.[λy_e.∃e[R(e) ∧ agent(e,x) ∧ theme(e,y)]]]]
7 : z
(7 ⊸ 6) : [λe_v.love(e)]
8 : y1
(12 ⊸ (6 ⊸ 6)) : [λP_<e,t>.[λQ_<e,t>.∀x[P(x) → Q(x)]]]
10 : x1
9 : x2
(13 ⊸ (6 ⊸ 6)) : [λP_<e,t>.[λQ_<e,t>.∃x[P(x) ∧ Q(x)]]]
11 : z1


Combining (7 ⊸ 6) : [λe_v.love(e)] and 7 : z
to: 6 : love(z)
Combining (10 ⊸ 12) : [λx_e.man(x)] and 10 : x1
to: 12 : man(x1)
Combining (11 ⊸ 13) : [λx_e.woman(x)] and 11 : z1
to: 13 : woman(z1)
Combining (6 ⊸ (8 ⊸ (9 ⊸ 6))) : [λR_<v,t>.[λx_e.[λy_e.∃e[R(e) ∧ agent(e,x) ∧ theme(e,y)]]]] and 6 : love(z)
to: (8 ⊸ (9 ⊸ 6)) : [λx_e.[λy_e.∃e[love(e) ∧ agent(e,x) ∧ theme(e,y)]]]
Combining (12 ⊸ (6 ⊸ 6)) : [λP_<e,t>.[λQ_<e,t>.∀x[P(x) → Q(x)]]] and 12 : man(x1)
to: (6 ⊸ 6) : [λQ_<e,t>.∀x[man(x) → Q(x)]]
Combining (13 ⊸ (6 ⊸ 6)) : [λP_<e,t>.[λQ_<e,t>.∃x[P(x) ∧ Q(x)]]] and 13 : woman(z1)
to: (6 ⊸ 6) : [λQ_<e,t>.∃x[woman(x) ∧ Q(x)]]
Combining (8 ⊸ (9 ⊸ 6)) : [λx_e.[λy_e.∃e[love(e) ∧ agent(e,x) ∧ theme(e,y)]]] and 8 : y1
to: (9 ⊸ 6) : [λy_e.∃e[love(e) ∧ agent(e,y1) ∧ theme(e,y)]]
Combining (9 ⊸ 6) : [λy_e.∃e[love(e) ∧ agent(e,y1) ∧ theme(e,y)]] and 9 : x2
to: 6 : ∃e[love(e) ∧ agent(e,y1) ∧ theme(e,x2)]
Combining (6 ⊸ 6) : [λQ_<e,t>.∀x[man(x) → Q(x)]] and 6 : ∃e[love(e) ∧ agent(e,y1) ∧ theme(e,x2)]
to: 6 : ∀x[man(x) → ∃e[love(e) ∧ agent(e,x) ∧ theme(e,x2)]]
Combining (6 ⊸ 6) : [λQ_<e,t>.∃x[woman(x) ∧ Q(x)]] and 6 : ∃e[love(e) ∧ agent(e,y1) ∧ theme(e,x2)]
to: 6 : ∃x[woman(x) ∧ ∃e[love(e) ∧ agent(e,y1) ∧ theme(e,x)]]
Combining (6 ⊸ 6) : [λQ_<e,t>.∃x[woman(x) ∧ Q(x)]] and 6 : ∀y2[man(y2) → ∃e[love(e) ∧ agent(e,y2) ∧ theme(e,x2)]]
to: 6 : ∃x[woman(x) ∧ ∀y2[man(y2) → ∃e[love(e) ∧ agent(e,y2) ∧ theme(e,x)]]]
Combining (6 ⊸ 6) : [λQ_<e,t>.∀y2[man(y2) → Q(y2)]] and 6 : ∃x[woman(x) ∧ ∃e[love(e) ∧ agent(e,y1) ∧ theme(e,x)]]
to: 6 : ∀y2[man(y2) → ∃x[woman(x) ∧ ∃e[love(e) ∧ agent(e,y2) ∧ theme(e,x)]]]




Result of the Glue derivation:
6 : ∃x[woman(x) ∧ ∀y2[man(y2) → ∃e[love(e) ∧ agent(e,y2) ∧ theme(e,x)]]]
6 : ∀y2[man(y2) → ∃x[woman(x) ∧ ∃e[love(e) ∧ agent(e,y2) ∧ theme(e,x)]]]

Done
```

# Rule syntax: A quick intro

- Rules consist of a left-hand side and a right hand side separated by "==>". The end of a rule is indicated by a "." 
- Graph nodes are referred to via variable names starting with "#", e.g. #a,#b,#c,...
- Relation labels can be any strings used in the syntactic input or in facts introduced by previous rules
- Values are any strings following relation labels. They are limited by a new element in the rule (e.g. variables, conjunctions,==>) or by the "."
- Variables over values are referred to via variable names starting with "%", e.g. %a,%b,%c,...

Simple graph matching can be achieved by defining mother nodes, relation labels and daughter nodes. Several edges can be concatenated, as shown in the following example:

```
#a SEM #b VAR #c & #b RESTR #d & #a TOKEN %a
```

In directed graphs, such as f-structures and UD structures, functional application can be used to check for dominance relations without specifiying intermediate graph nodes. In the following examples "#g" and "#h" refer to the same structural nodes, but the dominance relation between them is checked in a different order by virtue of the "!"(identifying sub-structures) and "^"(identifying super structures) operators. 

```
#g !(COMP>TNS-ASP) #h
```

```
#h ^(TNS-ASP>COMP) #g
```

Functional application can also be extended to functional uncertainty by adding the "*" operator, indicating that the corresponding relation label occurs one or more times.


```
#g !(COMP*>TNS-ASP) #h
```

Values and value variables can be compared for equality or non-equality (however, note that some instances of equality can also be checked via unification):

```
TENSE %a & %a == past       //corresponds to TENSE past
TENSE %a & %a != past 
```

Values can be looked up in a lexicon, stored in resources via the predicate `lex/2`:

```
#a POS MD & #a TOKEN %a ==> #a MORPH #b & #b FIN '+' & #b FORM 'lex(%a,tense)' & #b HEAD %a.
```

The dictionary is a text file sorting values in categories:

```
***
#FUTURE_PREDICTION
threaten, vow, promise, pressure, force, urge, warn, indicate, insist, postpone, should
```

For XLE, there exists a special predicate `strip/1` that reduces the values of `PRED`s to their entry in the lexicon. 

```
#a PRED %a 
`%a == semform('seem',5,[var(8)],[var(2)])
strip(%a) == 'seem'
````



