import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        String json = listToJson(parseCSV(columnMapping, fileName));
        writeString(json, "data.json");

        List<Employee> list = new ArrayList<>();
        try {
            list = parseXML("data.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String json2 = listToJson(list);
        writeString(json2, "data2.json");

        String jsonFromJson = readString("data.json");
        List<Employee> listJson = jsonToList(jsonFromJson);

    }

    private static List<Employee> jsonToList(String jsonFromJson) {
        List<Employee> employeeList = new ArrayList<>();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        JSONParser parser = new JSONParser();
        try {
            JSONArray array = (JSONArray) parser.parse(jsonFromJson);
            for (Object object : array) {
                Employee employee = gson.fromJson(String.valueOf(object), Employee.class);
                employeeList.add(employee);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return employeeList;

    }

    private static String readString(String path) {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String s;
            while ((s = reader.readLine()) != null) {
                result.append(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    private static List<Employee> parseXML(String s) throws ParserConfigurationException, IOException, SAXException {
        List<Employee> result = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(s));
        doc.getDocumentElement().normalize();
        NodeList nodeList = doc.getElementsByTagName("employee");
        for (int i = 0; i < nodeList.getLength(); i++) {
            result.add(getEmployee(nodeList.item(i)));
        }
        return result;
    }

    private static Employee getEmployee(Node node) {
        Employee employee = new Employee();
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            employee.id = Long.parseLong(getTagValue("id", element));
            employee.firstName = getTagValue("firstName", element);
            employee.lastName = getTagValue("lastName", element);
            employee.country = getTagValue("country", element);
            employee.age = Integer.parseInt(getTagValue("age", element));
        }

        return employee;
    }

    private static String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }


    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        List<Employee> result = null;
        try (CSVReader reader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(reader)
                    .withMappingStrategy(strategy)
                    .build();
            result = csv.parse();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String listToJson(List<Employee> list) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        return gson.toJson(list, listType);
    }

    private static void writeString(String str, String path) {
        try (FileWriter writer = new FileWriter(path)) {
            writer.write(str);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

