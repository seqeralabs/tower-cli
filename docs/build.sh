#!/usr/bin/env bash

cd ..
./gradlew asciidoctor
mkdir -p build/docs
cd build/generated-picocli-docs-xml
ls *.xml | awk '{system("pandoc -f docbook -t markdown_strict "$1" -o ../docs/"$1".md")}'
cd ../docs
ls *.md | awk '{system("sed -i 's/\.xml/\.xml\.md/g' "$1)}'
cd ..
cp -r ../docs/* .
