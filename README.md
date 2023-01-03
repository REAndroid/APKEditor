<details><summary> 👈 <code><i> Click arrows to expand/collapse details on this page </i></code></summary></details>

# APKEditor
### Powerful android apk resources editor
This tool uses [ARSCLib](https://github.com/REAndroid/ARSCLib) to edit any apk resources and has five main features

<details><summary><code>java -jar APKEditor.jar <b>-h</b></code></summary>

```ShellSession
$ java -jar APKEditor.jar -h
APKEditor - x.x.x
Using: ARSCLib-x.x.x
https://github.com/REAndroid/APKEditor
Android binary resource files editor
Usage: 
 java -jar APKEditor.jar <command> <args>
 commands: 
  1)  d | decode     -   Decodes android resources binary to readable json
  2)  b | build      -   Builds android binary from json
  3)  m | merge      -   Merges split apk files from directory
  4)  x | refactor   -   Refactors obfuscated resource names
  5)  p | protect    -   Protects/Obfuscates apk resource
 run with <command> -h to get detailed help about each command
 
```
</details>

#### 1- Decompile
* Decompiles resources of apk to human readable json string.
* Decompiles resources of apk to XML source code (for un-obfuscated apk only). Use  ``` -t xml ```
<details> <summary><code>java -jar APKEditor.jar <b>d</b> -i path/to/your-file.apk</code></summary>

```ShellSession
$ java -jar APKEditor.jar d -i test.apk -o test_json
00.000 I: [DECOMPILE] Decompiling ...
 Input: test.apk
Output: test_json
 ---------------------------- 
00.036 I: [DECOMPILE] Loading ...
00.129 I: [DECOMPILE] Decompiling to json ...
30.093 I: [DECOMPILE] Done
```

</details>

#### 2- Build
Builds back to apk from decompiled json/XML files
<details> <summary><code>java -jar APKEditor.jar <b>b</b> -i path/to/decompiled-directory</code></summary>

```ShellSession
$ java -jar APKEditor.jar b -i test_json -o test_edited.apk

00.000 I: [BUILD] Building ...
 Input: test_json/base
Output: test_edited.apk
 ---------------------------- 
00.048 I: [BUILD] Scanning directory ...
00.247 I: [BUILD] Writing apk...
22.032 [BUILD] Writing: method=STORED total=284921526 bytes : resources.arsc  
25.009 I: [BUILD] Zip align ...
27.101 I: [BUILD] Saved to: test_edited.apk
30.217 I: [BUILD] Done
```
</details>

#### 3- Merge
Merges multiple splitted apk (app bundles) to standalone apk
<details> <summary><code>java -jar APKEditor.jar <b>m</b> -i path/to/directory-of-apk-files</code></summary>

 ```ShellSession
$ java -jar APKEditor.jar m -i apk_files
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
03.729 I: [MERGE] Zip align ... 
04.611 I: [MERGE] Saved to: apk_files_merged.apk
04.700 I: [MERGE] Done

```  
![apkmerger](/.github/apkmerger.png)

</details>

#### 4- Refactor
Refactors obfuscated resource entry names
<details> <summary><code>java -jar APKEditor.jar <b>x</b> -i path/to/input.apk</code></summary>

 ```ShellSession
$ java -jar APKEditor.jar x -i input.apk
00.000 I: [REFACTOR] Refactoring ...
   Input: input.apk
 Output: input_refactored.apk
 ---------------------------- 
00.017 I: [REFACTOR] Loading apk: input.apk
00.952 I: [REFACTOR] Renamed entries: 5888
00.954 I: [REFACTOR] Writing apk ...
03.268 [REFACTOR] Writing: total=47589184 bytes : resources.arsc              
03.350 I: [REFACTOR] Zip align ...
03.504 I: [REFACTOR] Saved to: input_refactored.apk
03.504 I: [REFACTOR] Done

```  

</details>

#### 5- Protect  (⭐NEW⭐)
Protects apk resources against almost all known decompile/modify tools.
<details> <summary><code>java -jar APKEditor.jar <b>p</b> -i path/to/input.apk</code></summary>

 ```ShellSession
00.026 I: [PROTECT] Protecting ...
   Input: test.apk
 Output: test_protected.apk
 ---------------------------- 
00.027 I: [PROTECT] Loading apk file ...
00.052 I: [PROTECT] Protecting files ..
00.454 I: [PROTECT] Protecting resource table ..
00.474 I: [PROTECT] Writing apk ...
02.264 [PROTECT] Writing: total=47654392 bytes : resources.arsc              
02.346 I: [PROTECT] Zip align ...
02.451 I: [PROTECT] Saved to: test_protected.apk
02.451 I: [PROTECT] Done

```  

</details>
 
---

***Build executable jar***
<details> <summary> <code> ./gradlew fatJar </code> </summary>

 ```ShellSession
 
# NB: Due to my lazyness , the dependency ARSCLib.jar is pre-built and placed under APKEditor/libs/ARSCLib.jar or you can build yourself and replace it.
git clone https://github.com/REAndroid/APKEditor
cd APKEditor
./gradlew -fatJar
# Executable jar will be placed ./build/libs/APKEditor-x.x.x.jar

 ```
 </details>

***Downloads***
* [Latest release with pre-built executable jar](https://github.com/REAndroid/APKEditor/releases/latest)

***Contribute***
* Everyone is so welcome in this project, if you have some code improvements please make a pull request
* Please share your ideas / thoughts in [discussions](https://github.com/REAndroid/APKEditor/discussions)
* Please create issue you faced while using this tool along with your apk



<details> <summary><i><b>Contact</b></i></summary> 

* Telegram: [@kikfox](https://t.me/kikfox)
* Email: [thekikfox@gmail.com](mailto:thekikfox@gmail.com)

</details>
