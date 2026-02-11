#!/bin/bash

if [[ $# -eq 0 ]] ; then
    echo usage: $0 logdir
    exit 1
fi
LOG_DIR=$1

while IFS="" read -r jar || [ -n "$jar" ]
do
  jarfilename=$(basename $jar)
  jar_base_filename=$(basename $jar .jar)
  jar_dirname=$(dirname $jar)

  if [ ! -f $jarfilename-assembly.xml ]; then
        echo """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<assembly xmlns=\"http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2\"
          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
          xsi:schemaLocation=\"http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2 http://maven.apache.org/xsd/assembly-1.1.2.xsd\">

    <id>reduced</id>
    <formats>
        <format>jar</format>
    </formats>

    <includeBaseDirectory>false</includeBaseDirectory>
    <fileSets>
        <fileSet>
            <directory>target/$jar_base_filename</directory>
            <outputDirectory>/</outputDirectory>
            <includes>
                <!-- BEGIN generated includes -->
                <!-- END generated includes -->
            </includes>
        </fileSet>
    </fileSets>
</assembly>""" > $jarfilename-assembly.xml
  fi

  echo \
  """                            <execution>
                                <id>$jar_base_filename</id>
                                <phase>package</phase>
                                <goals>
                                    <goal>single</goal>
                                </goals>
                                <configuration>
                                    <descriptors>
                                        <descriptor>src/assembly/minimal-reduced-jars/$jarfilename-assembly.xml</descriptor>
                                    </descriptors>
                                    <finalName>$jar_base_filename</finalName>
                                    <outputDirectory>\${project.build.directory}/idea-reduced/$jar_dirname</outputDirectory>
                                </configuration>
                          </execution>""" >> executions.xml
done < ../minimal-jars.txt

sed -i -ne "/<!-- BEGIN generated executions -->/ {p; r executions.xml" -e ":a; n; /<!-- END generated executions -->/ {p; b}; ba}; p" ../../../pom.xml
rm executions.xml


INDENT="                "
while IFS="" read -r jar || [ -n "$jar" ]
do
  jarfilename=$(basename $jar)
  jar_base_filename=$(basename $jar .jar)


  echo "$INDENT<!-- generated with $0 - DO NOT EDIT - -->" > includes.xml
  grep $jarfilename $LOG_DIR/idea_loaded_classes_and_resources.txt | cut -f1 -d "|" |  sort -u | sed "s/^/$INDENT<include>/g" | sed "s/$/<\/include>/g" >> includes.xml
  sed -i -ne "/<!-- BEGIN generated includes -->/ {p; r includes.xml" -e ":a; n; /<!-- END generated includes -->/ {p; b}; ba}; p" $jarfilename-assembly.xml
  rm includes.xml
done < ../minimal-jars.txt
