package edu.uob;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.util.Hashtable;

import com.alexmerz.graphviz.Parser;
import com.alexmerz.graphviz.ParseException;
import com.alexmerz.graphviz.objects.Graph;
import com.alexmerz.graphviz.objects.Node;
import com.alexmerz.graphviz.objects.Edge;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class EntitiesFileTests {

  // Test to make sure that the basic entities file is readable
  @Test
  void testBasicEntitiesFileIsReadable() {
      try {
          Parser parser = new Parser();
          FileReader reader = new FileReader("config" + File.separator + "extended-entities.dot");
          parser.parse(reader);
          Graph wholeDocument = parser.getGraphs().get(0);
          ArrayList<Graph> sections = wholeDocument.getSubgraphs();

          // The locations will always be in the first subgraph
          ArrayList<Graph> locations = sections.get(0).getSubgraphs();

          Graph firstLocation = locations.get(0);
          Node locationDetails = firstLocation.getNodes(false).get(0);
          // Yes, you do need to get the ID twice !
          String locationName = locationDetails.getId().getId();
          Hashtable<String, String> test = locationDetails.getAttributes();
//          System.out.println(test.get("description"));

          //this is forest
//          Graph test1 = locations.get(1);
//          Node locationtest = test1.getNodes(false).get(0);
//          String test2 = locationtest.getId().getId();
//          System.out.println(test2);

          // i want to reach the artefact
          Graph subGraph1 = firstLocation.getSubgraphs().get(0);
          Node subNode1 = subGraph1.getNodes(false).get(2);
          System.out.println(subNode1.toString());
          Graph subGraph3 = firstLocation.getSubgraphs().get(1);
          Node subNode3 = subGraph3.getNodes(false).get(0);
          System.out.println(subNode3.toString());
          Graph subGraph2 = firstLocation.getSubgraphs().get(1);
          Node subNode2 = subGraph2.getNodes(false).get(0);
//          System.out.println(subGraph2.toString());
          System.out.println(subNode2.getId().getId());
          System.out.println(subNode2.toString());

          // test the entitiesStorage hashMap



          ArrayList<Graph> subGraphList = locations.get(0).getSubgraphs();
//          System.out.println(subGraphList.size());
          String nodeName = subNode2.getId().getId();
//          System.out.println(nodeName);
          assertEquals("cabin", locationName, "First location should have been 'cabin'");

          // The paths will always be in the second subgraph
          ArrayList<Edge> paths = sections.get(1).getEdges();
          Edge firstPath = paths.get(0);
          Node fromLocation = firstPath.getSource().getNode();
          String fromName = fromLocation.getId().getId();
          Node toLocation = firstPath.getTarget().getNode();
          String toName = toLocation.getId().getId();
//          System.out.println(fromName);
//          System.out.println(toName);
          Edge secondPath = paths.get(1);
          Node fromLocation2 = secondPath.getSource().getNode();
//          System.out.println(fromLocation2.getId().getId());
          assertEquals("cabin", fromName, "First path should have been from 'cabin'");
          assertEquals("forest", toName, "First path should have been to 'forest'");

      } catch (FileNotFoundException fnfe) {
          fail("FileNotFoundException was thrown when attempting to read basic entities file");
      } catch (ParseException pe) {
          fail("ParseException was thrown when attempting to read basic entities file");
      }
  }

}
