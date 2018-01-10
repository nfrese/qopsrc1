#!/bin/bash

find . -type f -name \*.shp | sed 's/\.[^.]*$//' > tmp-list-f1.txt
cat tmp-list-f1.txt | sed 's:.*/::' | tr '[:upper:]' '[:lower:]' > tmp-list-f2.txt
paste -d" " tmp-list-f1.txt tmp-list-f2.txt | awk '{print "$TOSQLCMD " $0 " | $STORECMD"}'

