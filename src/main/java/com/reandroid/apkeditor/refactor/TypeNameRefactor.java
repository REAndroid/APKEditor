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

import com.reandroid.apk.*;
import com.reandroid.app.AndroidManifest;
import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.arsc.chunk.TableBlock;
import com.reandroid.arsc.chunk.xml.AndroidManifestBlock;
import com.reandroid.arsc.chunk.xml.ResXmlAttribute;
import com.reandroid.arsc.chunk.xml.ResXmlDocument;
import com.reandroid.arsc.chunk.xml.ResXmlElement;
import com.reandroid.arsc.model.ResourceEntry;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.model.FrameworkTable;
import com.reandroid.arsc.value.AttributeDataFormat;
import com.reandroid.arsc.value.Entry;
import com.reandroid.arsc.value.ResTableMapEntry;
import com.reandroid.arsc.value.ValueType;
import com.reandroid.arsc.value.array.ArrayBag;
import com.reandroid.arsc.value.attribute.AttributeBag;
import com.reandroid.arsc.value.plurals.PluralsBag;
import com.reandroid.utils.collection.CollectionUtil;
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
        AndroidManifestBlock manifestBlock=apkModule.getAndroidManifest();
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
            element.setName("renamed");
            element.setAttribute("count", String.valueOf(refactoredTypeMap.count()));
            log.append(element.getDebugText());
        }
        TypeNameMap remain=new TypeNameMap();
        for(Map.Entry<Integer, TypeString> entry:mTypeStrings.entrySet()){
            remain.add(entry.getKey(), entry.getValue().get());
        }
        if(remain.count()>0){
            log.append("\n");
            XMLDocument xmlDocument=remain.toXMLDocument();
            XMLElement element=xmlDocument.getDocumentElement();
            element.setName("remain");
            log.append(xmlDocument.getDebugText());
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
        Iterator<ResourceEntry> itr = packageBlock.getResources();
        while (itr.hasNext() && !isFinished()){
            ResourceEntry resourceEntry = itr.next();
            checkEntryGroup(resourceEntry);
        }
    }
    private void checkEntryGroup(ResourceEntry resourceEntry){
        int resourceId=resourceEntry.getResourceId();
        if(hasRefactoredId(resourceId)){
            return;
        }
        boolean renameOk = checkBag(resourceEntry);
        if(renameOk){
            return;
        }
    }
    private boolean checkBag(ResourceEntry resourceEntry){
        if(!hasRefactoredName("style") || !hasRefactoredName("attr")){
            return false;
        }
        boolean hasBagEntry=false;
        Iterator<Entry> itr = resourceEntry.iterator(true);
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
        if(resValueBag.getValue().size()<2){
            return false;
        }
        if(!ArrayBag.isArray(resValueBag.getParentEntry())){
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
        if(resValueBag.getValue().size()<2){
            return false;
        }
        if(!PluralsBag.isPlurals(resValueBag.getParentEntry())){
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
            for(TypeString typeString:packageBlock.getTypeStringPool()){
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
        List<ResXmlAttribute> attributeList = CollectionUtil.toList(xmlBlock.recursiveAttributes());
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
        ResXmlElement root=resXmlDocument.getDocumentElement();
        if(root==null){
            return false;
        }
        String tag=root.getName();
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
        ResXmlElement root=resXmlDocument.getDocumentElement();
        if(root==null){
            return false;
        }
        if(!"alpha".equals(root.getName())){
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
        ResXmlElement root=resXmlDocument.getDocumentElement();
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
        ResXmlElement root=resXmlDocument.getDocumentElement();
        if(root==null){
            return false;
        }
        if(!"menu".equals(root.getName())){
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
        ResXmlElement root=resXmlDocument.getDocumentElement();
        if(root==null){
            return false;
        }
        if(!"selector".equals(root.getName())){
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
        ResXmlElement root=resXmlDocument.getDocumentElement();
        if(root==null){
            return false;
        }
        if(!"vector".equals(root.getName())){
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
        ResXmlElement root=resXmlDocument.getDocumentElement();
        if(root==null){
            return false;
        }
        if(!"LinearLayout".equals(root.getName())){
            return false;
        }
        return rename(resourceId, name);
    }
    private boolean checkAttr(ResXmlAttribute attribute){
        String name="attr";
        if(hasRefactoredName(name)){
            return false;
        }
        return rename(attribute.getNameId(), name);
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
        int nameId=attribute.getNameId();
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
                attribute, AttributeDataFormat.BOOL);
    }
    private boolean checkInteger(ResXmlAttribute attribute){
        return checkWithAndroidAttribute("integer",
                attribute, AttributeDataFormat.INTEGER);
    }
    private boolean checkWithAndroidAttribute(String name,
                                              ResXmlAttribute attribute,
                                              AttributeDataFormat attributeValueType){
        if(hasRefactoredName(name)){
            return false;
        }
        int nameId=attribute.getNameId();
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
        int nameId=attribute.getNameId();
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
        if(attribute.getNameId()!=AndroidManifest.ID_id){
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
        if(attribute.getNameId() != AndroidManifest.ID_theme){
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
        if(attribute.getNameId() != AndroidManifest.ID_label){
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
        if(!"PreferenceScreen".equals(root.getName())){
            return false;
        }
        for(ResXmlElement element:root.listElements()){
            String tag = element.getName();
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
        if(!"paths".equals(root.getName())){
            return false;
        }
        for(ResXmlElement element:root.listElements()){
            String tag = element.getName();
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
    private boolean isEqualAndroidAttributeType(int attributeResourceId, AttributeDataFormat attributeValueType){
        FrameworkApk frameworkApk = AndroidFrameworks.getCurrent();
        if(frameworkApk==null){
            return false;
        }
        FrameworkTable frameworkTable = frameworkApk.getTableBlock();
        if(frameworkTable==null){
            return false;
        }
        ResourceEntry resourceEntry = frameworkTable.getResource(attributeResourceId);
        if(resourceEntry==null){
            return false;
        }
        Entry entryBlock = resourceEntry.get();
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
