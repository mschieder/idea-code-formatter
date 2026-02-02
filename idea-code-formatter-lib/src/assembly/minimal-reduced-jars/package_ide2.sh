#!/bin/bash

IDEA_DIR=target/idea
IDEA_REDUCED_DIR=target/idea-reduced
DEST_DIR=../idea-code-formatter/target/repackaged

#rm $IDEA_DIR/lib/*.jar

cp target/*.jar $IDEA_REDUCED_DIR/lib

mv $IDEA_DIR target/idea-minimal
mv $IDEA_REDUCED_DIR $IDEA_DIR

mkdir -p $DEST_DIR
pushd target
zip -r idea.zip idea
popd
mv target/idea.zip $DEST_DIR
zipinfo -1 $DEST_DIR/idea.zip
echo idea.zip file list md5sum: $(zipinfo -1 $DEST_DIR/idea.zip | md5sum)
