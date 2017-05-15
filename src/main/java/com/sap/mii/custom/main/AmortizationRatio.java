package com.sap.mii.custom.main;

import com.google.gson.Gson;
import com.sap.mii.custom.action.RNode;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Leon on 17/5/10.
 */
public class AmortizationRatio {
    public static final String NODE_ROW = "Row";
    public static final String NODE_ROWSET = "Rowset";
    public static final String ATTR_PRT_GRP = "ParentGroupName";
    public static final String ATTR_GRP = "GroupName";
    public static final String ATTR_NODE_NAME = "NodeName";
    public static final String ATTR_SUM_ELEC = "SUM_POC_Electricity";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String NODE_ROWSETS = "Rowsets";
    public static final String ATTR_CATCHEDTIME = "CachedTime";
    public static final String ATTR_DATECREATED = "DateCreated";
    public static final String ATTR_ENDDATE = "EndDate";
    public static final String ATTR_STARTDATE = "StartDate";
    public static final String ATTR_VERSION = "Version";
    public static final String NODE_COLUMNS = "Columns";
    public static final String NODE_NODENAME = "NodeName";
    public static final String NODE_METERVALUE = "MeterValue";
    public static final String SQLTYPE_STRING = "12";
    public static final String NODE_RATIO = "Ratio";
    public static final String SQLTYPE_NUMBER = "8";
    public static final String NODE_CALCUVALUE = "CalcuValue";
    public static final String NODE_LINELOSS = "LineLoss";
    public static final String NODE_PARENTNODE = "ParentNode";
    public static final String VERSION = "15.1 SP3 Patch 0 (Oct 5, 2016)";
    public static final String NODE_COLUMN_DESC = "Description";
    public static final String NODE_COLUMN_MAXRANGE = "MaxRange";
    public static final String NODE_COLUMN_MINRANGE = "MinRange";
    public static final String NODE_COLUMN_NAME = "Name";
    public static final String NODE_COLUMN_SQLDATATYPE = "SQLDataType";
    public static final String NODE_COLUMN_SOURCECOLUMN = "SourceColumn";
    public static final String NODE_COLUMN = "Column";

    private Document input_Tree;
    private Document input_EnergyResult;
    private String input_RootNodeName;

    private String output_JsonStr;
    private String output_XMLStr;

    public AmortizationRatio(){

    }

    public void Invoke(){
        String rootNodeName = input_RootNodeName;
        Gson gson = new Gson();
        RNode rootNode = new RNode(rootNodeName);
        Element rootElement = input_EnergyResult.getRootElement();
        List<Element> rowElements = rootElement.element(NODE_ROWSET).elements(NODE_ROW);
        for(Iterator it = rowElements.iterator(); it.hasNext();){
            Element rowElement = (Element) it.next();
            if(rootNodeName.equalsIgnoreCase(rowElement.elementText(ATTR_NODE_NAME))) {
                rootNode.setMeterValue(new BigDecimal(rowElement.elementText(ATTR_SUM_ELEC)).setScale(5, BigDecimal.ROUND_HALF_UP));
                rootNode.setRatio(new BigDecimal(1));
                rootNode.setCalcuValue(new BigDecimal(rowElement.elementText(ATTR_SUM_ELEC)).setScale(5, BigDecimal.ROUND_HALF_UP));
                rootNode.setLineLoss(new BigDecimal(0));
            }
        }
        RNode structuredRNode = passTree(rootNode);
        output_JsonStr = gson.toJson(structuredRNode);
        output_XMLStr = parseMiiXml(structuredRNode);

    }

