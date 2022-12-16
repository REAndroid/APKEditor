# APKEditor
### Powerful android apk resources editor
### Using [ARSCLib](https://github.com/REAndroid/ARSCLib)
* 100% java
* Independent of AAPT/AAPT2
* Can be used for obfuscation or de-obfuscation resources
* Fast

#### 1- Decompile
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
### 2- Build
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
### 3- Merge
```
java -jar APKEditor-1.0.5.jar m -i "apk_files"
00.049 I: [MERGE] Merging ...
   Input: apk_files
 Output: apk_files_merged.apk
 ---------------------------- 
00.050 I: [MERGE] Searching apk files ...
00.060 I: [MERGE] Found apk files: 3           
00.192 I: [MERGE] Found modules: 3
00.302 I: [MERGE] Merging: base
00.307 I: [MERGE] Added [base] classes.dex -> classes.dex
00.308 I: [MERGE] Merging resource table: base
01.302 I: [MERGE] Merging: config.xxhdpi-1
01.304 I: [MERGE] Merging resource table: config.xxhdpi-1
01.386 [MERGE] tum_ic_visibility_white_24.png
01.386 I: [MERGE] Merging: config.arm64_v8a-1
01.390 [MERGE] : lib/arm64-v8a/libnativeai.so

01.475 I: [MERGE] Sanitizing manifest ...
01.478 I: [MERGE] Removed: extractNativeLibs
01.480 I: [MERGE] Removed: isSplitRequired

01.480 I: [MERGE] Writing apk...
03.686 [MERGE] Writing: total=47693672 bytes : resources.arsc
         
03.768 I: [MERGE] Saved to: apk_files_merged.apk
03.768 I: [MERGE] Done
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