# About

This repository contains a system for expanding linguistic annotations. Inspired by a proposal in Ide & Bunt (2010), linguistic annotations are translated into "abstract" graph representations that may be expanded using the present system. Presently, the system provides native support for the output produced by XLE grammars and for the output produced by the dependency parser of the Stanford CoreNLP (i.e. universal dependencies). Furthermore, the system makes use of the Glue Semantics Workbench (GSWB) making it particularly useful for formal semantic analysis. The system is embedded into a micro-service architecture based on Spring Boot. This component is currently under development (see https://github.com/Mmaz1988/abstract-syntax-annotator-client for a small Demo). 




## Licensing
The abstract-syntax-annotator
Copyright 2021 Mark-Matthias Zymla.
This file is part of the abstract-syntax-annotator.
The abstract-syntax-annotator is free software and distributed under the conditions of the GNU General Public License,
without any warranty.
You should have received a copy of the GNU General Public License along with the source code.
If not, please visit http://www.gnu.org/licenses/ for more information.

# Installation:
To install this repository, simply import it as a maven project into your IDE. To use the -sem function for semantic analysis, install the GlueSemanticsWorkbench.jar to your local maven repository. To do this navigate to the project folder and execute the following command, or execute it as a maven goal in your IDE:

```
mvn install:install-file -Dfile=resources\glueSemWorkbench2.jar -DgroupId=uni.kn.zymla -DartifactId=gswb -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true
```
