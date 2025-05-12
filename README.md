<details><summary> 👈 <code><i> Click arrows to expand/collapse details on this page </i></code></summary></details>

# APKEditor
### Powerful android apk resources editor
This tool uses [ARSCLib](https://github.com/REAndroid/ARSCLib) to edit any apk resources and has six main features

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
  3)  m | merge      -   Merges split apk files from directory or XAPK, APKM, APKS ...
  4)  x | refactor   -   Refactors obfuscated resource names
  5)  p | protect    -   Protects/Obfuscates apk resource
  6)  info           -   Prints information of apk
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
<details> <summary><code>more info</code></summary>

```ShellSession
Decodes android resources binary to readable json/xml/raw.
Usage:
  d [Options, flags]
Options:
  -framework            Path of framework file (can be multiple).
  -framework-version    Preferred framework version number
  -i                    Input path.
  -load-dex             Number of dex files to load at a time.
                        If the apk dex files count greater than this value,
                        then the decoder loads one dex at a time.
                          *Applies only when -dex-lib set to internal.
                          *Default = 3
                          *See<Notes> below.
  -o                    Output path. Optional, if not provided then a new file
                        will be generated on same directory as input
  -res-dir              Sets resource files root dir name. e.g. for obfuscation
                        to move files from 'res/*' to 'r/*' or vice versa.
  -sig                  Signatures directory path.
  -t                    Decode types:
                        [xml, json, raw, sig]
  -dex-lib              Dex library to use:
                         1) internal : Use internal library, supports dex
                        versions up to 042.
                         2) jf : Use library by JesusFreke/smali, supports dex
                        versions 035 and below.
                          *Default = jf
                          **WARN: The default value will be replaced by
                        "internal" on coming versions.
                          *See <Notes> below.
                        [internal, jf]
Flags:
  -dex                  Copy raw dex files / skip smali.
  -dex-markers          Dumps dex markers (applies only when smali mode).
  -f                    Force delete output path.
  -h | -help | --help   Displays this help and exit.
  -keep-res-path        Keeps original res/* file paths:
                          *Applies only when decoding to xml
                          *All res/* files will be placed on dir <res-files>
                          *The relative paths will be linked to values/*xml
  -no-dex-debug         Drops all debug info from smali/dex.
  -split-json           Splits resources.arsc into multiple parts as per type
                        entries (use this for large files)
  -vrd                  Validate resources dir name
                        (eg. if a drawable resource file path is 'res/abc.png'
                        then it will be moved to 'res/drawable/abc.png)'
Examples:
  1)  [Basic]
  java -jar APKEditor.jar d -i path/input.apk
  2)  [Specify output]
  java -jar APKEditor.jar d -i path/input.apk -o path/output.apk
  3)  [Specify decode type]
  java -jar APKEditor.jar d -t xml -i path/input.apk
  4)  [Specify framework file(s)]
  java -jar APKEditor.jar d -i path/input.apk -framework framework-res.apk
  -framework platforms/android-32/android.jar
  5)  [Decode apk signature block]
  java -jar APKEditor.jar d -t sig -i path/input.apk -sig path/signatures_dir
Notes:
  1)  [internal] Dex builder:
  * Fully supports dex files up to 042.
  * Highest dex file compression.
  * Builds with similar dex-section order as r8/dx.
  * Convenient dex markers editing, see file smali/classes/dex-file.json
  * Additional helpful smali comments: e.g class/method hierarchy.
  * Supports whitespaces on class simple name as introduced on dex 041+
  2)  [-load-dex] To print correct class/method hierarchy, it is necessary to
  load all dex files at once. This may result high memory consumption and
  could fail with "OutOfMemoryError" thus you are required to limit the
  number of dex files to load at a time. You can overcome this problem with
  -Xmx memory arg e.g java -Xmx8g -jar APKEditor.jar ...
```
</details>
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
<details> <summary><code>more info</code></summary>

