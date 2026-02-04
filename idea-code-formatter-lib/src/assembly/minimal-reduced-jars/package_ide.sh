#!/bin/bash
DOWNLOAD_FILE=target/idea.original.zip
IDEA_DIR=target/idea
IDEA_REDUCED_DIR=target/idea-reduced
REPACKAGED_IDEA_DIR=../idea-code-formatter/target/repackaged

echo unzipping minimal idea lib and plugin files
mkdir -p $IDEA_DIR
while IFS="" read -r jar || [ -n "$jar" ]
do
  # extract the jar from the idea zip file
  unzip -o $DOWNLOAD_FILE "$jar" -d $IDEA_DIR
  # unzip the jar file to an own target-dir
  jarfilename=$(basename $jar .jar)
  mkdir -p target/$jarfilename
  unzip -o -q "$IDEA_DIR/$jar" -d target/$jarfilename
done < src/assembly/minimal-jars.txt
