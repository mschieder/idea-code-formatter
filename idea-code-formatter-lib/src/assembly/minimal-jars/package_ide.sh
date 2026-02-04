#!/bin/bash
DOWNLOAD_FILE=target/idea.original.zip
IDEA_DIR=target/idea
REPACKAGED_IDEA_DIR=../idea-code-formatter/target/repackaged

echo unzipping minimal idea lib and plugin files
mkdir -p $IDEA_DIR
while IFS="" read -r jar || [ -n "$jar" ]
do
  unzip -o $DOWNLOAD_FILE "$jar" -d $IDEA_DIR
done < src/assembly/minimal-jars.txt

echo packaging ide with minimal jars
mkdir -p $REPACKAGED_IDEA_DIR
pushd target
cp *.jar idea/lib  #include also idea-code-formatter-lib
zip -r idea.zip idea
popd
mv target/idea.zip $REPACKAGED_IDEA_DIR