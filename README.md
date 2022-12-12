# APKEditor
### Powerful android apk resources editor
### Using [ARSCLib](https://github.com/REAndroid/ARSCLib)
* 100% java
* Independent of AAPT/AAPT2
* Can be used for obfuscation or de-obfuscation resources
* Fast

#### Decompile
```
java -jar APKEditor.jar d -split -f -i test.apk -o test_json
00.000 I: [DECOMPILE] Decompiling ...
 Input: test.apk
Output: test_json
 Force: true
 Split: true
 ---------------------------- 
00.036 I: [DECOMPILE] Loading ...
00.129 I: [DECOMPILE] Decompiling to json ...
30.093 I: [DECOMPILE] Done
```
### Build
```
java -jar APKEditor.jar b -f -i test_json -o test_edited.apk

00.000 I: [BUILD] Building ...
 Input: test_json/base
Output: test_edited.apk
 Force: true
 ---------------------------- 
00.048 I: [BUILD] Scanning directory ...
00.247 I: [BUILD] Writing apk...
22.032 [BUILD] Writing: method=STORED total=284921526 bytes : resources.arsc              
56.217 I: [BUILD] Done
```
* ### Building Jar
```console
git clone https://github.com/REAndroid/APKEditor.git
cd APKEditor
# Linux / Mac
./gradlew fatJar
# Windows
gradlew.bat fatJar
# Executable jar will be built under build/libs/APKEditor-{version}.jar
```
## Downloads
* ### [Latest release](https://github.com/REAndroid/APKEditor/releases/latest)