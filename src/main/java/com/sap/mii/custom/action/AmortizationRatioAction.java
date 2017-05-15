package com.sap.mii.custom.action;

import com.google.gson.Gson;
import com.sap.mii.custom.main.AmortizationRatio;
import com.sap.xmii.xacute.actions.ActionReflectionBase;
import com.sap.xmii.xacute.core.ILog;
import com.sap.xmii.xacute.core.Transaction;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by I302473 on 5/11/2017.
 */
public class AmortizationRatioAction extends ActionReflectionBase {

    // input parameter of PIC Tree XML String
    private String input_TreeXML;
    // input parameter of Energy Result XML String
    private String input_EnergyXML;
    // input parameter of root node name
    private String input_RootNodeName;

    // output parameter of return json string
    private String output_JsonStr;
    // output parameter of return Mii XML string
    private String output_XMLStr;

    public AmortizationRatioAction() throws DocumentException {
        input_TreeXML = new String("");
        input_EnergyXML = new String("");
        input_RootNodeName = new String("");
        output_JsonStr = new String("");
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
        try{
            SAXReader saxReader = new SAXReader();
            Document input_EnergyResult = saxReader.read(new ByteArrayInputStream(input_EnergyXML.getBytes()));
            String rootNodeName = input_RootNodeName;
            Gson gson = new Gson();
            RNode rootNode = new RNode(rootNodeName);
            Element rootElement = input_EnergyResult.getRootElement();
            List<Element> rowElements = rootElement.element(AmortizationRatio.NODE_ROWSET).elements(AmortizationRatio.NODE_ROW);
            for(Iterator it = rowElements.iterator(); it.hasNext();){
                Element rowElement = (Element) it.next();
                if(rootNodeName.equalsIgnoreCase(rowElement.elementText(AmortizationRatio.ATTR_NODE_NAME))) {
                    rootNode.setMeterValue(new BigDecimal(rowElement.elementText(AmortizationRatio.ATTR_SUM_ELEC)).setScale(5, BigDecimal.ROUND_HALF_UP));
                    rootNode.setRatio(new BigDecimal(1));
                    rootNode.setCalcuValue(new BigDecimal(rowElement.elementText(AmortizationRatio.ATTR_SUM_ELEC)).setScale(5, BigDecimal.ROUND_HALF_UP));
                    rootNode.setLineLoss(new BigDecimal(0));
                }
            }
            RNode structuredRNode = passTree(rootNode);
            output_JsonStr = gson.toJson(structuredRNode);
            output_XMLStr = parseMiiXml(structuredRNode);
            _success=true;
        }catch (Exception e) {
            _success=false;// Set _success to false if any exception is cought
            ilog.error(e);
        }
    }


    public String getInputTreeXML() {
        return input_TreeXML;
    }

    public void setInputTreeXML(String input_TreeXML) {
        this.input_TreeXML = input_TreeXML;
    }

    public String getInputEnergyXML() {
        return input_EnergyXML;
    }

