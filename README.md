# APKEditor
## Android apk editor (suitable for resource obfuscated apks)
## Using [ARSCLib](https://github.com/REAndroid/ARSCLib)
* ## Decompile

```console
java -jar APKEditor-1.0.1.jar d -split -f -i test.apk -o test_json
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
* ## Build

```console
java -jar APKEditor-1.0.1.jar b -f -i test_json -o test_edited.apk

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