#!/bin/bash
DOWNLOAD_FILE=target/idea.original.zip
IDEA_DIR=target/idea
REPACKAGED_IDEA_DIR=../idea-code-formatter/target/repackaged

# extract all idea lib jars and all jars of following plugins: java | java-ide-customization
echo unzipping idea plugin files
mkdir -p $IDEA_DIR
echo extracting all jars from java plugin
unzip -o $DOWNLOAD_FILE "plugins/java/**/*.jar" -d $IDEA_DIR
echo extracting all jars from java-ide-customization plugin
unzip -o $DOWNLOAD_FILE "plugins/java-ide-customization/**/*.jar" -d $IDEA_DIR
echo extracting all idea lib files
unzip -o $DOWNLOAD_FILE "lib/*.jar" -d $IDEA_DIR

mkdir -p $REPACKAGED_IDEA_DIR
pushd target
cp *.jar idea/lib #include also idea-code-formatter-lib
zip -r idea.zip idea
popd
pwd
mv target/idea.zip $REPACKAGED_IDEA_DIR