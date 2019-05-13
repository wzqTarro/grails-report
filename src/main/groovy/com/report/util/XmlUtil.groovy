package com.report.util

import javax.xml.XMLConstants
import javax.xml.transform.Source
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

class XmlUtil {
    /**
     * 生成报表xsd
     * @return
     */
    private static String initReportXSD() {
        String xsd = "<?xml version=\"1.0\"?>\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "<xs:element name=\"report\">\n" +
                "\t<xs:complexType>\n" +
                "\t\t<xs:sequence>\n" +
                "\t\t\t<xs:element name=\"var_set\">\n" +
                "\t\t\t\t<xs:complexType>\n" +
                "\t\t\t\t\t<xs:sequence>\n" +
                "\t\t\t\t\t\t<xs:element name=\"var_record\"  maxOccurs=\"unbounded\">\n" +
                "\t\t\t\t\t\t\t<xs:complexType>\n" +
                "\t\t\t\t\t\t\t\t<xs:sequence>\n" +
                "\t\t\t\t\t\t\t\t\t<xs:element name=\"var_name\"/>\n" +
                "\t\t\t\t\t\t\t\t\t<xs:element name=\"var_value\"/>\n" +
                "\t\t\t\t\t\t\t\t\t<xs:element name=\"var_title\"/>\n" +
                "\t\t\t\t\t\t\t\t</xs:sequence>\n" +
                "\t\t\t\t\t\t\t</xs:complexType>\n" +
                "\t\t\t\t\t\t</xs:element>\n" +
                "\t\t\t\t\t</xs:sequence>\n" +
                "\t\t\t\t</xs:complexType>\n" +
                "\t\t\t</xs:element>\n" +
                "\t\t\t<xs:element name=\"data_table\"  maxOccurs=\"unbounded\">\n" +
                "\t\t\t\t<xs:complexType>\n" +
                "\t\t\t\t\t<xs:sequence>\n" +
                "\t\t\t\t\t\t<xs:element name=\"record\" maxOccurs=\"unbounded\"></xs:element>\n" +
                "\t\t\t\t\t</xs:sequence>\n" +
                "\t\t\t\t\t<xs:attribute name=\"name\" type=\"xs:string\"/>\n" +
                "\t\t\t\t\t<xs:attribute name=\"seq_num\" type=\"xs:integer\"/>\n" +
                "\t\t\t\t</xs:complexType>\n" +
                "\t\t\t</xs:element>\n" +
                "\t\t</xs:sequence>\n" +
                "\t</xs:complexType>\n" +
                "</xs:element>\n" +
                "</xs:schema>"
        return xsd
    }
    /**
     * 生成大屏结构xsd
     * @return
     */
    private static String initScreenStructXSD() {
        String xsd = "<?xml version=\"1.0\"?>\n" +
                "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "<xs:element name=\"report\">\n" +
                "\t<xs:complexType>\n" +
                "\t\t<xs:sequence>\n" +
                "\t\t\t<xs:element name=\"var_set\">\n" +
                "\t\t\t\t<xs:complexType>\n" +
                "\t\t\t\t\t<xs:sequence>\n" +
                "\t\t\t\t\t\t<xs:element name=\"var_record\"  maxOccurs=\"unbounded\">\n" +
                "\t\t\t\t\t\t\t<xs:complexType>\n" +
                "\t\t\t\t\t\t\t\t<xs:sequence>\n" +
                "\t\t\t\t\t\t\t\t\t<xs:element name=\"var_name\"/>\n" +
                "\t\t\t\t\t\t\t\t\t<xs:element name=\"var_value\"/>\n" +
                "\t\t\t\t\t\t\t\t\t<xs:element name=\"var_title\"/>\n" +
                "\t\t\t\t\t\t\t\t</xs:sequence>\n" +
                "\t\t\t\t\t\t\t</xs:complexType>\n" +
                "\t\t\t\t\t\t</xs:element>\n" +
                "\t\t\t\t\t</xs:sequence>\n" +
                "\t\t\t\t</xs:complexType>\n" +
                "\t\t\t</xs:element>\n" +
                "\t\t\t<xs:element name=\"data_table\" maxOccurs=\"unbounded\">\n" +
                "\t\t\t\t<xs:complexType>\n" +
                "\t\t\t\t\t<xs:sequence>\n" +
                "\t\t\t\t\t\t<xs:element name=\"name\"/>\n" +
                "\t\t\t\t\t\t<xs:element name=\"columns\">\n" +
                "\t\t\t\t\t\t\t<xs:complexType>\n" +
                "\t\t\t\t\t\t\t\t<xs:sequence>\n" +
                "\t\t\t\t\t\t\t\t\t<xs:element name=\"column\"  maxOccurs=\"unbounded\"/>\n" +
                "\t\t\t\t\t\t\t\t</xs:sequence>\n" +
                "\t\t\t\t\t\t\t</xs:complexType>\n" +
                "\t\t\t\t\t\t</xs:element>\n" +
                "\t\t\t\t\t</xs:sequence>\n" +
                "\t\t\t\t</xs:complexType>\n" +
                "\t\t\t</xs:element>\n" +
                "\t\t</xs:sequence>\n" +
                "\t</xs:complexType>\n" +
                "</xs:element>\n" +
                "</xs:schema>"
        return xsd
    }
    /**
     * 验证xml
     * @param xml
     * @param xsd
     * @return
     */
    private static Validator validate(String xsd) {
        Validator validator = SchemaFactory.newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI )
                .newSchema( new StreamSource( new ByteArrayInputStream(xsd.bytes) ) )
                .newValidator()
        return validator
    }
    /**
     * 验证报表xml格式
     * @param xml
     * @return
     */
    static Validator validateReport() {
        String xsd = initReportXSD()
        return validate(xsd)
    }
    /**
     * 验证大屏xml格式
     * @param xml
     * @return
     */
    static Validator validateScreen() {
        String xsd = initScreenStructXSD()
        return validate(xsd)
    }
}