    public void setInputEnergyXML(String input_EnergyXML) {
        this.input_EnergyXML = input_EnergyXML;
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

    public String getOutXML() {
        return output_XMLStr;
    }
    /**
     * This is required to make the Configure Button Disabled
     * Note: If you want to have Custom ConfigureDialog, you need not put this method.

     */
    public boolean isConfigurable(){
        return false;
    }

    private String parseMiiXml(RNode rNode) {
        String outXmlStr = new String();
        Document outDoc = DocumentHelper.createDocument();
        DateFormat sdf = new SimpleDateFormat(AmortizationRatio.DATE_FORMAT);
        Element root = DocumentHelper.createElement(AmortizationRatio.NODE_ROWSETS);
        root.addAttribute(AmortizationRatio.ATTR_CATCHEDTIME,"");
        root.addAttribute(AmortizationRatio.ATTR_DATECREATED, sdf.format(new Date()));
        root.addAttribute(AmortizationRatio.ATTR_ENDDATE, sdf.format(new Date()));
        root.addAttribute(AmortizationRatio.ATTR_STARTDATE, sdf.format(new Date()));
        root.addAttribute(AmortizationRatio.ATTR_VERSION, AmortizationRatio.VERSION);

        Element rowset = DocumentHelper.createElement(AmortizationRatio.NODE_ROWSET);
        //create Columns
        Element colums = DocumentHelper.createElement(AmortizationRatio.NODE_COLUMNS);
        // create Column
        Element nodeNameColumn = DocumentHelper.createElement(AmortizationRatio.NODE_COLUMN);
        nodeNameColumn = setColumnAttributes(nodeNameColumn, AmortizationRatio.NODE_NODENAME, AmortizationRatio.SQLTYPE_STRING);
        colums.add(nodeNameColumn);

        Element meterValueColumn = DocumentHelper.createElement(AmortizationRatio.NODE_COLUMN);
        meterValueColumn = setColumnAttributes(meterValueColumn, AmortizationRatio.NODE_METERVALUE, AmortizationRatio.SQLTYPE_NUMBER);
        colums.add(meterValueColumn);

        Element ratioColumn = DocumentHelper.createElement(AmortizationRatio.NODE_COLUMN);
        ratioColumn = setColumnAttributes(ratioColumn, AmortizationRatio.NODE_RATIO, AmortizationRatio.SQLTYPE_NUMBER);
        colums.add(ratioColumn);

        Element calcuValueColumn = DocumentHelper.createElement(AmortizationRatio.NODE_COLUMN);
        calcuValueColumn = setColumnAttributes(calcuValueColumn, AmortizationRatio.NODE_CALCUVALUE, AmortizationRatio.SQLTYPE_NUMBER);
        colums.add(calcuValueColumn);

        Element lineLossColumn = DocumentHelper.createElement(AmortizationRatio.NODE_COLUMN);
        lineLossColumn = setColumnAttributes(lineLossColumn, AmortizationRatio.NODE_LINELOSS, AmortizationRatio.SQLTYPE_NUMBER);
        colums.add(lineLossColumn);

        Element parentNodeColumn = DocumentHelper.createElement(AmortizationRatio.NODE_COLUMN);
        parentNodeColumn = setColumnAttributes(parentNodeColumn, AmortizationRatio.NODE_PARENTNODE, AmortizationRatio.SQLTYPE_STRING);
        colums.add(parentNodeColumn);

        rowset.add(colums);

        List<Element> rows = new ArrayList<>();
        rows = generateRowElements(rows, rNode, "---");

        for(Element row : rows){
            rowset.add(row);
        }

        root.add(rowset);
        outDoc.setRootElement(root);
        outXmlStr = outDoc.asXML();
        return outXmlStr;
    }

    private List<Element> generateRowElements(List<Element> rows, RNode rNode, String parent) {
        Element row = DocumentHelper.createElement(AmortizationRatio.NODE_ROW);

        Element name = DocumentHelper.createElement(AmortizationRatio.NODE_NODENAME);
        name.addText(rNode.getName());
        row.add(name);

        Element meterValue = DocumentHelper.createElement(AmortizationRatio.NODE_METERVALUE);
        meterValue.addText(rNode.getMeterValue().toString());
        row.add(meterValue);

        Element ratio = DocumentHelper.createElement(AmortizationRatio.NODE_RATIO);
        ratio.addText(rNode.getRatio().toString());
        row.add(ratio);

        Element calcuValue = DocumentHelper.createElement(AmortizationRatio.NODE_CALCUVALUE);
        calcuValue.addText(rNode.getCalcuValue().toString());
        row.add(calcuValue);

        Element lineLoss = DocumentHelper.createElement(AmortizationRatio.NODE_LINELOSS);
        lineLoss.addText(rNode.getLineLoss().toString());
        row.add(lineLoss);

        Element parentNode = DocumentHelper.createElement(AmortizationRatio.NODE_PARENTNODE);
        parentNode.addText(parent);
        row.add(parentNode);

        rows.add(row);

        for(RNode childNode : rNode.getChildren()){
            generateRowElements(rows, childNode, rNode.getName());
        }

        return rows;
    }

    private Element setColumnAttributes(Element columnElement, String columnName, String sqlType) {
        columnElement.addAttribute(AmortizationRatio.NODE_COLUMN_DESC, columnName);
        columnElement.addAttribute(AmortizationRatio.NODE_COLUMN_MAXRANGE, "0.0");
        columnElement.addAttribute(AmortizationRatio.NODE_COLUMN_MINRANGE, "0.0");
        columnElement.addAttribute(AmortizationRatio.NODE_COLUMN_NAME, columnName);
        columnElement.addAttribute(AmortizationRatio.NODE_COLUMN_SQLDATATYPE, sqlType);
        columnElement.addAttribute(AmortizationRatio.NODE_COLUMN_SOURCECOLUMN, columnName);
        return columnElement;
    }

    public RNode passTree(RNode node) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document input_Tree = saxReader.read(new ByteArrayInputStream(input_TreeXML.getBytes()));
        Document input_EnergyResult = saxReader.read(new ByteArrayInputStream(input_EnergyXML.getBytes()));
        Element rootTreeElement = input_Tree.getRootElement();
        Element rootEnergyElement = input_EnergyResult.getRootElement();
        List<Element> rowTreeElements = rootTreeElement.element(AmortizationRatio.NODE_ROWSET).elements(AmortizationRatio.NODE_ROW);
        List<Element> rowEnergyElements = rootEnergyElement.element(AmortizationRatio.NODE_ROWSET).elements(AmortizationRatio.NODE_ROW);
        List<RNode> childNodes = new ArrayList<>();
        BigDecimal sumChildValue = new BigDecimal(0);
        for(Iterator treeIt = rowTreeElements.iterator(); treeIt.hasNext();){
            Element rowTreeElement = (Element) treeIt.next();
            if( node.getName().equalsIgnoreCase(rowTreeElement.elementText(AmortizationRatio.ATTR_PRT_GRP))){
                RNode childNode = new RNode(rowTreeElement.elementText(AmortizationRatio.ATTR_GRP));
                for(Iterator energyIt = rowEnergyElements.iterator(); energyIt.hasNext();) {
                    Element rowEnergyElement = (Element) energyIt.next();
                    if(childNode.getName().equalsIgnoreCase(rowEnergyElement.elementText(AmortizationRatio.ATTR_NODE_NAME))){
                        BigDecimal childValue = new BigDecimal(rowEnergyElement.elementText(AmortizationRatio.ATTR_SUM_ELEC)).setScale(5, BigDecimal.ROUND_HALF_UP);
                        childNode.setMeterValue(childValue);
                        sumChildValue = sumChildValue.add(childValue);
                    }
                }
                childNodes.add(childNode);
            }
        }
        if(childNodes.size()>0){
            node.setLineLoss(node.getMeterValue().subtract(sumChildValue));
        }
        for(RNode childNode : childNodes) {
            childNode.setRatio(node.getRatio()
                    .multiply(childNode.getMeterValue()
                            .divide(sumChildValue, 5, BigDecimal.ROUND_HALF_UP)).setScale(5, BigDecimal.ROUND_HALF_UP));
            childNode.setCalcuValue(node.getMeterValue().multiply(childNode.getRatio()).setScale(5, BigDecimal.ROUND_HALF_UP));
            childNode.setLineLoss(new BigDecimal(0));
        }
        node.setChildren(childNodes);
        for(RNode childNode : childNodes) {
            passTree(childNode);
        }
        return node;
    }
}
