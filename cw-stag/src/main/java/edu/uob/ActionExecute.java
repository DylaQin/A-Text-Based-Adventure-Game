package edu.uob;

import java.util.*;

public class ActionExecute {

    public String executeCommand(ActionEntity gameAction, String userName) {
        try {
            // store
            storePlayer(userName);
            // execute basic command -- extract to be a function
            String entityName = gameAction.basicInfo;
            Players player = GameInitiate.playerStorage.get(userName);

            String res;
            res = executeBasicAction(gameAction, entityName, player);
            if (res==null){
                return checkActionExist(gameAction,userName);
            }
            return res;

        } catch (Exception e) {
            return "wrong!";
        }
    }

    public void storePlayer(String userName){
        TreeMap<String, Players> playerStorage = GameInitiate.playerStorage;
        if(playerStorage.containsKey(userName.toLowerCase())){
            return;
        }
        Players players = new Players(userName);
        players.location = GameInitiate.fromLocation;
        GameInitiate.playerStorage.put(userName, players);
    }

    public String executeBasicAction(ActionEntity gameAction, String entityName, Players player){
        switch (gameAction.trigger) {
            case "inv" -> {
                return invCommand(player);
            }
            case "get" -> {
                return getCommand(player, entityName);
            }
            case "drop" -> {
                return dropCommand(player, entityName);
            }
            case "goto" -> {
                return gotoCommand(entityName,player);
            }
            case "look" -> {
                return lookCommand(player);
            }
        }
        return null;
    }

    public String invCommand(Players player){
        // lists all the artefacts currently being carried by the player

        StringBuilder res = new StringBuilder();
        TreeMap<String, String> inventory = player.inventory;

        res.append("You can find the following items in your inventory:\n");
        for (Map.Entry<String,String> entity : inventory.entrySet()){
            Artefacts artefactEntity = GameInitiate.artefactStorage.get(entity.getKey());
            res.append(artefactEntity.getDescription());
            res.append("\n");
        }

        return res.toString();
    }

    public String getCommand(Players player, String entityName){
        // picks up a specified artefact from the current location and adds it into player's inventory
        String res="";
        TreeMap<String, String> inventory = player.inventory;
        boolean exist = false;
        if (player.location.artefacts.containsKey(entityName)){
            player.location.artefacts.remove(entityName);
            exist = true;
        }
        if (exist){
            inventory.put(entityName,null);
            res+="You picked up a ";
            res+=entityName;
            res+="\n";
            return res;
        }
        return "Cannot find this item in current location. Item does not exist!\n";
    }

    public String dropCommand(Players player, String entityName){
        // puts down an artefact from player's inventory and places it into the current location
        String res = "";
        TreeMap<String, String> inventory = player.inventory;
        if (inventory.containsKey(entityName)){
            inventory.remove(entityName);
            if (GameInitiate.furnitureStorage.containsKey(entityName)){
                player.location.furniture.put(entityName,null);
            }
            if (GameInitiate.characterStorage.containsKey(entityName)){
                player.location.characters.put(entityName,null);
            }
            if (GameInitiate.artefactStorage.containsKey(entityName)){
                player.location.artefacts.put(entityName,null);
            }
            res+="You dropped a ";
            res+=entityName;
            res+="\n";
        } else {
            return "Cannot find this item in your inventory. this item is not exist\n";
        }
        return res;

    }

    public String gotoCommand(String entityName,Players player){
        Locations locationEntity = player.location;
        TreeMap<String, Locations> destination = locationEntity.destination;
        for (String toLocation : destination.keySet()){
            if (!toLocation.equals(entityName)){
                continue;
            }
            player.location = GameInitiate.locationStorage.get(entityName);
            return lookCommand(player);
        }
        return "Cannot find this path. This path does not exist!\n";
    }

    public String lookCommand(Players player){
        StringBuffer res = new StringBuffer();
        Locations locationEntity = player.location;
        res.append(locationEntity.getDescription());
        res.append("\n");
        TreeMap<String,String> artefacts = locationEntity.artefacts;
        TreeMap<String,String> furnitures = locationEntity.furniture;
        TreeMap<String,String> characters = locationEntity.characters;
        for (String artefactName : artefacts.keySet()){
            Artefacts artefactEntity = GameInitiate.artefactStorage.get(artefactName);
            res.append(artefactName).append(" : ");
            res.append(artefactEntity.getDescription()).append("\n");
        }
        for (String furnitureName : furnitures.keySet()){
            Furniture furnitureEntity = GameInitiate.furnitureStorage.get(furnitureName);
            res.append(furnitureName).append(" : ");
            res.append(furnitureEntity.getDescription()).append("\n");
        }
        for (String characterName : characters.keySet()){
            Characters characterEntity = GameInitiate.characterStorage.get(characterName);
            res.append(characterName).append(" : ");
            res.append(characterEntity.getDescription()).append("\n");
        }
        StringBuilder add = lookCommandHelper(locationEntity,player);
        res.append(add);
        return res.toString();
    }