```ShellSession
Builds android binary from json/xml/raw.
Options:
  -framework            Path of framework file (can be multiple).
  -framework-version    Preferred framework version number
  -i                    Input path.
  -o                    Output path. Optional, if not provided then a new file
                        will be generated on same directory as input
  -res-dir              Sets resource files root dir name. e.g. for obfuscation
                        to move files from 'res/*' to 'r/*' or vice versa.
  -sig                  Signatures directory path.
  -t                    Build types, By default build types determined by quick
                        scanning of input directory files. Values are:
                        [xml, json, raw, sig]
  -extractNativeLibs    Sets extractNativeLibs attribute on manifest and
                        applies compression of library files (*.so).
                          *Default = manifest
                          1) manifest: read and apply from manifest.
                          2) none: remove attribute from manifest and store
                        libraries compressed.
                          3) true: set manifest attribute 'true' and store
                        libraries compressed.
                          4) false: set manifest attribute 'false' and store
                        libraries un-compressed with 4096 alignment.
                        [manifest, none, false, true]
  -dex-lib              Dex library to use:
                         1) internal : Use internal library, supports dex
                        versions up to 042.
                         2) jf : Use library by JesusFreke/smali, supports dex
                        versions 035 and below.
                          *Default = jf
                          **WARN: The default value will be replaced by
                        "internal" on coming versions.
                          *See <Notes> below.
                        [internal, jf]
Flags:
  -f                    Force delete output path.
  -h | -help | --help   Displays this help and exit.
  -no-cache             Ignore built cached .dex files and re-build smali files.
  -vrd                  Validate resources dir name
                        (eg. if a drawable resource file path is 'res/abc.png'
                        then it will be moved to 'res/drawable/abc.png)'
Examples:
  1)  [Basic]
    java -jar APKEditor.jar b -i path/input_directory
  2)  [Specify output]
    java -jar APKEditor.jar b -i path/input_directory -o path/output.apk
  3)  [Restore signatures]
    java -jar APKEditor.jar b -t sig -i path/input.apk -sig
  path/signatures_dir
  4)  [Specify framework]
    java -jar APKEditor.jar b -i path/input_directory -framework
  framework-res.apk -framework platforms/android-32/android.jar
```
</details>
</details>

#### 3- Merge
Merges multiple splitted apk files (directory, xapk, apkm, apks ...) to standalone apk
<details> <summary><code>java -jar APKEditor.jar <b>m</b> -i path/to/input</code></summary>

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

<details> <summary><code>more info</code></summary>

```ShellSession
Merges split apk files from directory or compressed apk files like XAPK,
APKM, APKS ...
Options:
  -i                    Input path.
  -o                    Output path. Optional, if not provided then a new file
                        will be generated on same directory as input
  -res-dir              Sets resource files root dir name. e.g. for obfuscation
                        to move files from 'res/*' to 'r/*' or vice versa.
  -extractNativeLibs    Sets extractNativeLibs attribute on manifest and
                        applies compression of library files (*.so).
                          *Default = manifest
                          1) manifest: read and apply from manifest.
                          2) none: remove attribute from manifest and store
                        libraries compressed.
                          3) true: set manifest attribute 'true' and store
                        libraries compressed.
                          4) false: set manifest attribute 'false' and store
                        libraries un-compressed with 4096 alignment.
                        [manifest, none, false, true]
Flags:
  -clean-meta           Cleans META-INF directory along with signature block.
  -f                    Force delete output path.
  -h | -help | --help   Displays this help and exit.
  -validate-modules     Validates for same versionNumber of base.apk with split
                        apk files.
  -vrd                  Validate resources dir name
                        (eg. if a drawable resource file path is 'res/abc.png'
                        then it will be moved to 'res/drawable/abc.png)'
Examples:
  1)  [Basic]
    java -jar APKEditor.jar m -i path/input -o path/output.apk
```
</details>
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
<details> <summary><code>more info</code></summary>

```ShellSession
Refactors obfuscated resource names
Options:
  -i                    Input path.
  -o                    Output path. Optional, if not provided then a new file
                        will be generated on same directory as input
  -public-xml           Path of resource ids xml file (public.xml)
                        Loads names and applies to resources from 'public.xml'
                        file
Flags:
  -clean-meta           Cleans META-INF directory along with signature block.
  -f                    Force delete output path.
  -fix-types            Corrects resource type names based on usages and values
  -h | -help | --help   Displays this help and exit.
Examples:
  1)  [Basic]
    java -jar APKEditor.jar x -i path/input.apk -o path/output.apk
```
</details>
</details>

#### 5- Protect
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
<details> <summary><code>more info</code></summary>

