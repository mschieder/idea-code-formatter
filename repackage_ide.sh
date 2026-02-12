#!/bin/bash

VERSION=$1
DOWNLOAD_DIR=$(pwd)/downloaded_ides
DEST_DIR=idea-code-formatter-lib/target/idea

mkdir -p $DEST_DIR

echo downloading IntelliJ IDEA CE $VERSION
DOWNLOAD_FILE=ideaIC-$VERSION.win.zip
DOWNLOAD_FILE2=idea-code-formatter-lib/target/idea.original.zip

if test -f "$DOWNLOAD_DIR/$DOWNLOAD_FILE"; then
    echo "download already exists. copying $DOWNLOAD_DIR/$DOWNLOAD_FILE"

else
     mkdir $DOWNLOAD_DIR
     curl --output $DOWNLOAD_DIR/$DOWNLOAD_FILE https://download-cdn.jetbrains.com/idea/$DOWNLOAD_FILE
fi
 cp $DOWNLOAD_DIR/$DOWNLOAD_FILE $DOWNLOAD_FILE2


echo unzipping idea plugin files

for jar in aether-dependency-resolver.jar java-impl.jar jps-builders-6.jar jps-builders.jar rt/protobuf-java6.jar;
  do unzip -o $DOWNLOAD_FILE2 "plugins/java/lib/${jar}" -d $DEST_DIR
done;

unzip -o $DOWNLOAD_FILE2 "plugins/java-ide-customization/lib/java-ide-customization.jar" -d $DEST_DIR

echo unzipping idea lib files
for jar in 3rd-party-rt.jar app.jar external-system-rt.jar forms_rt.jar jps-model.jar stats.jar util.jar util_rt.jar util-8.jar;
  do unzip -o $DOWNLOAD_FILE2 "lib/${jar}" -d $DEST_DIR
done;

echo do the cleanup
#rm $DOWNLOAD_FILE2
