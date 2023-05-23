package edu.uob;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class CommandParser {
    ArrayList<String> keyWord = new ArrayList<>();

    public ActionEntity parseCommand(String[] commandTokens){
        try{
            initiateKeyWords();
            ActionEntity gameAction;
            // trim invalid command
            ArrayList<String> commandList = trimInvalidWords(commandTokens);
            if (commandList==null){
                return null;
            }
            // parse basic command
            if (parseBasicCommand(commandList)){
                gameAction = getBasicInfo(commandList);
            } else {
                // parse action, allow repetition so convert to hashSet
                // one command can only execute once
                HashSet<String> commandSet = new HashSet<>(commandList);
                gameAction = parseActionCommand(commandSet);

                // if command has entity belong to other action's subjectList, false
                if (!checkSubjectValid(gameAction, commandSet)){
                    return null;
                }
            }
            return gameAction;

        } catch (Exception e){
            return null;
        }
    }

    public void initiateKeyWords(){
        keyWord.addAll(GameInitiate.artefactStorage.keySet());
        keyWord.addAll(GameInitiate.locationStorage.keySet());
        keyWord.addAll(GameInitiate.characterStorage.keySet());
        keyWord.addAll(GameInitiate.furnitureStorage.keySet());
        keyWord.addAll(GameInitiate.actionStorage.keySet());
        keyWord.addAll(GameInitiate.playerStorage.keySet());
    }

    public ArrayList<String> trimInvalidWords(String[] commandTokens){
        // trim invalidate words

        ArrayList<String> commandList = new ArrayList<>();
        for (String command: commandTokens){
            if (keyWord.contains(command)){
                commandList.add(command);
            } else if (GameInitiate.basicCommand.contains(command)){
                commandList.add(command);
            }
        }

        return commandList;
    }

    public boolean parseBasicCommand(ArrayList<String> commands){
        // basic command is stricter, only one built-in command exist in one line

        // case 1: only one command -- inv/look
        if (commands.get(0).equalsIgnoreCase("look") || commands.get(0).equalsIgnoreCase("inv") || commands.get(0).equalsIgnoreCase("inventory")){
            return (commands.size()==1);
        }
        String entityName = commands.get(1);
        // case 2: check artefactName exist -- get/drop
        if (commands.get(0).equalsIgnoreCase("get") || commands.get(0).equalsIgnoreCase("drop")){
            return GameInitiate.artefactStorage.containsKey(entityName);
        }
        // case 3: goto location Name
        if (commands.get(0).equalsIgnoreCase("goto")){
            return GameInitiate.locationStorage.containsKey(entityName);
        }
        return false;
    }

    public ActionEntity getBasicInfo(ArrayList<String> commands){
        ActionEntity gameAction = new ActionEntity();

        switch (commands.get(0)) {
            case "inv", "inventory" -> gameAction.trigger = "inv";
            case "get" -> {
                gameAction.trigger = "get";
                gameAction.basicInfo = commands.get(1);
            }
            case "drop" -> {
                gameAction.trigger = "drop";
                gameAction.basicInfo = commands.get(1);
            }
            case "goto" -> {
                gameAction.trigger = "goto";
                gameAction.basicInfo = commands.get(1);
            }
            case "look" -> gameAction.trigger = "look";
            default -> gameAction.trigger = "action";
        }

        return gameAction;
    }

    public ActionEntity parseActionCommand(HashSet<String> commands){
        // only allowed one action exist, or two action with the same meaning/narration, so check the action triggers first
        int countActionNum = 0;
        ActionEntity gameAction = new ActionEntity();
        ArrayList<ActionEntity> gameActionList = new ArrayList<>();

        for (String command: commands){
            if (GameInitiate.actionStorage.containsKey(command)){
                countActionNum++;
                gameAction = GameInitiate.actionStorage.get(command);
                gameActionList.add(gameAction);
            }
        }
        // check if the multiple action is the same one
        if (countActionNum==2){
            matchAction(gameActionList);
            if (!matchAction(gameActionList)){
                return null;
            }
        }
        if (countActionNum>2){
            return null;
        }

        // based on the action, loop through the command check whether required entity exist
        int length = gameAction.subjectList.size();
        boolean isContain = false;
        for (int i=0;i<length;i++){
            if (commands.contains(gameAction.subjectList.get(i))) {
                isContain = true;
                break;
            }
        }
        if (!isContain){
            return null;
        }

        return gameAction;
    }

    public boolean matchAction(ArrayList<ActionEntity> actionList){
        ActionEntity gameAction1 = actionList.get(0);
        String narration1 = gameAction1.narration;
        ActionEntity gameAction2 = actionList.get(1);
        String narration2 = gameAction2.narration;

        return narration1.equals(narration2);
    }

    public boolean checkSubjectValid(ActionEntity gameAction, HashSet<String> commandSet){
        // if command has subject doest belong to the action correspond trigger
        ArrayList<String> triggerList = getSameTriggerAction(gameAction.narration);
        for (String command: commandSet){

            for (Map.Entry<String,ActionEntity> actionMap: GameInitiate.actionStorage.entrySet()){
                if (triggerList.contains(actionMap.getKey())){
                    continue;
                }
                if (actionMap.getValue().subjectList.contains(command) && !gameAction.subjectList.contains(command)){
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<String> getSameTriggerAction(String narration){
        ArrayList<String> triggerList = new ArrayList<>();
        for (Map.Entry<String,ActionEntity> actionMap : GameInitiate.actionStorage.entrySet()){
            if (actionMap.getValue().narration.equals(narration)){
                triggerList.add(actionMap.getValue().trigger);
            }
        }
        return triggerList;
    }
}


