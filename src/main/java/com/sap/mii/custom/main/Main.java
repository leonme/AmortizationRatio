package com.sap.mii.custom.main;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Created by I302473 on 5/15/2017.
 */
public class Main {
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

        jsonResult = ar.getOutJson();

        System.out.println(jsonResult);
        System.out.println(ar.getOutXML());
    }
}
