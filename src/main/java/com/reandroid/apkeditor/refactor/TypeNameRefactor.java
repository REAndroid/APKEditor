 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.apkeditor.refactor;

import com.reandroid.apk.APKLogger;
import com.reandroid.apk.ApkModule;
import com.reandroid.apk.ResFile;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.group.EntryGroup;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.util.FrameworkTable;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.arsc.value.array.ArrayBag;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.arsc.value.attribute.AttributeValueType;
import com.reandroid.arsc.value.plurals.PluralsBag;
import com.reandroid.common.Frameworks;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLElement;

import java.io.IOException;
import java.util.*;

public class TypeNameRefactor {
    private final ApkModule apkModule;
    private Map<Integer,TypeString> mTypeStrings;
    private APKLogger apkLogger;
    private final TypeNameMap refactoredTypeMap;
    public TypeNameRefactor(ApkModule apkModule){
        this.apkModule=apkModule;
        this.refactoredTypeMap =new TypeNameMap();
    }
    public void setApkLogger(APKLogger apkLogger) {
        this.apkLogger = apkLogger;
    }
    public void refactor() throws IOException {
        logMessage("Refactoring types ...");
        loadTypeStrings(apkModule.getTableBlock());
        logMessage("Refactoring from AndroidManifest ...");
        AndroidManifestBlock manifestBlock=apkModule.getAndroidManifestBlock();
        scanXml(manifestBlock, 0);
        scanResFiles();
        if(!isFinished()){
            scanTableEntries(apkModule.getTableBlock());
        }
        logFinished();
    }
    private void logFinished(){
        StringBuilder log=new StringBuilder();
        log.append("Finished type rename=");
        log.append(refactoredTypeMap.count());
        if(refactoredTypeMap.count()>0){
            log.append("\n");
            XMLElement element=refactoredTypeMap.toXMLDocument().getDocumentElement();
            element.setTagName("renamed");
            element.setAttribute("count", refactoredTypeMap.count());
            log.append(element.toText(2, false));
        }
        TypeNameMap remain=new TypeNameMap();
        for(Map.Entry<Integer, TypeString> entry:mTypeStrings.entrySet()){
            remain.add(entry.getKey(), entry.getValue().get());
        }
        if(remain.count()>0){
            log.append("\n");
            XMLDocument xmlDocument=remain.toXMLDocument();
            XMLElement element=xmlDocument.getDocumentElement();
            element.setTagName("remain");
            log.append(xmlDocument.toText(2, false));
        }
        logMessage(log.toString());
    }
    private void scanTableEntries(TableBlock tableBlock){
        logMessage("Refactoring from TableBlock ...");
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            if(isFinished()){
                break;
            }
            scanPackageEntries(packageBlock);
        }
    }
    private void scanPackageEntries(PackageBlock packageBlock){
        for(EntryGroup entryGroup:packageBlock.listEntryGroup()){
            if(isFinished()){
                break;
            }
            checkEntryGroup(entryGroup);
        }
    }
    private void checkEntryGroup(EntryGroup entryGroup){
        int resourceId=entryGroup.getResourceId();
        if(hasRefactoredId(resourceId)){
            return;
        }
        boolean renameOk = checkBag(entryGroup);
        if(renameOk){
            return;
        }
    }
    private boolean checkBag(EntryGroup entryGroup){
        if(!hasRefactoredName("style") || !hasRefactoredName("attr")){
            return false;
        }
        boolean hasBagEntry=false;
        Iterator<Entry> itr = entryGroup.iterator(true);
        while (itr.hasNext()){
            Entry entryBlock=itr.next();
            if(!entryBlock.isComplex()){
                return false;
            }
            hasBagEntry=true;
            ResTableMapEntry resValueBag=(ResTableMapEntry) entryBlock.getTableEntry();
            if(checkPlurals(resValueBag)){
                return true;
            }
            if(checkArray(resValueBag)){
                return true;
            }
        }
        return hasBagEntry;
    }
    private boolean checkArray(ResTableMapEntry resValueBag){
        String name="array";
        if(hasRefactoredName(name)){
            return false;
        }
        if(resValueBag.getValue().childesCount()<2){
            return false;
        }
        if(!ArrayBag.isArray(resValueBag)){
            return false;
        }
        int resourceId=resValueBag.getParentEntry().getResourceId();
        rename(resourceId, name);
        return true;
    }
    private boolean checkPlurals(ResTableMapEntry resValueBag){
        String name="plurals";
        if(hasRefactoredName(name)){
            return false;
        }
        if(resValueBag.getValue().childesCount()<2){
            return false;
        }
        if(!PluralsBag.isPlurals(resValueBag)){
            return false;
        }
        int resourceId=resValueBag.getParentEntry().getResourceId();
        rename(resourceId, name);
        return true;
    }
    private void scanResFiles() throws IOException {
        logMessage("Refactoring from resource files ...");
        for(ResFile resFile: apkModule.listResFiles()){
            if(isFinished()){
                break;
            }
            if(resFile.isBinaryXml()){
                ResXmlDocument resXmlDocument=new ResXmlDocument();
                resXmlDocument.readBytes(resFile.getInputSource().openStream());
                scanXml(resXmlDocument, resFile.pickOne().getResourceId());
            }
        }
    }
    private void loadTypeStrings(TableBlock tableBlock){
        mTypeStrings=new HashMap<>();
        for(PackageBlock packageBlock:tableBlock.listPackages()){
            int pkgId=packageBlock.getId();
            for(TypeString typeString:packageBlock.getTypeStringPool().listStrings()){
                int pkgTypeId = (pkgId<<24) | ((0xff & typeString.getId())<<16);
                mTypeStrings.put(pkgTypeId, typeString);
            }
        }
    }
    private void scanXml(ResXmlDocument xmlBlock, int resourceId){
        boolean isManifest=(xmlBlock instanceof AndroidManifestBlock);
        if(!isManifest && resourceId!=0 && !hasRefactoredId(resourceId)){
            boolean renameOk;
            renameOk = checkLayout(xmlBlock, resourceId);
            if(renameOk){
                return;
            }
            renameOk = checkDrawable(xmlBlock, resourceId);
            if(renameOk){
                return;
            }
            renameOk = checkAnimator(xmlBlock, resourceId);
            if(renameOk){
                return;
            }
            renameOk = checkMenu(xmlBlock, resourceId);
            if(renameOk){
                return;
            }
            renameOk = checkXml(xmlBlock, resourceId);
            if(renameOk){
                return;
            }
            renameOk = checkAnim(xmlBlock, resourceId);
            if(renameOk){
                return;
            }
            renameOk = checkInterpolator(xmlBlock, resourceId);
            if(renameOk){
                return;
            }
        }
        List<ResXmlAttribute> attributeList = listAttributes(xmlBlock.getResXmlElement());
        for(ResXmlAttribute attribute:attributeList){
            scanAttribute(attribute, isManifest);
        }
    }
    private void scanAttribute(ResXmlAttribute attribute, boolean isManifest){
        boolean renameOk;
        if(isManifest){
            renameOk = checkString(attribute);
            if(!renameOk){
                renameOk = checkStyle(attribute);
            }
            return;
        }
        renameOk = checkAttr(attribute);
        if(hasRefactoredId(attribute.getData())){
            return;
        }
        if(!renameOk){
            renameOk = checkId(attribute);
        }
        if(!renameOk){
            renameOk = checkDimen(attribute);
        }
        if(!renameOk){
            renameOk = checkInteger(attribute);
        }
        if(!renameOk){
            renameOk = checkColor(attribute);
        }
        if(!renameOk){
            renameOk = checkBool(attribute);
        }
    }
    private boolean checkInterpolator(ResXmlDocument resXmlDocument, int resourceId){
        String name="interpolator";
        if(hasRefactoredName(name)){
            return false;
        }
        ResXmlElement root=resXmlDocument.getResXmlElement();
        if(root==null){
            return false;
        }
        String tag=root.getTag();
        if(!"pathInterpolator".equals(tag) && !"linearInterpolator".equals(tag)){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkAnim(ResXmlDocument resXmlDocument, int resourceId){
        String name="anim";
        if(hasRefactoredName(name)){
            return false;
        }
        if(!hasRefactoredName("animator")){
            return false;
        }
        ResXmlElement root=resXmlDocument.getResXmlElement();
        if(root==null){
            return false;
        }
        if(!"alpha".equals(root.getTag())){
            return false;
        }
        int fromAlpha=0x010101ca;
        if(root.searchAttributeByResourceId(fromAlpha)==null){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkXml(ResXmlDocument resXmlDocument, int resourceId){
        String name="xml";
        if(hasRefactoredName(name)){
            return false;
        }
        ResXmlElement root=resXmlDocument.getResXmlElement();
        if(root==null){
            return false;
        }
        if(!isXml(root)){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkMenu(ResXmlDocument resXmlDocument, int resourceId){
        String name="menu";
        if(hasRefactoredName(name)){
            return false;
        }
        ResXmlElement root=resXmlDocument.getResXmlElement();
        if(root==null){
            return false;
        }
        if(!"menu".equals(root.getTag())){
            return false;
        }
        if(root.listElements("item").size()==0){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkAnimator(ResXmlDocument resXmlDocument, int resourceId){
        String name="animator";
        if(hasRefactoredName(name)){
            return false;
        }
        ResXmlElement root=resXmlDocument.getResXmlElement();
        if(root==null){
            return false;
        }
        if(!"selector".equals(root.getTag())){
            return false;
        }
        int state_enabled=0x0101009e;
        boolean hasObjectAnimator=false;
        for(ResXmlElement itemElement:root.listElements("item")){
            if(itemElement.searchAttributeByResourceId(state_enabled)==null){
                continue;
            }
            hasObjectAnimator=itemElement
                    .listElements("objectAnimator").size()>0;
            if(hasObjectAnimator){
                break;
            }
        }
        if(!hasObjectAnimator){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkDrawable(ResXmlDocument resXmlDocument, int resourceId){
        String name="drawable";
        if(hasRefactoredName(name)){
            return false;
        }
        ResXmlElement root=resXmlDocument.getResXmlElement();
        if(root==null){
            return false;
        }
        if(!"vector".equals(root.getTag())){
            return false;
        }
        int pathData=0x01010405;
        boolean hasPathData=false;
        for(ResXmlElement element:root.listElements("path")){
            if(element.searchAttributeByResourceId(pathData)!=null){
                hasPathData=true;
            }
        }
        if(!hasPathData){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkLayout(ResXmlDocument resXmlDocument, int resourceId){
        String name="layout";
        if(hasRefactoredName(name)){
            return false;
        }
        ResXmlElement root=resXmlDocument.getResXmlElement();
        if(root==null){
            return false;
        }
        if(!"LinearLayout".equals(root.getTag())){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkAttr(ResXmlAttribute attribute){
        String name="attr";
        if(hasRefactoredName(name)){
            return false;
        }
        return rename(attribute.getNameResourceID(), name);
    }
    private boolean checkColor(ResXmlAttribute attribute){
        String name="color";
        if(hasRefactoredName(name)){
            return false;
        }
        if(!hasRefactoredName("drawable")){
            return false;
        }
        int textColor=0x01010098;
        int nameId=attribute.getNameResourceID();
        if(nameId!=textColor){
            return false;
        }
        if(attribute.getValueType() != ValueType.REFERENCE){
            return true;
        }
        rename(attribute.getData(), name);
        return true;
    }
    private boolean checkBool(ResXmlAttribute attribute){
        return checkWithAndroidAttribute("bool",
                attribute, AttributeValueType.BOOL);
    }
    private boolean checkInteger(ResXmlAttribute attribute){
        return checkWithAndroidAttribute("integer",
                attribute, AttributeValueType.INTEGER);
    }
    private boolean checkWithAndroidAttribute(String name,
                                              ResXmlAttribute attribute,
                                              AttributeValueType attributeValueType){
        if(hasRefactoredName(name)){
            return false;
        }
        int nameId=attribute.getNameResourceID();
        if(nameId == 0){
            return false;
        }
        if(attribute.getValueType() != ValueType.REFERENCE){
            return true;
        }
        if(!isEqualAndroidAttributeType(nameId, attributeValueType)){
            return false;
        }
        rename(attribute.getData(), name);
        return true;
    }
    private boolean checkDimen(ResXmlAttribute attribute){
        String name="dimen";
        if(hasRefactoredName(name)){
            return false;
        }
        int layout_width=0x010100f4;
        int layout_height=0x010100f5;
        int nameId=attribute.getNameResourceID();
        if(nameId!=layout_width && nameId!=layout_height){
            return false;
        }
        if(attribute.getValueType() != ValueType.REFERENCE){
            return true;
        }
        rename(attribute.getData(), name);
        return true;
    }
    private boolean checkId(ResXmlAttribute attribute){
        String name="id";
        if(hasRefactoredName(name)){
            return false;
        }
        if(attribute.getNameResourceID()!=AndroidManifestBlock.ID_id){
            return false;
        }
        if(attribute.getValueType() != ValueType.REFERENCE){
            return true;
        }
        rename(attribute.getData(), name);
        return true;
    }
    private boolean checkStyle(ResXmlAttribute attribute){
        String name="style";
        if(hasRefactoredName(name)){
            return false;
        }
        if(attribute.getNameResourceID()!=AndroidManifestBlock.ID_theme){
            return false;
        }
        if(attribute.getValueType() != ValueType.REFERENCE){
            return true;
        }
        rename(attribute.getData(), name);
        return true;
    }
    private boolean checkString(ResXmlAttribute attribute){
        String name="string";
        if(hasRefactoredName(name)){
            return false;
        }
        if(attribute.getNameResourceID()!=AndroidManifestBlock.ID_label){
            return false;
        }
        if(attribute.getValueType() != ValueType.REFERENCE){
            return true;
        }
        rename(attribute.getData(), name);
        return true;
    }
    private boolean isXml(ResXmlElement root){
        if(isPaths(root)){
            return true;
        }
        if(isPreferenceScreen(root)){
            return true;
        }
        return false;
    }
    private boolean isPreferenceScreen(ResXmlElement root){
        if(!"PreferenceScreen".equals(root.getTag())){
            return false;
        }
        for(ResXmlElement element:root.listElements()){
            String tag = element.getTag();
            if("PreferenceCategory".equals(tag)){
                return true;
            }
            if("CheckBoxPreference".equals(tag)){
                return true;
            }
        }
        return false;
    }
    private boolean isPaths(ResXmlElement root){
        if(!"paths".equals(root.getTag())){
            return false;
        }
        for(ResXmlElement element:root.listElements()){
            String tag = element.getTag();
            if("files-path".equals(tag) || "cache-path".equals(tag)){
                return true;
            }
            if("external-path".equals(tag) || "root-path".equals(tag)){
                return true;
            }
            if("external-files-path".equals(tag) || "external-cache-path".equals(tag)){
                return true;
            }
        }
        return false;
    }
    private boolean rename(int resourceId, String name){
        TypeString typeString=getTypeString(resourceId);
        if(typeString==null){
            return false;
        }
        removeTypeString(resourceId);
        addRefactored(resourceId, name);
        if(name.equals(typeString.get())){
            return true;
        }
        logMessage("Renamed: '"+typeString.get()+"' --> '"+name+"'");
        typeString.set(name);
        return true;
    }
    private boolean isEqualAndroidAttributeType(int attributeResourceId, AttributeValueType attributeValueType){
        FrameworkTable frameworkTable = Frameworks.getAndroid();
        EntryGroup entryGroup = frameworkTable.search(attributeResourceId);
        if(entryGroup==null){
            return false;
        }
        Entry entryBlock = entryGroup.pickOne();
        if(entryBlock==null || !entryBlock.isComplex()){
            return false;
        }
        AttributeBag attributeBag =
                AttributeBag.create(((ResTableMapEntry) entryBlock.getTableEntry()).getValue());
        if(attributeBag==null){
            return false;
        }
        if(attributeBag.isFlag() || attributeBag.isEnum()){
            return false;
        }
        return attributeBag.getFormat()
                .isEqualType(attributeValueType);
    }
    private void addRefactored(int id, String name){
        refactoredTypeMap.add(id, name);
    }
    private boolean isFinished(){
        return mTypeStrings.size()==0;
    }
    private boolean hasRefactoredName(String name){
        return refactoredTypeMap.contains(name);
    }
    private boolean hasRefactoredId(int resourceId){
        return refactoredTypeMap.contains(resourceId);
    }
    private TypeString getTypeString(int resourceId){
        return mTypeStrings.get(resourceId&0xffff0000);
    }
    private void removeTypeString(int resourceId){
        mTypeStrings.remove(resourceId&0xffff0000);
    }
    private boolean isColor(ValueType valueType){
        if(valueType==null){
            return false;
        }
        switch (valueType){
            case INT_COLOR_ARGB4:
            case INT_COLOR_RGB4:
            case INT_COLOR_ARGB8:
            case INT_COLOR_RGB8:
                return true;
            default:
                return false;
        }
    }
    private List<ResXmlAttribute> listAttributes(ResXmlElement element){
        if(element==null){
            return new ArrayList<>();
        }
        List<ResXmlAttribute> results = new ArrayList<>(element.listAttributes());
        for(ResXmlElement child:element.listElements()){
            results.addAll(listAttributes(child));
        }
        return results;
    }
    private void logMessage(String msg){
        APKLogger logger=apkLogger;
        if(logger!=null){
            logger.logMessage(msg);
        }
    }
    private void logVerbose(String msg){
        APKLogger logger=apkLogger;
        if(logger!=null){
            logger.logVerbose(msg);
        }
    }
}
