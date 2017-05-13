package com.sap.mii.custom.action;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Created by I302473 on 5/11/2017.
 */
public class main {
    public static void main(String[] args) throws Exception{
        SAXReader sr = new SAXReader();
        Document energyDoc = sr.read("C:\\Users\\i302473\\workspace\\AmortizationRatio\\src\\main\\resources\\xml\\Energy.xml");
        Document treeDoc = sr.read("C:\\Users\\i302473\\workspace\\AmortizationRatio\\src\\main\\resources\\xml\\Tree.xml");
        String rootNodeName = "E|TOTAL";

        String jsonResult;

        AmortizationRatio ar = new AmortizationRatio();
        ar.setInputEnergyResult(energyDoc);
        ar.setInputRootNodeName(rootNodeName);
        ar.setInputTree(treeDoc);

        ar.Invoke();

        jsonResult = ar.getOutResult();

        System.out.println(jsonResult);
    }
}
