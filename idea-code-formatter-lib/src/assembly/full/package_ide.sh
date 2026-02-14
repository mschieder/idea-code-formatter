#!/bin/bash
DOWNLOAD_FILE=target/idea.original.zip
IDEA_DIR=target/idea
REPACKAGED_IDEA_DIR=../idea-code-formatter/target/repackaged

# extract all idea lib jars and all jars of following plugins: java | java-ide-customization
mkdir -p $IDEA_DIR

echo unzipping lib jars
unzip -o $DOWNLOAD_FILE "lib/*.jar" -d $IDEA_DIR
unzip -o $DOWNLOAD_FILE "lib/*/*.jar" -d $IDEA_DIR

echo unzipping idea plugin files
echo extracting all required plugins
while IFS="" read -r plugin || [ -n "$plugin" ]
do
  jarfiles=$(echo "plugins/$plugin/*/*.jar"|tr -d '\n'|tr -d '\r')
  echo extracting plugin dir $plugin
  unzip -o $DOWNLOAD_FILE $jarfiles -d $IDEA_DIR
done < src/assembly/plugin_dirs.txt

mkdir -p $REPACKAGED_IDEA_DIR
pushd target
cp *.jar idea/lib #include also idea-code-formatter-lib
#zip -rq idea.zip idea
7z a -r idea.zip idea
popd
mv target/idea.zip $REPACKAGED_IDEA_DIR