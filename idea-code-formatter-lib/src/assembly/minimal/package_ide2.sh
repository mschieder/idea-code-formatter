#!/bin/bash

IDEA_DIR=target/idea
DEST_DIR=../idea-code-formatter/target/repackaged
rm $IDEA_DIR/lib/*.jar
cp target/*.jar $IDEA_DIR/lib

mkdir -p $DEST_DIR
pushd target
zip -r idea.zip idea
popd
mv target/idea.zip $DEST_DIR
