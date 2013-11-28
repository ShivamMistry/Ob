#!/bin/bash

rm example/HelloWorld.class
rm example/HelloWorld_bak.class
cp bin/HelloWorld.class example/HelloWorld.class
echo Running HelloWorld without obfuscation
java -cp example -XX:-UseSplitVerifier HelloWorld
echo
echo "Running obfuscator"
echo
java -cp bin:resources/bcel.jar:. com.speed.ob.Obfuscate example/HelloWorld.class example/Test.jar example/SpeedPaste.jar
echo
echo "Running HelloWorld with obfuscation"
echo
java -cp example -XX:-UseSplitVerifier HelloWorld
java -XX:-UseSplitVerifier -jar example/SpeedPaste-ob.jar