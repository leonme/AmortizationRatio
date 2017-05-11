package com.sap.mii.custom.action;

import com.google.gson.Gson;
import com.sap.xmii.xacute.actions.ActionReflectionBase;
import com.sap.xmii.xacute.core.ILog;
import com.sap.xmii.xacute.core.Transaction;
import org.dom4j.Document;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by I302473 on 5/11/2017.
 */
public class AmortizationRatioAction extends ActionReflectionBase {
    public final String NODE_ROW = "Row";
    public final String NODE_ROWSET = "Rowset";
    public final String ATTR_PRT_GRP = "ParentGroupName";
    public final String ATTR_GRP = "GroupName";
    public final String ATTR_NODE_NAME = "NodeName";
    public final String ATTR_SUM_ELEC = "SUM_POC_Electricity";

    private Document input_Tree;
    private Document input_EnergyResult;
    private String input_RootNodeName;

    private String output_JsonStr;

    public AmortizationRatioAction(){

    }

    /**
     * This will take the Icon to display in the BLS
     */
    public String GetIconPath() {
        return "/ActionIcon.png";
    }


    /**
     * This method contains the actual business logic for the
     * Action Block
     */
    public void Invoke(Transaction trx, ILog ilog)
    {
        String rootNodeName = input_RootNodeName;
        Gson gson = new Gson();
        RNode rootNode = new RNode(rootNodeName);
        Element rootElement = input_EnergyResult.getRootElement();
        List<Element> rowElements = rootElement.element(NODE_ROWSET).elements(NODE_ROW);
        for(Iterator it = rowElements.iterator(); it.hasNext();){
            Element rowElement = (Element) it.next();
            if(rootNodeName.equalsIgnoreCase(rowElement.elementText(ATTR_NODE_NAME))) {
                rootNode.setMeterValue(new BigDecimal(rowElement.elementText(ATTR_SUM_ELEC)));
                rootNode.setRatio(new BigDecimal(1));
                rootNode.setCalcuValue(new BigDecimal(rowElement.elementText(ATTR_SUM_ELEC)));
                rootNode.setLineLoss(new BigDecimal(0));
            }
        }
        RNode structuredRNode = passTree(rootNode);
        output_JsonStr = gson.toJson(structuredRNode);
    }

    /**
     * This is required to make the Configure Button Disabled
     * Note: If you want to have Custom ConfigureDialog, you need not put this method.

     */
    public boolean isConfigurable(){
        return false;
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

    public RNode passTree(RNode node) {
        Element rootTreeElement = input_Tree.getRootElement();
        Element rootEnergyElement = input_EnergyResult.getRootElement();
        List<Element> rowTreeElements = rootTreeElement.element(NODE_ROWSET).elements(NODE_ROW);
        List<Element> rowEnergyElements = rootEnergyElement.element(NODE_ROWSET).elements(NODE_ROW);
        List<RNode> childNodes = new ArrayList<>();
        BigDecimal sumChildValue = new BigDecimal(0);
        for(Iterator treeIt = rowTreeElements.iterator(); treeIt.hasNext();){
            Element rowTreeElement = (Element) treeIt.next();
            if( node.getName().equalsIgnoreCase(rowTreeElement.elementText(ATTR_PRT_GRP))){
                RNode childNode = new RNode(rowTreeElement.elementText(ATTR_GRP));
                for(Iterator energyIt = rowEnergyElements.iterator(); energyIt.hasNext();) {
                    Element rowEnergyElement = (Element) energyIt.next();
                    if(childNode.getName().equalsIgnoreCase(rowEnergyElement.elementText(ATTR_NODE_NAME))){
                        BigDecimal childValue = new BigDecimal(rowEnergyElement.elementText(ATTR_SUM_ELEC));
                        childNode.setMeterValue(childValue);
                        sumChildValue = sumChildValue.add(childValue);
                    }
                }
                childNodes.add(childNode);
            }
        }
        for(RNode childNode : childNodes) {
            childNode.setRatio(node.getRatio()
                    .multiply(childNode.getMeterValue()
                            .divide(sumChildValue, 5, BigDecimal.ROUND_HALF_UP)));
            childNode.setCalcuValue(node.getMeterValue().multiply(childNode.getRatio()));
            childNode.setLineLoss(node.getMeterValue().subtract(childNode.getCalcuValue()));
        }
        node.setChildren(childNodes);
        for(RNode childNode : childNodes) {
            passTree(childNode);
        }
        return node;
    }
}
