#!/bin/bash
if [[ $# -eq 0 ]] ; then
    echo usage: $0 logdir
    exit 1
fi
LOG_DIR=$1
cat $LOG_DIR/idea_loaded_classes_and_resources.txt | cut -f2 -d "|" | sed 's|.*/idea/||' | sort -u > ../minimal-jars.txt