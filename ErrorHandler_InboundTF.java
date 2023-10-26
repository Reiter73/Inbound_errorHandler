/*----------------------ERROR HANDLER PROGRAM TO FETCH RECORD/FIELD INFORMATION--------------------
 * This program is used to fetch the details of the error fields/records/groups.
 * Author: Priya Balakrishnan
 * Date: 3-October-2016
 * Input Given: 1. MXL file of the map
 *              2. Translation error report file from ProcessData
 *              3. Errored out Transation
 * Note: The MXL file has to be saved after removing the attribute xmlns="http://www.w3.org/2001" in the element Mapper
 * Source code help: http://stackoverflow.com/questions/2811001/how-to-read-xml-using-xpath-in-java/2811101
 *                   http://stackoverflow.com/questions/16100175/store-text-file-content-line-by-line-into-array
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ErrorHandler {

        public static void main(String[] args) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder;
                Document doc = null;

                String FileName = "";
                String MapName = "";
                String ProcessID = "";
                String Entity = "";
                String[] MessageVal = null;
                String Message = "";
                String TPCode = "";
                String timestamp = "";
                String ErrorFileName = "";
                String ErrorNature = "";

                /*
                 * String FileName = "CES_DSNO_12345.dat"; String MapName =
                 * "GEN_oINV_D97A_R12_CUM"; String ProcessID = "112233"; String Entity =
                 * "ARD"; String[] MessageVal = MapName.split("_"); String Message =
                 * "856";
                 */
                int AppFlag = 0;

                if (args.length == 6) {
                        FileName = args[0];
                        MapName = args[1];
                        ProcessID = args[2];
                        Entity = args[3];
                        MessageVal = MapName.split("_");
                        Message = MessageVal[1].substring(1, 4);
                        TPCode = args[4];
                        timestamp = args[5];
                } else {
                        System.out
                                        .println("Java Error:| Insufficient arguments have been passed to the Java command.| \n"
                                                        + "Please check for the following elements in the ProcessData in the same order:|\n"
                                                        + "1. First Level Archival file name: FarchFileName|\n"
                                                        + "2. Map name: Original_MapName|\n"
                                                        + "3. Process ID: PROCID|\n"
                                                        + "4. Entity: ENTITY|\n"
                                                        + "5. TPCode: EnvelopeName|\n"
                                                        + "6. Time Stamp: time|\n");
                        return;
                }

                Integer TagStart = 0;
                Integer TagEnd = 0;
                String Tag = "";
                Integer UniqueNumStart = 0;
                Integer UniqueNumEnd = 0;
                Integer LineNum = 0;
                String ReportDate = "";
                String UniqueNumber = null;

                DateFormat SystemDateFormat = new SimpleDateFormat(
                                "dd/MM/yyyy HH:mm:ss");
                Date date = new Date();
                ReportDate = SystemDateFormat.format(date);
                System.out.println("ReportDate:" +  SystemDateFormat.format(date));

                try {

                        // Processing MXL file to remove default Namespace
                        // xmlns="http://www.stercomm.com/SI/Map"
                        File Map = new File("/u02/EDI/SI/scripts/ERH/MAPS/" + MapName
                                        + ".mxl");

                        if (Map.exists()) {
                                try {
                                        TransformerFactory factory1 = TransformerFactory
                                                        .newInstance();
                                        Source xslt = new StreamSource(new File(
                                                        "/u02/EDI/SI/scripts/ERH/XSLT/removeNs.xslt"));
                                        // Source xslt = new StreamSource(new
                                        // File("removeNs.xslt"));
                                        Transformer transformer = factory1.newTransformer(xslt);

                                        Source text = new StreamSource(Map);
                                        transformer.transform(text, new StreamResult(new File(
                                                        "/u02/EDI/SI/scripts/ERH/TEMP/" + ProcessID
                                                                        + ".xml")));
                                        System.out.println("Default Namespace removed from MXL");
                                } catch (TransformerConfigurationException e) {
                                        e.printStackTrace();
                                } catch (TransformerException e) {
                                        e.printStackTrace();
                                }
                        } else {
                                System.out
                                                .println("Java Error:| Please check if "
                                                                + MapName
                                                                + " is present in the directory: /u02/EDI/SI/scripts/ERH/MAPS");
                                return;
                        }

                        builder = factory.newDocumentBuilder();
                        doc = builder.parse("/u02/EDI/SI/scripts/ERH/TEMP/" + ProcessID
                                        + ".xml");

                        // Create XPathFactory object
                        XPathFactory xpathFactory = XPathFactory.newInstance();

                        // Create XPath object
                        XPath xpath = xpathFactory.newXPath();

                        // Reading Error Transaction & getting Unique Number/Message
                        File errFile = new File("/u02/EDI/SI/JOBLOGS/" + Entity + "/STAGE/FAILURE/TEMP/" + ProcessID + "_Error_File.txt");

                        if (errFile.exists()) {
                                FileReader errFileReader = new FileReader(errFile);
                                BufferedReader ErrorFile = new BufferedReader(errFileReader);
                                BufferedReader AppDetails = new BufferedReader(
                                                new FileReader(
                                                                "/u02/EDI/SI/scripts/ERH/APP_DETAILS/ApplicationDetails.txt"));

                                List<String> AppDetailList = new ArrayList<String>();
                                String AppDetailString = "";
                                while ((AppDetailString = AppDetails.readLine()) != null) {
                                        AppDetailList.add(AppDetailString);
                                }

                                String[] AppArray = AppDetailList.toArray(new String[0]);
                                for (int k = 0; k < AppArray.length; k++) {
                                        String AppLine = AppArray[k];
                                        ArrayList<String> AppLineValues = new ArrayList<String>(
                                                        Arrays.asList(AppLine.split(",")));

                                        if ((AppLineValues.get(0).equals(Entity))
                                                        && (AppLineValues.get(1).equals(Message))) {
                                                TagStart = Integer.parseInt(AppLineValues.get(2));
                                                TagEnd = Integer.parseInt(AppLineValues.get(3));
                                                LineNum = Integer.parseInt(AppLineValues.get(4));
                                                Tag = AppLineValues.get(5);
                                                UniqueNumStart = Integer.parseInt(AppLineValues.get(6));
                                                UniqueNumEnd = Integer.parseInt(AppLineValues.get(7));
                                                //System.out.println("TagStart:" + TagStart);
                                                UniqueNumber = getUniqueNumber(TagStart, TagEnd,
                                                                LineNum, Tag, UniqueNumStart, UniqueNumEnd,
                                                                ErrorFile);
                                                AppFlag = 1;
                                        }
                                }

                                if (AppFlag == 0) {
                                        System.out.println("App Flag is not set for the entity"
                                                        + Entity + "and Message " + Message
                                                        + ". Please Add Layout. Unique Number = null");
                                }
                        } else {
                                System.out
                                                .println("Java Error:| Please check if "
                                                                + ProcessID
                                                                + "_Error_File.txt is present in the directory /u02/EDI/SI/JOBLOGS/"
                                                                + Entity + "/STAGE/FAILURE/TEMP");
                                return;

                        }

                        // Reading Translation Report CSV & Getting each Line as a List
                        File errRptFile = new File("/u02/EDI/SI/JOBLOGS/" + Entity
                                        + "/STAGE/ERRORREPORT/" + ProcessID + "_trans_report.txt");

                        if (errRptFile.exists()) {
                                FileReader errRptFileRead = new FileReader(errRptFile);
                                BufferedReader ErrorRpt = new BufferedReader(errRptFileRead);
                                String FieldName = "";
                                String FieldName2;
                                List<String> ErrorList = new ArrayList<String>();

                                while ((FieldName2 = ErrorRpt.readLine()) != null) {
                                        ErrorList.add(FieldName2);
                                }

                                String[] ErrorArr = ErrorList.toArray(new String[0]);

                                String content = new Scanner(new File("/u02/EDI/SI/JOBLOGS/"
                                                + Entity + "/STAGE/ERRORREPORT/" + ProcessID
                                                + "_trans_report.txt")).useDelimiter("\\Z").next();
                                System.out.println(content);

                                if (content.contains("OutputError")) {
                                        ErrorFileName = "/u02/EDI/SI/errors/ERH/translation_failed/app/"
                                                        + TPCode
                                                        + "."
                                                        + ProcessID
                                                        + "."
                                                        + timestamp
                                                        + ".Output.txt";
                                        ErrorNature = "Output";
                                } else {
                                        ErrorFileName = "/u02/EDI/SI/errors/ERH/translation_failed/app/"
                                                        + TPCode
                                                        + "."
                                                        + ProcessID
                                                        + "."
                                                        + timestamp
                                                        + ".Input.txt";
                                        ErrorNature = "Input";
                                }

                                // Appending Input & Output Error Files separately
                                FileReader Read = null;
                                FileWriter Import = null;
                                try {
                                        Scanner scanner = new Scanner("/u02/EDI/SI/JOBLOGS/"
                                                        + Entity + "/STAGE/FAILURE/TEMP/" + ProcessID
                                                        + "_Error_File.txt");
                                        String filename = scanner.nextLine();
                                        File file = new File(filename);
                                        Read = new FileReader(filename);
                                        Import = new FileWriter(ErrorFileName, true);
                                        int ReadInput = Read.read();
                                        System.out.println(ErrorFileName + "Appended Successfully");
                                        while (ReadInput != -1) {
                                                Import.write(ReadInput);
                                                ReadInput = Read.read();
                                        }
                                } catch (IOException e) {
                                        e.printStackTrace();
                                } finally {
                                        Read.close();
                                        Import.close();
                                }

                                FileWriter fw = new FileWriter("/u02/EDI/SI/errors/ERH/RPT/"
                                                + ProcessID + "." + ErrorNature + ".html", true);
                                BufferedWriter bw = new BufferedWriter(fw);
                                PrintWriter outputfile = new PrintWriter(bw);
                                outputfile.print("<html>");
                                outputfile.print("\n");
                                outputfile.print("<head>");
                                outputfile
                                                .print("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
                                outputfile.print("\n<style type=\"Text/css\">");
                                outputfile.print("\n");
                                outputfile.print("table, th, td {");
                                outputfile.print("      border: 1px solid black;");
                                outputfile.print("   }");
                                outputfile.print("th{text-align: center;"
                                                + "background-color: #ccffcc" + "}");
                                outputfile.print("#heading{" + "background-color: #ffcc99;"
                                                + "}");
                                outputfile.print("table {");
                                outputfile.print("    border-collapse: collapse;"
                                // + "table-layout: fixed;");
                                                + "}");
                                outputfile.print("th,td{" + "display: table-cell;"
                                // + "width: 200px;"
                                                + "}");
                                outputfile.print("#sno{" + "width: 1px" + "}");
                                outputfile.print("\n</style>");
                                outputfile.print("\n</head>");
                                outputfile.print("\n<body>");
                                outputfile
                                                .print("\n<table border=\"1\" width=\"100%\"style=\"margin: 0px;\">");
                                outputfile.print("\n<tbody>");
                                // outputfile.print("<br>");
                                outputfile
                                                .print("<th id=\"heading\" align=\"center\" colspan=\"6\" > ");
                                outputfile.print("EDI Error Report");
                                outputfile.print("</th>");

                                outputfile
                                                .print("<tr><th colspan=\"2\">File Name</th><th>Report Date</th><th>Unique Number</th><th>Number of Errors</th><th>Message</th></tr>");
                                outputfile.print("<tr><td colspan=\"2\">" + FileName
                                                + "</td><td>" + ReportDate + "</td><td>" + UniqueNumber
                                                + "</td><td>" + ErrorArr.length + "</td><td>" + Message
                                                + "</td></tr>");
                                outputfile
                                                .print("\n<tr><th id=\"sno\">S.No</th><th>Error</th><th colspan=\"2\">Error Details</th><th colspan=\"2\">Error Data</th></tr>");

                                for (int j = 0; j < ErrorArr.length; j++) {

                                        String Line = ErrorArr[j];
                                        ArrayList<String> LineValues = new ArrayList<String>(
                                                        Arrays.asList(Line.split(",")));

                                        outputfile.print("<tr><td id=\"sno\" align=\"center\">"
                                                        + (j + 1) + "</td><td>" + LineValues.get(2)
                                                        + "</td>");
                                        outputfile.print("<td colspan=\"2\">");
                                        FieldName = (String) LineValues.get(10);
                                        if (!LineValues.get(0).equals("")) {
                                                if (LineValues.get(0).equals("InputError")) {
                                                        outputfile.print("Error Type: Input(Application)"
                                                                + "<br>");
                                                }else if (LineValues.get(0).equals("OutputError")) {
                                                outputfile.print("Error Type: Output(EDI)"
                                                                + "<br>");
                                                }
                                        }
                                        if (!LineValues.get(16).equals("")) {
                                                outputfile.print("Group Name: " + LineValues.get(16)
                                                                + "<br>");
                                        }
										/*
                                        if (!LineValues.get(34).equals("")) {
                                                outputfile.print("Block SignatureID Tag: "
                                                                + LineValues.get(34) + "<br>");
                                        }
										
                                        if (!LineValues.get(4).equals("")) {
                                                outputfile.print("Block Signature: "
                                                                + LineValues.get(4) + "<br>");
                                        }
										*/
                                        if (!LineValues.get(8).equals("")) {
                                                outputfile.print("Block Name: " + LineValues.get(8)
                                                                + "<br>");
                                        }

                                        if (!LineValues.get(36).equals("")) {
                                                outputfile.print("Map Iteration Count: "
                                                                + LineValues.get(36) + "<br>");
                                        }

                                        if (!LineValues.get(10).equals("")) {
                                                outputfile.print("Field Name: " + LineValues.get(10)
                                                                + "<br>");
                                        }
										if (!LineValues.get(20).equals("")) {
                                                outputfile.print("Field Number: " + LineValues.get(20)
                                                                + "<br>");
                                        }

                                        if (!FieldName.equals("")) {

                                                // Field Length & Start Position are not available in
                                                // Standards
                                                // Get Field Length
                                                if (LineValues.get(0).equals("InputError")) {
                                                        Double FieldLength = (Double) getFieldLength(doc,
                                                                        xpath, FieldName);
                                                        int Length = FieldLength.intValue();
                                                        System.out.println("Length: " + Length);
                                                        outputfile.print("Length: " + Length + "<br>");

                                                        // Get Start Position
                                                        Double FieldStartPosition = (Double) getFieldPosition(
                                                                        doc, xpath, FieldName);
                                                        int Position = FieldStartPosition.intValue();
                                                        Position = Position + 1;
														/*
                                                        outputfile.print("Start Position: " + Position
                                                                        + "<br>");
																		*/
                                                        System.out.println("Start Position :" + Position);
                                                }

                                                Double MaxLength = getFieldMaxLength(doc, xpath,
                                                                FieldName);
                                                int MaximumLength = MaxLength.intValue();
                                                outputfile.print("Maximum Length: " + MaximumLength
                                                                + "<br>");

                                                Double MinLength = getFieldMinLength(doc, xpath,
                                                                FieldName);
                                                int MinimumLength = MinLength.intValue();
                                                outputfile.print("Minimum Length: " + MinimumLength
                                                                + "<br>");

                                                String FieldType = getFieldDataType(doc, xpath,
                                                                FieldName);
                                                outputfile.print("Data Type: " + FieldType + "<br>");
                                                System.out.println("Data Type : " + FieldType);

                                                String FieldFormat = getFieldFormat(doc, xpath,
                                                                FieldName);
                                                outputfile
                                                                .print("Data Format: " + FieldFormat + "<br>");
                                                System.out.println("Data Format : " + FieldFormat);

                                        }
										if (!LineValues.get(38).equals("")) {
                                                outputfile.print("Additional Information: " + LineValues.get(38)
                                                                + "<br>");
                                        }

                                        if (!LineValues.get(56).equals("")) {
                                                outputfile.print("Element Position: "
                                                                + LineValues.get(56) + "<br>");
                                        }

                                        /*
                                         * if (!LineValues.get(58).equals("")) {
                                         * outputfile.print("SubElement Position: " +
                                         * LineValues.get(58) + "<br>"); }
                                         */

                                        outputfile.print("</td>");
                                        outputfile.print("<td colspan=\"2\">");
                                        // Field/Block Data
                                        if (!LineValues.get(12).equals("")) {
                                                outputfile.print("Field Data: " + LineValues.get(12)
                                                                + "<br>");
                                        } else if (!LineValues.get(32).equals("")) {
                                                outputfile.print("Raw Block Data: "
                                                                + LineValues.get(32));
                                        } else {
                                                outputfile.print("Not Applicable");
                                        }
                                        outputfile.print("</td></tr>");

                                }
                                outputfile.print("<br>");
                                outputfile.print("\n</tbody>");
                                outputfile.print("\n</table>");
                                outputfile.print("\n</div");
                                outputfile.print("\n</body>");
                                outputfile.print("\n</html>");
                                outputfile.close();
                        } else {
                                System.out.println("Java Error:| Please check if the Error report: "
                                                + ProcessID + "_trans_report.txt"
                                                + " is present in the directory: "
                                                + "/u02/EDI/SI/JOBLOGS/" + Entity
                                                + "/STAGE/ERRORREPORT");
                                return;
                        }

                        // Deleting Temporary files
                        try {
                                String target = new String(
                                                "ksh /u02/EDI/SI/scripts/ERH/TEMP/removeTempFiles.sh"
                                                                + " " + ProcessID + " " + Entity);
                                Runtime rt = Runtime.getRuntime();
                                Process proc = rt.exec(target);
                                proc.waitFor();
                                StringBuffer output = new StringBuffer();
                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(proc.getInputStream()));
                                String line = "";
                                while ((line = reader.readLine()) != null) {
                                        output.append(line + "\n");
                                }
                                System.out.println(output);
                        } catch (Throwable t) {
                                t.printStackTrace();
                        }

                } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                } catch (SAXException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }

                return;

        }

        private static String getUniqueNumber(Integer tagStart, Integer tagEnd,
                        Integer lineNum, String tag, Integer uniqueNumStart,
                        Integer uniqueNumEnd, BufferedReader errorFile) {
                String UniqueNumber = null;
                String UniqNumLine = null;
                String UniqNumLineStr = null;
                Integer Count = 1;

                try {
                        while ((UniqNumLineStr = errorFile.readLine()) != null) {
                                if (Count.equals(lineNum)) {
                                        UniqNumLine = UniqNumLineStr;
                                        System.out.println("Line:" + UniqNumLine);
                                        System.out.println("Line Str:"
                                                        + UniqNumLine.substring(tagStart, tagEnd));
                                }
                                Count++;
                        }
                        if ((UniqNumLine.substring(tagStart, tagEnd).equals(tag)) && (UniqNumLine.length() > uniqueNumEnd)) {
                                UniqueNumber = UniqNumLine.substring(uniqueNumStart,
                                                uniqueNumEnd);
                        }
                        else
                        {
                                UniqueNumber = null;
                        }

                } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                // TODO Auto-generated method stub
                return UniqueNumber;
        }

        private static Double getFieldMaxLength(Document doc, XPath xpath,
                        String fieldName) {
                Double FieldMaxLength = (double) 0;
                try {
                        XPathExpression expr = xpath.compile("//Field[Name='" + fieldName
                                        + "']/StoreLimit/MaxLen/text()");
                        FieldMaxLength = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                }
                // TODO Auto-generated method stub
                return FieldMaxLength;
        }

        private static Double getFieldMinLength(Document doc, XPath xpath,
                        String fieldName) {
                Double FieldMinLength = (double) 0;
                try {
                        XPathExpression expr = xpath.compile("//Field[Name='" + fieldName
                                        + "']/StoreLimit/MinLen/text()");
                        FieldMinLength = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                }
                // TODO Auto-generated method stub
                return FieldMinLength;
        }

        private static Double getFieldPosition(Document doc, XPath xpath,
                        String fieldName) {
                Double FieldStartPosition = (double) 0;
                try {

                        XPathExpression expr = xpath.compile("//Field[Name='" + fieldName
                                        + "']/StartPos/text()");
                        FieldStartPosition = (Double) expr.evaluate(doc,
                                        XPathConstants.NUMBER);
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                        // TODO Auto-generated method stub
                }
                return FieldStartPosition;
                // TODO Auto-generated method stub
        }

        private static Double getFieldLength(Document doc, XPath xpath,
                        String fieldName) {
                Double FieldLength = (double) 0;
                try {

                        XPathExpression expr = xpath.compile("//Field[Name='" + fieldName
                                        + "']/Length/text()");
                        FieldLength = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                        // TODO Auto-generated method stub
                }
                return FieldLength;
        }

        private static String getFieldDataType(Document doc, XPath xpath,
                        String fieldName) {
                String FieldType = null;
                try {

                        XPathExpression expr = xpath.compile("//Field[Name='" + fieldName
                                        + "']/StoreLimit/DataType/text()");
                        FieldType = (String) expr.evaluate(doc, XPathConstants.STRING);
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                }
                return FieldType;
        }

        private static String getFieldFormat(Document doc, XPath xpath,
                        String fieldName) {
                String fieldFormat = null;
                try {
                        XPathExpression expr = xpath.compile("//Field[Name='" + fieldName
                                        + "']/StoreLimit/Format/text()");
                        fieldFormat = (String) expr.evaluate(doc, XPathConstants.STRING);
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                }
                // TODO Auto-generated method stub
                return fieldFormat;
        }
}
