# APKEditor
## Android apk editor (suitable for resource obfuscated apks)

* ## Decompile

```console
java -jar APKEditor-1.0.0.jar d -i facebook.apk
00.000 I: [DECOMPILE] Decompiling ...
Input: facebook.apk
Output: facebook_out
Split: false
00.016 I: [DECOMPILE] Loading ...
00.075 I: [DECOMPILE] Decompiling to json ...
02.998 I: [DECOMPILE] Done

```
* ## Build

```console
java -jar APKEditor-1.0.0.jar b -i facebook_out
00.000 I: [BUILD] Building ...
Input: facebook_out/base
Output: facebook_out_out.apk
00.021 I: [BUILD] Scanning directory ...
00.086 I: [BUILD] Writing apk...
06.623 I: [BUILD] Done
```