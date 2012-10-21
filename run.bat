@echo off
java -cp example;. HelloWorld
java -cp bin;bcel.jar;. com.speed.ob.Obfuscate example\HelloWorld.class
java -XX:-UseSplitVerifier -cp example;. HelloWorld
pause