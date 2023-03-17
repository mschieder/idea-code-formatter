#!/bin/bash

VERSION=$1
DEST_ZIP=generated-resources/ide.zip
if test -f "target/$DEST_ZIP"; then
    echo "target/$DEST_ZIP already exists. Skipping ide.zip creation."
    exit 0
fi

mkdir target
cd target

echo downloading IntelliJ IDEA CE $VERSION
DOWNLOAD_FILE=ideaIC-$VERSION.win.zip
wget --progress=bar:force:noscroll https://download-cdn.jetbrains.com/idea/$DOWNLOAD_FILE

echo unzipping required files
unzip -o $DOWNLOAD_FILE "plugins/java/lib/*" -d ide
unzip -o $DOWNLOAD_FILE "plugins/java-ide-customization/lib/*" -d ide
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

echo repackaging ide
mkdir -p generated-resources
zip -r $DEST_ZIP ide

echo do the cleanup
rm $DOWNLOAD_FILE
rm -rf ide
cd ..
