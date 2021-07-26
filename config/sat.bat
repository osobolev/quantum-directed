@echo off
call setjava.bat
%JAVABIN% -cp graph-directed.jar directed.events.SatStat %*