    public StringBuilder lookCommandHelper(Locations locationEntity, Players player){
        StringBuilder res = new StringBuilder();
        TreeMap<String, Locations> destinations = locationEntity.destination;

        res.append("You can access from here:\n");
        for (String destination : destinations.keySet()){
            res.append(destination).append("\n");
        }

        res.append("The other players:\n");
        for (Map.Entry<String,Players> playerSet : GameInitiate.playerStorage.entrySet()){
            if (player!=playerSet.getValue()){
                res.append(playerSet.getKey()).append(" : \n").append(playerSet.getValue().location.getName());
                res.append("\n");
            }
        }
        return res;
    }

        /* below is the action command execution --*/
    public String checkActionExist(ActionEntity gameAction, String userName){
        // all entities should only be one
        StringBuilder res = new StringBuilder();
        Players player = GameInitiate.playerStorage.get(userName);
        Locations currentLocation = player.location;
        TreeMap<String, String> inv = player.inventory;
        // check if there exist such entity
        for (String compare:gameAction.subjectList){
            if (checkSubjectValid(compare, currentLocation,inv,gameAction)){
                continue;
            }
            return "Something wrong!";
        }
        // check if the command not belong to other action's usbject list
//        if (!executeSubjectList()){
//            return "Something wrong!";
//        }
        // execute action command
        if (executeConsumeCommand(gameAction,player) && executeProduceCommand(player,gameAction)){
            res.append(gameAction.narration).append("\n");
        } else {
            res.append("Something wrong!");
        }
        return res.toString();
    }

    public boolean checkSubjectValid(String compare, Locations currentLocation,TreeMap<String, String> inv, ActionEntity gameAction){
        boolean res = false;
        if(currentLocation.artefacts.containsKey(compare) ||
                currentLocation.characters.containsKey(compare) ||
                currentLocation.furniture.containsKey(compare) ||
                inv.containsKey(compare)){
            res = true;
        }
//        ActionEntity action = GameInitiate.actionStorage.get(gameAction.trigger);
//        // compare the command to the action's subjects list
//        for (int i=0;i<GameInitiate.actionStorage.size();i++){
//            if (action.subjectList.contains(compare)){
//                continue;
//            }
//            if (!action.subjectList.contains(compare) && GameInitiate.actionStorage.get(i).subjectList.contains(compare)){
//                res = false;
//            }
//        }
        return res;
    }

//    public void executeSubjectList(ActionEntity gameAction, String compare){
//        boolean
//        ActionEntity action = GameInitiate.actionStorage.get(gameAction.trigger);
//        // compare the command to the action's subjects list\
//        for (int i=0;i<GameInitiate.actionStorage.size();i++){
//            if (!action.subjectList.contains(compare) && GameInitiate.actionStorage.get(i).subjectList.contains(compare)){
//                res = false;
//            }
//        }
//        return res;
//    }

    public boolean executeConsumeCommand(ActionEntity gameAction, Players player){
        // case 1 -- move entity from inventory to storeRoom (artefact)
        // case 2 -- move from curLocation to storeRoom (furniture)
        // case 3 -- health reduce
        TreeMap<String, String> inv = player.inventory;
        for (String entity:gameAction.consumedList){
            if (entity.equals("health")){
                setPlayerHealth(player);
                continue;
            }
            if (inv.containsKey(entity)){
                inv.remove(entity);
                putEntitytoStoreroom(entity);
                continue;
            }
            if (player.location.furniture.containsKey(entity)){
                player.location.furniture.remove(entity);
                putEntitytoStoreroom(entity);
                continue;
            }
            return false;
        }
        return true;
    }

    public void setPlayerHealth(Players player){

        if (player.health>1){
            player.health--;
            return;
        }
        // if health < 1 then
        // player lost all the artefact in the inv to the current place
        for (String discardName : player.inventory.keySet()){
            player.location.artefacts.put(discardName,null);
        }
        // player sent to the start location and health recover to 3
        player.location = GameInitiate.fromLocation;
        player.health=3;
        player.inventory=new TreeMap<>();
    }

    public void putEntitytoStoreroom(String entityName){
        TreeMap<String, Locations> locationStorage = GameInitiate.locationStorage;
        Locations storeRoom = locationStorage.get("storeroom");
        storeRoom.artefacts.put(entityName,null);
    }


    public boolean executeProduceCommand(Players player,ActionEntity gameAction){
        // case 1 -- increase health
        // case 2 -- entity from storeroom, store in current location
        // case 3 -- path created
        for (String entityName:gameAction.producedList){
            if (entityName.equals("health") && player.health<3){
                player.health++;
                continue;
            }
            if (GameInitiate.locationStorage.containsKey(entityName)){
                // path generated
                Locations path = GameInitiate.locationStorage.get(entityName);
                player.location.destination.put(entityName,path);
                continue;
            }
            if (!player.location.artefacts.containsKey(entityName) &&
                    !player.location.furniture.containsKey(entityName) &&
                    !player.location.characters.containsKey(entityName)){
                // create entity based on storeRoom
                pickEntityfrST(entityName,player);
                continue;
            }
            return false;
        }
        return true;
    }

    public void pickEntityfrST(String entityName, Players player){
        Locations storeRoom = GameInitiate.locationStorage.get("storeroom");
        if(storeRoom.furniture.containsKey(entityName)){
            player.location.furniture.put(entityName,null);
        }
        if(storeRoom.artefacts.containsKey(entityName)){
            player.location.artefacts.put(entityName,null);
        }
        if(storeRoom.characters.containsKey(entityName)){
            player.location.characters.put(entityName,null);
        }
    }
}
