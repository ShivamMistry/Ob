@echo off
java -cp example -XX:-UseSplitVerifier HelloWorld
echo.
java -cp bin;bcel.jar;. com.speed.ob.Obfuscate example\HelloWorld.class example\SpeedPaste.jar
echo.
java -cp example -XX:-UseSplitVerifier HelloWorld
pause