```ShellSession
Protects/Obfuscates apk resource files. Using unique obfuscation techniques.
Options:
  -i                    Input path.
  -keep-type            Keep specific resource type names (e.g drawable), By
                        default keeps only <font> resource type.
                         *Can be multiple
  -o                    Output path. Optional, if not provided then a new file
                        will be generated on same directory as input
Flags:
  -confuse-zip          Confuse zip structure. When this turned on:
                         * Apps might crash if accessing apk files directly e.g
                        Class.getResourceAsStream().
                         * Some apk scanners might flag it as "Malformed zip"
  -dic-dir-names        Path to a text file containing a list of directory
                        names separated by new line.
  -dic-file-names       Path to a text file containing a list of file names
                        separated by new line.
  -f                    Force delete output path.
  -h | -help | --help   Displays this help and exit.
  -skip-manifest        Do not protect manifest.
Examples:
  1)  [Basic]
    java -jar APKEditor.jar p -i path/input.apk -o path/output.apk
```
</details>
</details>

#### 6- Info  (⭐NEW⭐)
Prints/dumps from basic up to detailed information of apk.
<details> <summary><code>java -jar APKEditor.jar <b>info</b> -v -resources -i input.apk </code></summary>

 ```ShellSession
Package name=com.mypackage id=0x7f
  type string id=1 entryCount=1
    resource 0x7f010000 string/app_name
      () My Application
      (-de) Meine Bewerbung
      (-ru-rRU) Мое заявление
  type mipmap id=2 entryCount=1
    resource 0x7f020000 mipmap/ic_launcher_round
      () res/mipmap/ic_launcher_round.png
  type drawable id=3 entryCount=1
    resource 0x7f030000 drawable/ic_launcher
      () #006400

```  
<details> <summary><code>more info</code></summary>

```ShellSession
Prints information of apk.
Options:
  -filter-type          Prints only the specified resource type names
                          *This applies only when flag '-resources' used.
                          *Can be multiple.
  -framework            Path of framework file (can be multiple).
  -framework-version    Preferred framework version number
  -i                    Input path.
  -o                    Output path. Optional, if not provided then a new file
                        will be generated on same directory as input
  -res                  Prints resource entries specified by either of:
                          1) Hex or decimal resource id.
                          2) Full resource name e.g @string/app_name.
                         *Can be multiple.
  -xmlstrings           Print the strings of the given compiled xml assets.
                         *Can be multiple
  -xmltree              Prints the compiled xmls in the given assets.
                         *Can be multiple
  -t                    Print types/formats:
                        [text, json, xml]
Flags:
  -activities           Prints main activity class name. If verbose mode,
                        prints all declared activities including
                        <activity-alias>.
  -app-class            Application class name.
  -app-icon             App icon path/value. If verbose mode, prints all
                        configurations.
  -app-name             App name. If verbose mode, prints all configurations.
  -app-round-icon       App round icon path/value. If verbose mode, prints all
                        configurations.
  -configurations       Print the configurations in the APK.
  -dex                  Prints dex information.
  -f                    Force delete output path.
  -h | -help | --help   Displays this help and exit.
  -languages            Print the languages in the APK.
  -list-files           List files inside apk.
  -list-xml-files       List compiled xml files inside apk.
  -locales              Print the locales in the APK.
  -min-sdk-version      Minimum SDK version.
  -package              Package name(application id) from manifest and if
                        verbose mode, prints resource table packages.
  -permissions          Permissions.
  -resources            Prints all resources
  -signatures           Prints signature information.
  -signatures-base64    Prints signature information with base64 certificates.
  -strings              Print the contents of the resource table string pool in
                        the APK.
  -target-sdk-version   Target SDK version.
  -v                    Verbose mode.
  -version-code         App version code.
  -version-name         App version name.
Examples:
  1)  [Basic]
    java -jar APKEditor.jar info -i file.apk
  2)  [Specify output and type]
    java -jar APKEditor.jar info -i path/input.apk -t json -v -o
  info_file.json
  3)  [Print only specific type]
    java -jar APKEditor.jar info -i path/input.apk -resources -filter-type
  drawable
```
</details>
</details>


---

***Build executable jar***
<details> <summary> <code> ./gradlew fatJar </code> </summary>

 ```ShellSession
 
# NB: Due to my lazyness , the dependency ARSCLib.jar is pre-built and placed under APKEditor/libs/ARSCLib.jar or you can build yourself and replace it.
git clone https://github.com/REAndroid/APKEditor
cd APKEditor
./gradlew fatJar
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
