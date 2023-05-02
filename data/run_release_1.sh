#!/bin/bash

cd ~

# 二重起動防止

[[ $$ != `pgrep -fo "$0"`  ]] && [[ $PPID != `pgrep -fo "$0"`  ]] && echo "$0 is already running" && exit 2 

java -Dfile.encoding=UTF-8 -jar ss_service.jar ~/linux/config_linux.txt release