    private String parseMiiXml(RNode rNode) {
        String outXmlStr = new String();
        Document outDoc = DocumentHelper.createDocument();
        DateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Element root = DocumentHelper.createElement(NODE_ROWSETS);
        root.addAttribute(ATTR_CATCHEDTIME,"");
        root.addAttribute(ATTR_DATECREATED, sdf.format(new Date()));
        root.addAttribute(ATTR_ENDDATE, sdf.format(new Date()));
        root.addAttribute(ATTR_STARTDATE, sdf.format(new Date()));
        root.addAttribute(ATTR_VERSION, VERSION);

        Element rowset = DocumentHelper.createElement(NODE_ROWSET);
        //create Columns
        Element colums = DocumentHelper.createElement(NODE_COLUMNS);
        // create Column
        Element nodeNameColumn = DocumentHelper.createElement(NODE_COLUMN);
        nodeNameColumn = setColumnAttributes(nodeNameColumn, NODE_NODENAME, SQLTYPE_STRING);
        colums.add(nodeNameColumn);

        Element meterValueColumn = DocumentHelper.createElement(NODE_COLUMN);
        meterValueColumn = setColumnAttributes(meterValueColumn, NODE_METERVALUE, SQLTYPE_NUMBER);
        colums.add(meterValueColumn);

        Element ratioColumn = DocumentHelper.createElement(NODE_COLUMN);
        ratioColumn = setColumnAttributes(ratioColumn, NODE_RATIO, SQLTYPE_NUMBER);
        colums.add(ratioColumn);

        Element calcuValueColumn = DocumentHelper.createElement(NODE_COLUMN);
        calcuValueColumn = setColumnAttributes(calcuValueColumn, NODE_CALCUVALUE, SQLTYPE_NUMBER);
        colums.add(calcuValueColumn);

        Element lineLossColumn = DocumentHelper.createElement(NODE_COLUMN);
        lineLossColumn = setColumnAttributes(lineLossColumn, NODE_LINELOSS, SQLTYPE_NUMBER);
        colums.add(lineLossColumn);

        Element parentNodeColumn = DocumentHelper.createElement(NODE_COLUMN);
        parentNodeColumn = setColumnAttributes(parentNodeColumn, NODE_PARENTNODE, SQLTYPE_STRING);
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
        Element row = DocumentHelper.createElement(NODE_ROW);

        Element name = DocumentHelper.createElement(NODE_NODENAME);
        name.addText(rNode.getName());
        row.add(name);

        Element meterValue = DocumentHelper.createElement(NODE_METERVALUE);
        meterValue.addText(rNode.getMeterValue().toString());
        row.add(meterValue);

        Element ratio = DocumentHelper.createElement(NODE_RATIO);
        ratio.addText(rNode.getRatio().toString());
        row.add(ratio);

        Element calcuValue = DocumentHelper.createElement(NODE_CALCUVALUE);
        calcuValue.addText(rNode.getCalcuValue().toString());
        row.add(calcuValue);

        Element lineLoss = DocumentHelper.createElement(NODE_LINELOSS);
        lineLoss.addText(rNode.getLineLoss().toString());
        row.add(lineLoss);

        Element parentNode = DocumentHelper.createElement(NODE_PARENTNODE);
        parentNode.addText(parent);
        row.add(parentNode);

        rows.add(row);

        for(RNode childNode : rNode.getChildren()){
            generateRowElements(rows, childNode, rNode.getName());
        }

        return rows;
    }

    private Element setColumnAttributes(Element columnElement, String columnName, String sqlType) {
        columnElement.addAttribute(NODE_COLUMN_DESC, columnName);
        columnElement.addAttribute(NODE_COLUMN_MAXRANGE, "0.0");
        columnElement.addAttribute(NODE_COLUMN_MINRANGE, "0.0");
        columnElement.addAttribute(NODE_COLUMN_NAME, columnName);
        columnElement.addAttribute(NODE_COLUMN_SQLDATATYPE, sqlType);
        columnElement.addAttribute(NODE_COLUMN_SOURCECOLUMN, columnName);
        return columnElement;
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
                        BigDecimal childValue = new BigDecimal(rowEnergyElement.elementText(ATTR_SUM_ELEC)).setScale(5, BigDecimal.ROUND_HALF_UP);
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

    public String getOutJson() {
        return output_JsonStr;
    }

    public String getOutXML() {
        return output_XMLStr;
    }
}
