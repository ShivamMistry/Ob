@echo off
del example\HelloWorld.class
del example\HelloWorld_bak.class
copy bin\HelloWorld.class example\HelloWorld.class
java -cp example -XX:-UseSplitVerifier HelloWorld
echo.
java -cp bin;bcel.jar;. com.speed.ob.Obfuscate example\HelloWorld.class example\SpeedPaste.jar
echo.
java -cp example -XX:-UseSplitVerifier HelloWorld
pause