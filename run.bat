@echo off
del example\HelloWorld.class
del example\HelloWorld_bak.class
copy bin\HelloWorld.class example\HelloWorld.class
echo Running HelloWorld without obfuscation
java -cp example -XX:-UseSplitVerifier HelloWorld
echo.
echo Running obfuscator
echo.
java -cp bin;resources\bcel.jar;. com.speed.ob.Obfuscate example\HelloWorld.class example\Test.jar
echo.
echo Running HelloWorld with obfuscation
echo.
java -cp example -XX:-UseSplitVerifier HelloWorld
pause