@echo off
java -cp bin;bcel.jar;. com.speed.ob.Obfuscate example\HelloWorld.class
echo '  '
java -cp example -XX:-UseSplitVerifier HelloWorld
pause