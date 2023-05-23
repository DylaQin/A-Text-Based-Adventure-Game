package edu.uob;

import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.objects.Edge;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class GameInitiate{

    static TreeMap<String, Locations> locationStorage;
    static TreeMap<String, Artefacts> artefactStorage;
    static TreeMap<String, Furniture> furnitureStorage;
    static TreeMap<String, Characters> characterStorage;
    static TreeMap<String, Players> playerStorage;
    static TreeMap<String, ActionEntity> actionStorage;
    static Locations fromLocation;
    static HashSet<String> basicCommand;


    public GameInitiate() {
        locationStorage = new TreeMap<>();
        artefactStorage = new TreeMap<>();
        furnitureStorage = new TreeMap<>();
        characterStorage = new TreeMap<>();
        playerStorage = new TreeMap<>();
        actionStorage = new TreeMap<>();
        initiateBasicCommand();
    }

    public void parseEntityFile(File entitiesFile){
        try {
            Parser parser = new Parser();
            FileReader reader = new FileReader(entitiesFile);
            parser.parse(reader);
            Graph wholeDocument = parser.getGraphs().get(0);
            ArrayList<Graph> sections = wholeDocument.getSubgraphs();
            ArrayList<Graph> locations = sections.get(0).getSubgraphs();
            ArrayList<Edge> paths = sections.get(1).getEdges();

            getEntitiesfrFile(locations);
            getPathsfrFile(paths);

        } catch (FileNotFoundException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void parseActionFile(File actionFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(actionFile);
            Element root = document.getDocumentElement();
            NodeList actions = root.getChildNodes();

            getActionfrFile(actions);

        } catch (ParserConfigurationException | SAXException | IOException pce){
            throw new RuntimeException(pce);
        }

    }

    public void getEntitiesfrFile(ArrayList<Graph> locations){
        int locationsIndex=0;
        while(locationsIndex<locations.size()) {

            Graph eachLocation = locations.get(locationsIndex);
            Node locationDetail = eachLocation.getNodes(false).get(0);
            String locDes = locationDetail.getAttribute("description");
            String locName = locationDetail.getId().getId();
            Locations locationEntity = new Locations(locName,locDes);

            ArrayList<Graph> subNodeList = eachLocation.getSubgraphs();
            getEntitiesfrFileHelp1(subNodeList,locationEntity);

            if (locationsIndex==0){
                fromLocation = locationEntity;
            }
            locationsIndex++;
            locationStorage.put(locName,locationEntity);

        }
    }

    public void getEntitiesfrFileHelp1(ArrayList<Graph> subNodeList, Locations locationEntity){
        int entityTypeIndex=0;
        while(entityTypeIndex<subNodeList.size()){
            Graph eachNode = subNodeList.get(entityTypeIndex);
            String graphName = eachNode.getId().getId();
            ArrayList<Node> subNodes = eachNode.getNodes(false);
            getEntitiesfrFileHelp2(graphName,subNodes,locationEntity);
            entityTypeIndex++;
        }
    }

    public void getEntitiesfrFileHelp2(String graphName, ArrayList<Node> subNodes, Locations locationEntity){
        int entityIndex=0;
        while (entityIndex<subNodes.size()){
            Node item = subNodes.get(entityIndex);
            String itemDes = item.getAttribute("description");
            String itemName = item.getId().getId();
            addEntitybyGraph(itemName,itemDes,graphName,locationEntity);
            entityIndex++;
        }
    }

    public void addEntitybyGraph(String itemName, String itemDescription, String graphName, Locations location){
        switch (graphName) {
            case "artefacts" -> {
                location.artefacts.put(itemName, null);
                Artefacts artefacts = new Artefacts(itemName, itemDescription);
                artefactStorage.put(itemName, artefacts);
            }
            case "furniture" -> {
                location.furniture.put(itemName, null);
                Furniture furniture = new Furniture(itemName,itemDescription);
                furnitureStorage.put(itemName, furniture);
            }
            case "characters" -> {
                location.characters.put(itemName, null);
                Characters characterEntity = new Characters(itemName,itemDescription);
                characterStorage.put(itemName,characterEntity);
            }
        }
    }

    public void getPathsfrFile(ArrayList<Edge> paths){
        for (Edge subPath : paths) {
            Node fromLocation = subPath.getSource().getNode();
            Node toLocation = subPath.getTarget().getNode();
            String fromName = fromLocation.getId().getId();
            String toName = toLocation.getId().getId();

            Locations fromEntity = locationStorage.get(fromName);
            Locations toEntity = locationStorage.get(toName);
            fromEntity.destination.put(toEntity.getName(),toEntity);

        }
    }

    public void getActionfrFile(NodeList actions){

        for (int i=1;i<actions.getLength();i+=2){
            Element eachAction = (Element) actions.item(i);
            Element triggers = (Element) eachAction.getElementsByTagName("triggers").item(0);
            NodeList keyphrase = triggers.getChildNodes();
            for (int j=1;j<keyphrase.getLength();j++){
                ActionEntity gameAction = new ActionEntity();
                String triggerPhrase = keyphrase.item(j).getTextContent();
                helpFillAction(gameAction, eachAction);
                gameAction.trigger = triggerPhrase;
                actionStorage.put(gameAction.trigger,gameAction);
            }
        }
    }

    public void helpFillAction(ActionEntity gameAction, Element eachAction){

        Element subjects = (Element) eachAction.getElementsByTagName("subjects").item(0);
        NodeList entities = subjects.getElementsByTagName("entity");
        for (int i=0;i<entities.getLength();i++){
            ArrayList<String> itemList = gameAction.subjectList;
            itemList.add(entities.item(i).getTextContent());
        }

        Element consumed = (Element) eachAction.getElementsByTagName("consumed").item(0);
        entities = consumed.getElementsByTagName("entity");
        for (int i=0;i<entities.getLength();i++){
            gameAction.consumedList.add(entities.item(i).getTextContent());
        }

        Element produced = (Element) eachAction.getElementsByTagName("produced").item(0);
        entities = produced.getElementsByTagName("entity");
        for (int i=0;i<entities.getLength();i++){
            gameAction.producedList.add(entities.item(i).getTextContent());
        }

        Element narrate = (Element) eachAction.getElementsByTagName("narration").item(0);
        gameAction.narration = narrate.getTextContent();

    }

    public void initiateBasicCommand(){
        basicCommand = new HashSet<>();
        basicCommand.add("inventory");
        basicCommand.add("inv");
        basicCommand.add("drop");
        basicCommand.add("look");
        basicCommand.add("get");
        basicCommand.add("goto");
    }
}
