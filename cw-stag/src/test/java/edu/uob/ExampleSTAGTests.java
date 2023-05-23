package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ExampleSTAGTests {

    private GameServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    void setup() {
      File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
      File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
      server = new GameServer(entitiesFile, actionsFile);
    }

    String sendCommandToServer(String command) {
      // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
      return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
      "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A lot of tests will probably check the game state using 'look' - so we better make sure 'look' works well !
    @Test
    void testLook() {
        String response = sendCommandToServer("simon: look");
        response = response.toLowerCase();
        assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
        assertTrue(response.contains("log cabin"), "Did not see a description of the room in response to look");
        assertTrue(response.contains("magic potion"), "Did not see a description of artifacts in response to look");
        assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
        assertTrue(response.contains("forest"), "Did not see available paths in response to look");
    }

    // Test that we can pick something up and that it appears in our inventory
    @Test
    void testGet()
    {
      String response;
      sendCommandToServer("simon: get potion");
      response = sendCommandToServer("simon: inv");
      response = response.toLowerCase();

      assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
      response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
    }

    // Test that we can goto a different location (we won't get very far if we can't move around the game !)
    @Test
    void testGoto()
    {
      sendCommandToServer("simon: goto forest");
      String response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
    }

    // Add more unit tests or integration tests here.
    @Test
    void testDrop(){
      String response;
      sendCommandToServer("simon: get axe");
      sendCommandToServer("simon: drop axe");
      response = sendCommandToServer("simon: inv");
      assertFalse(response.contains("axe"),"axe is still in the inventory after drop");
    }

    @Test
    void testNullReturn(){
      String response;
      response = sendCommandToServer("simon ");
      assertTrue(response.contains("wrong"),"command should be invalid");
      response = sendCommandToServer("simon: ");
      assertTrue(response.contains("wrong"),"command should be invalid");
      response = sendCommandToServer("simon: lov");
      assertTrue(response.contains("wrong"),"command should be invalid");
    }

    @Test
    void testAction(){
      String response;
      sendCommandToServer("simon: get axe");
      sendCommandToServer("simon: goto forest");
      sendCommandToServer("simon: cut tree");
      response = sendCommandToServer("simon: look");
      assertTrue(response.contains("log"),"log is not generate after cut down the tree");
      sendCommandToServer("simon: get log");
      response = sendCommandToServer("simon: inv");
      assertTrue(response.contains("log"),"get command failed to get log");
      sendCommandToServer("simon: get key");
      sendCommandToServer("simon: goto cabin");
      response = sendCommandToServer("simon: unlock trapdoor");
      assertTrue(response.contains("cellar"), "description load failed");
      response = sendCommandToServer("simon: goto cellar");
      assertTrue(response.contains("cellar"),"new path generated failed");
      response = sendCommandToServer("simon: hit attack elf");
      assertTrue(response.contains("health"),"double action trigger belong to the same narration works");
      sendCommandToServer("simon: goto cabin");
      sendCommandToServer("simon: goto forest");

      sendCommandToServer("simon: goto riverbank");
      response = sendCommandToServer("simon: blow horn");
      assertTrue(response.contains("lumberjack"),"a lumberjack should appear in the description");
      response = sendCommandToServer("simon: look");
      assertTrue(response.contains("lumberjack"),"a lumberjack should appear in the current location");
      sendCommandToServer("simon: bridge log");
      response = sendCommandToServer("simon: look");
      assertTrue(response.contains("clearing"), " a new path named clearing should generate");
      sendCommandToServer("simon: goto clearing");
      response = sendCommandToServer("dig ground");
      assertFalse(response.contains("gold"), "dig ground doest work case there is no shovel in the inv");
      sendCommandToServer("simon: goto riverbank");
      sendCommandToServer("simon: goto forest");
      sendCommandToServer("simon: goto cabin");
      sendCommandToServer("simon: get coin");
      sendCommandToServer("simon: goto cellar");
      response = sendCommandToServer("simon: pay elf");
      assertTrue(response.contains("shovel"),"shovel should be produced");
      sendCommandToServer("simon: get shovel");
      sendCommandToServer("simon: goto cabin");
      sendCommandToServer("simon: goto forest");
      sendCommandToServer("simon: goto riverbank");
      sendCommandToServer("simon: goto clearing");
      response = sendCommandToServer("simon: dig ground");
      assertTrue(response.contains("gold"), "dig ground should work becase there is shovel in the inv");
      response = sendCommandToServer("simon: look");
      assertTrue(response.contains("hole"),"dig ground should generate a hole in the location");
    }

    @Test
    void testHealthSetting(){
        String response;
        sendCommandToServer("simon: get coin");
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: open trapdoor");
        sendCommandToServer("simon: goto cellar");
        response = sendCommandToServer("simon: hit elf");
        System.out.println(response);
        assertEquals(2, (int) GameInitiate.playerStorage.get("simon").health);
        sendCommandToServer("simon: hit elf");
        sendCommandToServer("simon: drink potion");
        assertEquals(2, (int) GameInitiate.playerStorage.get("simon").health);
        sendCommandToServer("simon: hit elf");
        sendCommandToServer("simon: hit elf");
        // move to the initial start location
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("cabin"),"after lose all the health the player is not in the start location");
        response = sendCommandToServer("simon: inv");
        assertFalse(response.contains("axe"), "after lose all the health the inventory is clear");
        assertFalse(response.contains("coin"), "after lose all the health the inventory is clear");
    }

    @Test
    void testBasicCommandFailure(){
        String response;
        response = sendCommandToServer("simon: drop axe");
        assertTrue(response.contains("Cannot find"), "drop somthing should not be in the inv");
        response = sendCommandToServer("simon: goto storeroom");
        assertTrue(response.contains("not exist"),"there is not such a path leading to storeroom");
        response = sendCommandToServer("simon: blow lumberjack");
        assertTrue(response.contains("wrong"), "no such entity related to the current location");
        response = sendCommandToServer("simon: open trapdoor");
        assertTrue(response.contains("wrong"), "no such consumed item exist");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: blow horn");
        response = sendCommandToServer("simon: blow horn");
        assertTrue(response.contains("wrong"), "action already execute, not such entity in storeroom");
    }

    @Test
    void testActionFailure(){
        String response;
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: cut tree tree");
        response = sendCommandToServer("simon: look");
        assertFalse(response.contains("tree"),"multiple repeated subject input is ignored");

        sendCommandToServer("simon: get log");
        sendCommandToServer("simon: goto riverbank");
        sendCommandToServer("simon: bridge log river pls");
        response = sendCommandToServer("simon: look");
        assertTrue(response.contains("clearing"), "multiple different subject input is allowed");
        response = sendCommandToServer("simon: blow horn shovel");
        assertTrue(response.contains("wrong"),"subject belong to other action is not allowed to input");
    }

    @Test
    void testMultiplePlater(){
        String response;
        sendCommandToServer("simon: look");
        response = sendCommandToServer("dyla: look");
        assertTrue(response.contains("simon"),"player does not exist");
    }
}
