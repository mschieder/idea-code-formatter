#!/bin/bash

VERSION=$1
DOWNLOAD_DIR=$(pwd)/downloaded_ides
DEST_DIR=idea-code-formatter-lib/target/idea

mkdir -p $DEST_DIR

echo downloading IntelliJ IDEA CE $VERSION
DOWNLOAD_FILE=ideaIC-$VERSION.win.zip
if test -f "$DOWNLOAD_DIR/$DOWNLOAD_FILE"; then
    echo "download already exists. copying $DOWNLOAD_DIR/$DOWNLOAD_FILE"
    cp $DOWNLOAD_DIR/$DOWNLOAD_FILE $DOWNLOAD_FILE
else
     mkdir $DOWNLOAD_DIR
     wget --progress=bar:force:noscroll https://download-cdn.jetbrains.com/idea/$DOWNLOAD_FILE
     cp $DOWNLOAD_FILE $DOWNLOAD_DIR/$DOWNLOAD_FILE
fi


echo unzipping idea plugin files
unzip -o $DOWNLOAD_FILE "plugins/java/lib/*" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "plugins/java-ide-customization/lib/*" -d $DEST_DIR

echo unzipping idea lib files
unzip -o $DOWNLOAD_FILE "lib/3rd-party-rt.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/app.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/external-system-rt.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/forms_rt.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/groovy.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/jps-model.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/protobuf.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/rd.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/stats.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/util.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/util_rt.jar" -d $DEST_DIR
unzip -o $DOWNLOAD_FILE "lib/util-8.jar" -d $DEST_DIR

echo do the cleanup
rm $DOWNLOAD_FILE
