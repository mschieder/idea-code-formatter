#!/bin/bash
DOWNLOAD_FILE=target/idea.original.zip
IDEA_DIR=target/idea
IDEA_REDUCED_DIR=target/idea-reduced
REPACKAGED_IDEA_DIR=../idea-code-formatter/target/repackaged

echo unzipping minimal idea lib and plugin files
mkdir -p $IDEA_DIR
while IFS="" read -r jar || [ -n "$jar" ]
do
  unzip -o $DOWNLOAD_FILE "$jar" -d $IDEA_DIR
done < src/assembly/minimal-jars.txt

# unzip the jars for the assembly plugin
mkdir -p target/3rd-party-rt
unzip -o -q "$IDEA_DIR/lib/3rd-party-rt.jar" -d target/3rd-party-rt
mkdir -p target/util
unzip -o -q "$IDEA_DIR/lib/util.jar" -d target/util
mkdir -p target/util-8
unzip -o -q "$IDEA_DIR/lib/util-8.jar" -d target/util-8
mkdir -p target/util_rt
unzip -o -q "$IDEA_DIR/lib/util_rt.jar" -d target/util_rt
mkdir -p target/app
unzip -o -q "$IDEA_DIR/lib/app.jar" -d target/app
mkdir -p target/java-impl
unzip -o -q "$IDEA_DIR/plugins/java/lib/java-impl.jar" -d target/java-impl
mkdir -p target/jps-builders
unzip -o -q "$IDEA_DIR/plugins/java/lib/jps-builders.jar" -d target/jps-builders

#copy the original jars to the IDEA_REDUCED_DIR
cp -r $IDEA_DIR $IDEA_REDUCED_DIR

rm $IDEA_REDUCED_DIR/lib/3rd-party-rt.jar
rm $IDEA_REDUCED_DIR/lib/util.jar
rm $IDEA_REDUCED_DIR/lib/util-8.jar
rm $IDEA_REDUCED_DIR/lib/util_rt.jar
rm $IDEA_REDUCED_DIR/lib/app.jar
rm $IDEA_REDUCED_DIR/plugins/java/lib/java-impl.jar
rm $IDEA_REDUCED_DIR/plugins/java/lib/jps-builders.jar
