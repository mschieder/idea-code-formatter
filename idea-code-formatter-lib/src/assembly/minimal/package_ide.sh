#!/bin/bash

# unzip all lib classes and lib resources
IDEA_DIR=target/idea
mkdir -p target/lib-classes/
unzip -q -o "$IDEA_DIR/lib/*.jar" -d target/lib-classes/