#!/bin/bash

IDEA_DIR=target/idea
IDEA_REDUCED_DIR=target/idea-reduced
DEST_DIR=../idea-code-formatter/target/repackaged

cp target/*.jar $IDEA_REDUCED_DIR/lib

mv $IDEA_DIR target/idea-minimal
mv $IDEA_REDUCED_DIR $IDEA_DIR

mkdir -p $DEST_DIR
pushd target
zip -r idea.zip idea
popd
mv target/idea.zip $DEST_DIR

