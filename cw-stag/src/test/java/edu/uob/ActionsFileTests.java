package edu.uob;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

final class ActionsFileTests {

  // Test to make sure that the basic actions file is readable
  @Test
  void testBasicActionsFileIsReadable() {
      try {
          DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
          Document document = builder.parse("config" + File.separator + "extended-actions.xml");
          Element root = document.getDocumentElement();
          NodeList actions = root.getChildNodes();
          // Get the first action (only the odd items are actually actions - 1, 3, 5 etc.)
          int actionLength = actions.getLength();
          System.out.println(actionLength);
          Element firstAction = (Element)actions.item(3);
          Element triggers = (Element)firstAction.getElementsByTagName("triggers").item(0);

          NodeList keyphrase = triggers.getChildNodes();
          String test = keyphrase.item(2).getTextContent();
          System.out.println(test);

          Element narrate = (Element) firstAction.getElementsByTagName("narration").item(0);
          System.out.println(narrate.getTextContent());


//          int length = firstAction.getElementsByTagName("subjects").getLength();
//          Element subjects = (Element) firstAction.getElementsByTagName("subjects").item(0);
//          String subject = subjects.getTextContent();
//          System.out.println(subject);
//          System.out.println(length);
//          // Get the first trigger phrase
//          String firstTriggerPhrase = triggers.getElementsByTagName("keyphrase").item(0).getTextContent();
//          System.out.println(firstTriggerPhrase);
//
//          Element narrate = (Element) firstAction.getElementsByTagName("narration").item(0);
//          String narration = narrate.getTextContent();
//          System.out.println(narration);
//          assertEquals("open", firstTriggerPhrase, "First trigger phrase was not 'open'");
      } catch(ParserConfigurationException pce) {
          fail("ParserConfigurationException was thrown when attempting to read basic actions file");
      } catch(SAXException saxe) {
          fail("SAXException was thrown when attempting to read basic actions file");
      } catch(IOException ioe) {
          fail("IOException was thrown when attempting to read basic actions file");
      }
  }

}
