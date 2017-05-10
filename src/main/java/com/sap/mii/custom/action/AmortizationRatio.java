package com.sap.mii.custom.action;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.dom4j.Document;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Leon on 17/5/10.
 */
public class AmortizationRatio {
    public final String NODE_ROOT = "Root";
    public final String NODE_ROW = "Row";
    public final String ATTR_PRT_GRP = "ParentGroupName";
    public final String ATTR_GRP = "GroupName";
    public final String ATTR_NODE_NAME = "NodeName";
    public final String ATTR_SUM_ELEC = "SUM_POC_Electricity";
    public final String JSON_CHILD = "Children";

    private Document input_Tree;
    private Document input_EnergyResult;
    private String input_RootNodeName;

    private String output_JsonStr;

    public AmortizationRatio(){

    }

    public void Invoke(){
        String rootNodeName = input_RootNodeName;
        JsonObject outputJson = new JsonParser().parse("{}").getAsJsonObject();
        Gson gson = new Gson();
        RNode rootNode = new RNode(rootNodeName);
        Element rootElement = input_EnergyResult.getRootElement();
        List<Element> rowElements = rootElement.elements(NODE_ROW);
        for(Iterator it = rowElements.iterator(); it.hasNext();){
            Element rowElement = (Element) it.next();
            if(rootNodeName == rowElement.attributeValue(ATTR_NODE_NAME)) {
                rootNode.setMeterValue(new BigDecimal(rowElement.attributeValue(ATTR_SUM_ELEC)));
                rootNode.setRatio(new BigDecimal(1));
                rootNode.setCalcuValue(new BigDecimal(rowElement.attributeValue(ATTR_SUM_ELEC)));
                rootNode.setLineLoss(new BigDecimal(0));
            }
        }

        JsonElement jsonElements = passTree(rootNode, gson.toJsonTree(rootNode));
        outputJson.add(NODE_ROOT, jsonElements);
        output_JsonStr = gson.toJson(outputJson);
    }

    public JsonElement passTree(RNode node, JsonElement jsonElement) {
        Gson gson = new Gson();
        Element rootTreeElement = input_Tree.getRootElement();
        Element rootEnergyElement = input_EnergyResult.getRootElement();
        List<Element> rowTreeElements = rootTreeElement.elements(NODE_ROW);
        List<Element> rowEnergyElements = rootEnergyElement.elements(NODE_ROW);
        List<RNode> childNodes = new ArrayList<RNode>();
        BigDecimal sumChildValue = new BigDecimal(0);
        boolean isLeafNode = true;
        for(Iterator treeIt = rowTreeElements.iterator(); treeIt.hasNext();){
            Element rowTreeElement = (Element) treeIt.next();
            if( node.getName() == rowTreeElement.attributeValue(ATTR_PRT_GRP)){
                isLeafNode = false;
                RNode childNode = new RNode(rowTreeElement.attributeValue(ATTR_GRP));
                for(Iterator energyIt = rowEnergyElements.iterator(); energyIt.hasNext();) {
                    Element rowEnergyElement = (Element) energyIt.next();
                    if(childNode.getName() == rowEnergyElement.attributeValue(ATTR_NODE_NAME)){
                        BigDecimal childValue = new BigDecimal(rowEnergyElement.attributeValue(ATTR_SUM_ELEC));
                        childNode.setMeterValue(childValue);
                        sumChildValue.add(childValue);
                    }
                }
                childNodes.add(childNode);
            }
        }
        if(childNodes.size() > 0) {
            for(RNode childNode : childNodes) {
                childNode.setRatio(childNode.getMeterValue().divide(sumChildValue));
                childNode.setCalcuValue(node.getMeterValue().multiply(childNode.getRatio()));
                childNode.setLineLoss(node.getMeterValue().subtract(childNode.getCalcuValue()));
            }
            jsonElement.getAsJsonObject().add(JSON_CHILD, gson.toJsonTree(childNodes));
        }

        if(isLeafNode) {
            // TODO: 递归判断
        }
        return jsonElement;
    }

    public Document getInputTree() {
        return input_Tree;
    }

    public void setInputTree(Document tree) {
        this.input_Tree = tree;
    }

    public Document getInputEnergyResult() {
        return input_EnergyResult;
    }

    public void setInputEnergyResult(Document energyResult) {
        this.input_EnergyResult = energyResult;
    }

    public String getInputRootNodeName() {
        return input_RootNodeName;
    }

    public void setInputRootNodeName(String rootNodeName) {
        this.input_RootNodeName = rootNodeName;
    }

    public String getOutResult() {
        return output_JsonStr;
    }
}
