#!/bin/bash

DOWNLOAD_FILE=../idea-code-formatter-lib/target/idea.original.zip

unzip -jo $DOWNLOAD_FILE bin/idea.bat
grep -oP "\--add-opens=\S+" idea.bat > ../idea-code-formatter/src/main/resources/add-opens.txt
rm idea.bat