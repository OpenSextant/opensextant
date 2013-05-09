@echo off
echo Detecting OS processor type

if "%PROCESSOR_ARCHITECTURE%"=="AMD64" goto 64BIT
echo 32-bit OS
%~dp0../java/j32/bin/java.exe -jar ../dist/OpenSextant.jar

goto END
:64BIT
echo 64-bit OS
%~dp0../java/j64/bin/java.exe -jar ../dist/OpenSextant.jar
:END

