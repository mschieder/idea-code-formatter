# idea-code-formatter

[![Java CI with Maven](https://github.com/mschieder/idea-code-formatter/actions/workflows/maven.yml/badge.svg?branch=main)](https://github.com/mschieder/idea-code-formatter/actions/workflows/maven.yml)

is a "smaller" (200 MB) standalone version of the [IntelliJ
IDEA command line formatter](https://www.jetbrains.com/help/idea/command-line-formatter.html)

build:
mvn install

usage:

```
$ java -jar idea-code-formatter-1.0.0-SNAPSHOT.jar
IntelliJ IDEA 2022.3.3, build IC-223.8836.41 Formatter

Usage: format [-h] [-r|-R] [-d|-dry] [-s|-settings settingsPath] [-charset charsetName] [-allowDefaults] path1 path2...
-h|-help         Show a help message and exit.
-s|-settings     A path to Intellij IDEA code style settings .xml file. This setting will be
be used as a primary one regardless to the surrounding project settings
-r|-R            Scan directories recursively.
-d|-dry          Perform a dry run: no file modifications, only exit status.
-m|-mask         A comma-separated list of file masks.
-charset         Force charset to use when reading and writing files.
-allowDefaults   Use factory defaults when style is not defined for a given file. I.e. when -s
is not not set and file doesn't belong to any IDEA project. Otherwise file will
be ignored.
path<n>        A path to a file or a directory.
```
