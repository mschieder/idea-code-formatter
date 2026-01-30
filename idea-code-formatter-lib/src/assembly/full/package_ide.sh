#!/bin/bash

echo packaging full ide
IDEA_DIR=target/idea
DEST_DIR=../idea-code-formatter/target/repackaged

mkdir -p $DEST_DIR
pushd target
cp *.jar idea/lib
zip -r idea.zip idea
popd
mv target/idea.zip $DEST_DIR