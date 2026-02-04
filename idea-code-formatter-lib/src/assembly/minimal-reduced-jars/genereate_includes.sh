#!/bin/bash

if [[ $# -eq 0 ]] ; then
    echo usage: $0 logdir
    exit 1
fi
LOG_DIR=$1
INDENT="                "
while IFS="" read -r jar || [ -n "$jar" ]
do
  jarfilename=$(basename $jar)
  echo "$INDENT<!-- generated with $0 - DO NOT EDIT - -->" > includes.xml
  grep $jarfilename $LOG_DIR/idea_loaded_classes_and_resources.txt | cut -f1 -d " " |  sort -u | sed "s/^/$INDENT<include>/g" | sed "s/$/<\/include>/g" >> includes.xml
  sed -i -ne "/<!-- BEGIN generated includes -->/ {p; r includes.xml" -e ":a; n; /<!-- END generated includes -->/ {p; b}; ba}; p" $jarfilename-assembly.xml
  rm includes.xml
done < ../minimal-jars.txt