cd /d %~dp0

:top

java -Dfile.encoding=UTF-8 -jar ss_service.jar C:\workspace\SeraphySearcherService\data\win\config_win.txt debug

timeout 120

goto top
