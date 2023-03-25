#!/bin/bash

VERSION=$1
DOWNLOAD_DIR=$(pwd)/downloaded_ides
DEST_ZIP=repackaged/ide.zip
DEST_DIR=idea-code-formatter/downloaded_jars
if test -f "$DEST_DIR/$DEST_ZIP"; then
    echo "$DEST_DIR/$DEST_ZIP already exists. Skipping ide.zip creation."
    exit 0
fi

mkdir -p $DEST_DIR
cd $DEST_DIR

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


echo unzipping required files
unzip -o $DOWNLOAD_FILE "plugins/java/lib/*" -d ide
unzip -o $DOWNLOAD_FILE "plugins/java-ide-customization/lib/*" -d ide

echo repackaging ide
mkdir -p repackaged
zip -r $DEST_ZIP ide

unzip -o $DOWNLOAD_FILE "lib/3rd-party-rt.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/app.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/external-system-rt.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/forms_rt.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/groovy.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/jps-model.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/jsp-base.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/protobuf.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/rd.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/stats.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/util.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/util_rt.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/xml-dom.jar" -d ide
unzip -o $DOWNLOAD_FILE "lib/xml-dom-impl.jar" -d ide

echo do the cleanup
rm $DOWNLOAD_FILE
cd ..
