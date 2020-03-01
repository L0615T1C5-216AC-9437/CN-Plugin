package CN;

//-----imports-----//
import arc.Core;
import arc.Events;
import arc.struct.Array;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.core.NetClient;
import mindustry.entities.type.Player;
import mindustry.entities.type.Unit;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.net.Administration;
import mindustry.plugin.Plugin;
import mindustry.plugin.*;
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.type.BaseUnit;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static mindustry.Vars.*;


public class Main extends Plugin {
    public static Array<String> GOW = new Array<>();
    public static Array<String> IW = new Array<>();
    public static HashMap<String, String> buffList = new HashMap<>();
    public static HashMap<String, pi> database = new HashMap<>();

    private boolean summonEnable = true;
    private boolean reaperEnable = true;
    private boolean lichEnable = true;
    private boolean eradicatorEnable = true;
    private boolean buffEnable = true;
    private String mba = "[white]You must be [scarlet]<Admin> [white]to use this command.";
    private boolean autoBan = true;
    private boolean sandbox = false;
    public Main() throws InterruptedException {

        Thread PIAS = new Thread() {
            public void run() {
                Log.info("PIAS started Successfully!");
                while (true) {
                    try {
                        Thread.sleep(60 * 1000);

                        //output save file
                        try {
                            FileOutputStream fileOut = new FileOutputStream("PDF.cn");
                            ObjectOutputStream out = new ObjectOutputStream(fileOut);
                            out.writeObject(Main.database);
                            out.close();
                            fileOut.close();
                        } catch (IOException i) {
                            i.printStackTrace();
                        }

                        //add 1 minute of play time for each player
                        for (Player p : playerGroup.all()) {
                            if (Main.database.containsKey(p.uuid)) {
                                Main.database.get(p.uuid).addTP(1);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        //load all player info.
        try {
            FileInputStream loadFile = new FileInputStream("PDF.cn");
            ObjectInputStream in = new ObjectInputStream(loadFile);
            database = (HashMap<String, pi>) in.readObject();
            in.close();
            loadFile.close();
            Log.info("Successfully loaded player info.");
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("PlayerInfo class not found");
            c.printStackTrace();
            return;
        }
        //PIAS Start
        Log.info("Attempting to start PIAS...");
        PIAS.start();

        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            if (autoBan) {
                if (player.getInfo().timesKicked > (player.getInfo().timesJoined / 5)) {
                    String playerID = player.getInfo().id;
                    netServer.admins.banPlayer(playerID);
                    Log.info("[B] Banned \"{0}\" [{1}] for (Kick) > (join)/5", player.name, playerID);
                    player.con.kick("Banned for being kicked most of the time. If you want to appeal, give the previous as reason.");
                } else if (player.getInfo().timesKicked > 15) {
                    String playerID = player.getInfo().id;
                    netServer.admins.banPlayer(playerID);
                    Log.info("[B] Banned \"{0}\" [{1}] for Kick > 15.", player.name, playerID);
                    player.con.kick("Banned for being kicked than 15. If you want to appeal, give the previous as reason.");
                }
            }
            if(player.getInfo().timesKicked == 10) {
                Call.onInfoMessage(player.con,"You've been kicked 10 times, 15 kicks and you're banned.");
            }

            //Join Message
            player.sendMessage("======================================================================" +
                    "\nWelcome to Chaotic Neutral!" +
                    "\nConsider joining our discord \uE848 through [lightgray]https://cn-discord.ddns.net [white]or using discord code [lightgray]xQ6gGfQ" +
                    "\n\nWe have a few useful commands, do /help to see them." +
                    "\nFor \uE801 Info, do /info");
            //Remove fake <> in name
            player.name = player.name.replaceFirst("\\<(.*)\\>", "");

            if (database.containsKey(player.uuid)) {
                //Admin/Mod
                switch (database.get(player.uuid).getRank()) {
                    case 6:
                        player.name = player.name + " [accent]<[scarlet]\uE814[accent]>";
                        break;
                    case 5:
                        player.name = player.name + " [accent]<[royal]\uE828[accent]>";
                        break;
                    case 4:
                        player.name = player.name + " [accent]<[lightgray]\uE80F[accent]>";
                        break;
                }

                //Verified Icon
                if (database.containsKey(player.uuid)) {
                    if (database.get(player.uuid).getVerified()) {
                        player.name = player.name + " [accent]<[sky]\uE848[accent]>";
                    }

                }
            } else {
                Call.sendMessage("[white]Welcome " + player.name + ", [white]first time on the server!");
                database.put(player.uuid, new pi());
            }

        });

        Events.on(EventType.WorldLoadEvent.class, event -> {
            IW.clear();
            GOW.clear();
            buffList.clear();
            sandbox = false;

            if(state.rules.infiniteResources) {
                sandbox = true;
                state.wave=2222;
            }
        });

        Events.on(EventType.WaveEvent.class, event -> {
            //Sandbox
            if(sandbox && state.wave!=2222) state.wave=2222;
        });

        Events.on(EventType.GameOverEvent.class, event -> {
            for (Player p : playerGroup.all()) {
                if (database.containsKey(p.uuid)) {
                    database.get(p.uuid).addGP();
                    Call.onInfoToast(p.con,"Games Played: +1",10);
                }
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        handler.register("cr","<uuid> <rank>", "Changes player rank through uuid.", arg -> {
            String pid= arg[1].replaceAll("[^0-9]", "");
            if (pid.equals("")) {
                Log.err("Rank must contain numbers!");
                return;
            }
            if (netServer.admins.getInfo(arg[0]).timesJoined > 0) {
                int pr = database.get(arg[0]).getRank();
                database.get(arg[0]).changeRank(Integer.parseInt(pid));
                Log.info("[R] Changed rank for {0} from {1} to {2}.", arg[0], pr, database.get(arg[0]).getRank());
            } else {
                player.sendMessage("Player not found!");
            }
        });

        handler.register("gnpdf", "List all thorium reactors in the map.", arg -> {
            database.clear();
            database.put("TEST", new pi());
            try {
                FileOutputStream fileOut = new FileOutputStream("PDF-new.cn");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(database);
                out.close();
                fileOut.close();
                player.sendMessage("done");
            } catch (IOException i) {
                i.printStackTrace();
            }
        });

        /*

         */
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        //-----USERS-----//
;
        //Summons Entities
        handler.<Player>register("summon","[unit] [Info]", "Summons a [royal]Unit [lightgray]at a cost. do /summon reaper info", (arg, player) -> {
            String unit = "none";
            //decider section
            if (arg.length != 0) {
                switch (arg[0]) {
                    case "reaper":
                        if (arg.length == 2) {
                            if (player.isAdmin) {
                                switch (arg[1]) {
                                    case "on":
                                        reaperEnable = true;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Reaper [white]turned [lightgray]on[white].");
                                        break;
                                    case "off":
                                        reaperEnable = false;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Reaper [white]turned [lightgray]off[white].");
                                        break;
                                    case "info":
                                        Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n5k \uF838 [#d99d73]copper\n[white]5k \uF837 [#8c7fa9]lead\n[white]4k \uF832 [#8da1e3]titanium[white]\n3.5k \uF831 [#f9a3c7]thorium[white]\n2k \uF82F [#53565c]Silicon[white]\n1.5k \uF82E [#cbd97f]plastanium[white]\n500 \uF82D [#f4ba6e]Phase fabric[white]\n1.25k \uF82C [#f3e979]Surge Alloy");
                                        break;
                                    default:
                                        player.sendMessage("[salmon]Summon[white]: Reaper args contains [lightgray]on[white]/[lightgray]off[white].");
                                        break;
                                }
                            } else if (arg[1].equals("info")){
                                Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n5k \uF838 [#d99d73]copper\n[white]5k \uF837 [#8c7fa9]lead\n[white]4k \uF832 [#8da1e3]titanium[white]\n3.5k \uF831 [#f9a3c7]thorium[white]\n2k \uF82F [#53565c]Silicon[white]\n1.5k \uF82E [#cbd97f]plastanium[white]\n500 \uF82D [#f4ba6e]Phase fabric[white]\n1.25k \uF82C [#f3e979]Surge Alloy");
                                return;
                            } else {
                                player.sendMessage(mba);
                            }
                        } else {
                            unit = "UnitTypes.reaper";
                        }
                        break;
                    case "lich":
                        if (arg.length == 2) {
                            if (player.isAdmin) {
                                switch (arg[1]) {
                                    case "on":
                                        lichEnable = true;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Lich [white]turned [lightgray]on[white].");
                                        break;
                                    case "off":
                                        lichEnable = false;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Lich [white]turned [lightgray]off[white].");
                                        break;
                                    case "info":
                                        Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n3.5k \uF838 [#d99d73]copper\n[white]3.5k \uF837 [#8c7fa9]lead\n[white]2k \uF836 [#ebeef5]metaglass[white]\n[white]1.3k \uF835 [#b2c6d2]graphite[white]\n[white]1.3k \uF832 [#8da1e3]titanium[white]\n1.5k \uF831 [#f9a3c7]thorium[white]\n1.3k \uF82F [#53565c]Silicon[white]\n500 \uF82E [#cbd97f]plastanium[white]\n500 \uF82C [#f3e979]Surge Alloy");
                                        break;
                                    default:
                                        player.sendMessage("[salmon]Summon[white]: Lich args contains [lightgray]on[white]/[lightgray]off[white].");
                                        break;
                                }
                            } else if (arg[1].equals("info")){
                                Call.onInfoMessage(player.con,"[accent]Resources needed[white]:" +
                                        "\n[white]3.5k \uF838 [#d99d73]copper" +
                                        "\n[white]3.5k \uF837 [#8c7fa9]lead" +
                                        "\n[white]2k \uF836 [#ebeef5]metaglass" +
                                        "\n[white]1.3k \uF835 [#b2c6d2]graphite" +
                                        "\n[white]1.3k \uF832 [#8da1e3]titanium" +
                                        "\n[white]1.5k \uF831 [#f9a3c7]thorium" +
                                        "\n[white]1.3k \uF82F [#53565c]Silicon" +
                                        "\n[white]500 \uF82E [#cbd97f]plastanium" +
                                        "\n[white]500 \uF82C [#f3e979]Surge Alloy");
                                return;
                            } else {
                                player.sendMessage(mba);
                            }
                        } else {
                            unit = "UnitTypes.lich";
                        }
                        break;

                    case "eradicator":
                        if (arg.length == 2) {
                            if (player.isAdmin) {
                                switch (arg[1]) {
                                    case "on":
                                        eradicatorEnable = true;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Reaper [white]turned [lightgray]on[white].");
                                        break;
                                    case "off":
                                        eradicatorEnable = false;
                                        player.sendMessage("[salmon]Summon[white]: [lightgray]Reaper [white]turned [lightgray]off[white].");
                                        break;
                                    case "info":
                                        Call.onInfoMessage(player.con,"[accent]Resources needed[white]:" +
                                        "\n5k \uF838 [#d99d73]copper" +
                                        "\n[white]3.5k \uF837 [#8c7fa9]lead"+
                                        "\n[white]2k \uF836 [#ebeef5]metaglass[white]"+
                                        "\n[white]1.8k \uF835 [#b2c6d2]graphite[white]"+
                                        "\n[white]3.5k \uF832 [#8da1e3]titanium[white]"+
                                        "\n4k \uF831 [#f9a3c7]thorium[white]"+
                                        "\n2.5k \uF82F [#53565c]Silicon[white]"+
                                        "\n1k  \uF82E [#cbd97f]plastanium[white]"+
                                        "\n350 \uF82D [#f4ba6e]Phase fabric[white]"+
                                        "\n750 \uF82C [#f3e979]Surge Alloy");
                                        break;
                                    default:
                                        player.sendMessage("[salmon]Summon[white]: Eradicator args contains [lightgray]on[white]/[lightgray]off[white].");
                                        break;
                                }
                            } else if (arg[1].equals("info")){
                                Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n5k \uF838 [#d99d73]copper\n[white]3.5k \uF837 [#8c7fa9]lead\n[white]2k \uF836 [#ebeef5]metaglass[white]\n[white]1.8k \uF835 [#b2c6d2]graphite[white]\n[white]3.5k \uF832 [#8da1e3]titanium[white]\n4k \uF831 [#f9a3c7]thorium[white]\n2.5k \uF82F [#53565c]Silicon[white]\n1k \uF82E [#cbd97f]plastanium[white]\n350 \uF82D [#f4ba6e]Phase fabric[white]\n750 \uF82C [#f3e979]Surge Alloy");
                                return;
                            } else {
                                player.sendMessage(mba);
                            }
                        } else {
                            unit = "UnitTypes.eradicator";
                        }
                        break;

                    case "on":
                        if (player.isAdmin) {
                            summonEnable = true;
                            player.sendMessage("[salmon]Summon[white]: [lightgray]Summon [white]turned [lightgray]on[white].");
                        } else {
                            player.sendMessage(mba);
                        }
                        return;
                    case "off":
                        if (player.isAdmin) {
                            summonEnable = false;
                            player.sendMessage("[salmon]Summon[white]: [lightgray]Summon [white]turned [lightgray]off[white].");
                        } else {
                            player.sendMessage(mba);
                        }
                        return;
                    default:
                        if (player.isAdmin) {
                            player.sendMessage("Summon: arg[0] = reaper, lich, on or off.");
                        } else {
                            player.sendMessage("Summon: arg[0] = reaper or lich.");
                        }
                        return;
                }
            } else {
                player.sendMessage("[salmon]Summon[white]: Summons a [royal]Reaper [white]or [royal]Lich.");
                return;
            }
            //Summon section
            if (!unit.equals("none") && summonEnable) {
                Teams.TeamData teamData = state.teams.get(player.getTeam());
                CoreBlock.CoreEntity core = teamData.cores.first();
                if (core == null) {
                    player.sendMessage("Your team doesn't have a core.");
                    return;
                }
                //over land?
                /*
                int sx=0;
                int sy=0;
                boolean solid = true;
                for (sx=0; sx <= world.getMap().width; sx++) {
                    for (sy=0; sy <= world.getMap().width; sy++) {
                        Teams.TeamData teamN = state.teams.get(world.tile(sx,sy).getTeam());
                        if (!world.tile(sx,sy).solid()) solid = false;
                    }
                }   */
                boolean solid = false;
                int x = (int) player.x;
                int y = (int) player.y;
                if (world.tile(x/8,y/8).solid()) {
                    solid = true;
                }
                switch (unit) {
                    case "UnitTypes.reaper":
                        if (reaperEnable) {
                            if (core.items.has(Items.copper, 5000) && core.items.has(Items.lead, 5000) && core.items.has(Items.titanium, 4000) && core.items.has(Items.thorium, 3500) && core.items.has(Items.silicon, 2000) && core.items.has(Items.plastanium, 1500) && core.items.has(Items.phasefabric, 500) && core.items.has(Items.surgealloy, 1250)) {
                                //
                                core.items.remove(Items.copper, 5000);
                                core.items.remove(Items.lead, 5000);
                                core.items.remove(Items.titanium, 4000);
                                core.items.remove(Items.thorium, 3500);
                                core.items.remove(Items.silicon, 2000);
                                core.items.remove(Items.plastanium, 1500);
                                core.items.remove(Items.phasefabric, 500);
                                core.items.remove(Items.surgealloy, 1250);
                                //
                                BaseUnit baseUnit = UnitTypes.reaper.create(player.getTeam());
                                baseUnit.set(player.x, player.y);
                                baseUnit.add();
                                ;
                                Call.sendMessage("[white]" + player.name + "[white] has summoned a [royal]Reaper[white].");
                            } else {
                                Call.sendMessage("[salmon]Summon[white]: " + player.name + "[lightgray] tried[white] to summon a [royal]Reaper[white].");
                                player.sendMessage("[salmon]Summon[white]: Not enough resources to spawn [royal]Reaper[white]. Do [lightgray]`/summon reaper info` [white]to see required resources.");
                            }
                        } else {
                            player.sendMessage("[salmon]Summon[white]: [royal]Reaper [white]is disabled.");
                        }
                        break;
                    case "UnitTypes.lich":
                        if (lichEnable) {
                            if (core.items.has(Items.copper, 3500) && core.items.has(Items.lead, 3500) && core.items.has(Items.metaglass, 2000) && core.items.has(Items.graphite, 1250) && core.items.has(Items.titanium, 2500) && core.items.has(Items.thorium, 1750) && core.items.has(Items.silicon, 1250) && core.items.has(Items.plastanium, 500) && core.items.has(Items.surgealloy, 350)) {
                                //
                                core.items.remove(Items.copper, 3500);
                                core.items.remove(Items.lead, 3500);
                                core.items.remove(Items.metaglass, 2000);
                                core.items.remove(Items.graphite, 1250);
                                core.items.remove(Items.titanium, 1250);
                                core.items.remove(Items.thorium, 1500);
                                core.items.remove(Items.silicon, 1250);
                                core.items.remove(Items.plastanium, 500);
                                core.items.remove(Items.surgealloy, 350);
                                //
                                BaseUnit baseUnit = UnitTypes.lich.create(player.getTeam());
                                baseUnit.set(player.x, player.y);
                                baseUnit.add();
                                ;
                                Call.sendMessage("[white]" + player.name + "[white] has summoned a [royal]Lich[white].");
                            } else {
                                Call.sendMessage("[salmon]Summon[white]: " + player.name + "[lightgray] tried[white] to summon a [royal]Lich[white].");
                                player.sendMessage("[salmon]Summon[white]: Not enough resources to spawn [royal]Lich[white]. Do [lightgray]`/summon lich info` [white]to see required resources.");
                            }
                        } else {
                            player.sendMessage("[salmon]Summon[white]: [royal]Lich [white]is disabled.");
                        }
                        break;

                    case "UnitTypes.eradicator":
                        if (eradicatorEnable) {
                            if (!solid && core.items.has(Items.copper, 5000) && core.items.has(Items.lead, 3500) && core.items.has(Items.metaglass, 2000) && core.items.has(Items.graphite, 1750) && core.items.has(Items.titanium, 3500) && core.items.has(Items.thorium, 4000) && core.items.has(Items.silicon, 2500) && core.items.has(Items.plastanium, 1000) && core.items.has(Items.phasefabric, 350) && core.items.has(Items.surgealloy, 750)) {
                                //
                                core.items.remove(Items.copper, 5000);
                                core.items.remove(Items.lead, 3500);
                                core.items.remove(Items.metaglass, 2000);
                                core.items.remove(Items.graphite, 1750);
                                core.items.remove(Items.titanium, 3500);
                                core.items.remove(Items.thorium, 4000);
                                core.items.remove(Items.silicon, 2500);
                                core.items.remove(Items.plastanium, 1000);
                                core.items.remove(Items.phasefabric, 350);
                                core.items.remove(Items.surgealloy, 750);
                                //
                                BaseUnit baseUnit = UnitTypes.eradicator.create(player.getTeam());
                                baseUnit.set(player.x, player.y);
                                baseUnit.add();

                                Call.sendMessage("[white]" + player.name + "[white] has summoned a [royal]Eradicator[white].");

                            } else if (solid){
                                player.sendMessage("[salmon]Summon[white]: Can't Summon land unit over [lightgray]Solid [white]surface.");
                            } else {
                                Call.sendMessage("[salmon]Summon[white]: " + player.name + "[lightgray] tried[white] to summon a [royal]Eradicator[white].");
                                player.sendMessage("[salmon]Summon[white]: Not enough resources to spawn [royal]Eradicator[white]. Do [lightgray]`/summon eradicator info` [white]to see required resources.");
                            }
                        } else {
                            player.sendMessage("[salmon]Summon[white]: [royal]Eradicator [white]is disabled.");
                        }
                        break;

                    default:
                        player.sendMessage("ERROR");
                        break;
                }
            } else if (arg.length == 1){
                player.sendMessage("[salmon]Summon[white]: [salmon]Summon[white] is disabled.");
            }
        });
        //Shows team info
        handler.<Player>register("myteam","[Info]", "Gives team info", (arg, player) -> {
            Teams.TeamData teamData = state.teams.get(player.getTeam());
            CoreBlock.CoreEntity core = teamData.cores.first();
            if (core == null) {
                player.sendMessage("Your team doesn't have a core.");
                return;
            }
            String playerTeam = player.getTeam().name;
            switch (playerTeam) {
                case "sharded":
                    playerTeam = "[accent]" + playerTeam;
                    break;
                case "crux":
                    playerTeam = "[scarlet]" + playerTeam;
                    break;
                case "blue":
                    playerTeam = "[royal]" + playerTeam;
                    break;
                case "derelict":
                    playerTeam = "[gray]" + playerTeam;
                    break;
                case "green":
                    playerTeam = "[lime]" + playerTeam;
                    break;
                case "purple":
                    playerTeam = "[purple]" + playerTeam;
                    break;
            }
            //
            int draug = 0;
            int spirit = 0;
            int phantom = 0;
            int dagger = 0;
            int crawler = 0;
            int titan = 0;
            int fortress = 0;
            int wraith = 0;
            int ghoul = 0;;
            int revenant = 0;
            int lich = 0;
            int reaper = 0;
            int eradicator = 0;
            int All = 0;
            //
            for (Unit u : unitGroup.all()) {
                if(u.getTeam() == player.getTeam()) {
                    if (u.getTypeID().name.equals("draug")) draug = draug + 1;
                    if (u.getTypeID().name.equals("spirit")) spirit = spirit + 1;
                    if (u.getTypeID().name.equals("phantom")) phantom = phantom + 1;
                    if (u.getTypeID().name.equals("dagger")) dagger = dagger + 1;
                    if (u.getTypeID().name.equals("crawler")) crawler = crawler + 1;
                    if (u.getTypeID().name.equals("titan")) titan = titan + 1;
                    if (u.getTypeID().name.equals("fortress")) fortress = fortress + 1;
                    if (u.getTypeID().name.equals("wraith")) wraith = wraith + 1;
                    if (u.getTypeID().name.equals("ghoul")) ghoul = ghoul + 1;
                    if (u.getTypeID().name.equals("revenant")) revenant = revenant + 1;
                    if (u.getTypeID().name.equals("lich")) lich = lich + 1;
                    if (u.getTypeID().name.equals("reaper")) reaper = reaper + 1
                    if (u.getTypeID().name.equals("eradicator")) eradicator = eradicator + 1;
                    All = All + 1;
                }
            }

            Call.onInfoMessage(player.con,
                    "Your team is " + playerTeam +
                        "\n\n[accent]Core Resources[white]:" +
                            "\n[white]" + core.items.get(Items.copper) +        " \uF838 [#d99d73]copper" +
                            "\n[white]" + core.items.get(Items.lead) +          " \uF837 [#8c7fa9]lead" +
                            "\n[white]" + core.items.get(Items.metaglass) +     " \uF836 [#ebeef5]metaglass" +
                            "\n[white]" + core.items.get(Items.graphite) +      " \uF835 [#b2c6d2]graphite" +
                            "\n[white]" + core.items.get(Items.titanium) +      " \uF832 [#8da1e3]titanium" +
                            "\n[white]" + core.items.get(Items.thorium) +       " \uF831 [#f9a3c7]thorium" +
                            "\n[white]" + core.items.get(Items.silicon) +       " \uF82F [#53565c]Silicon" +
                            "\n[white]" + core.items.get(Items.plastanium) +    " \uF82E [#cbd97f]plastanium" +
                            "\n[white]" + core.items.get(Items.phasefabric) +   " \uF82D [#f4ba6e]phase fabric" +
                            "\n[white]" + core.items.get(Items.surgealloy) +    " \uF82C [#f3e979]surge alloy" +
                        "\n\n[accent]Team Units: [white]" +
                            "\nDraug Miner Drone: " + draug +
                            "\nSpirit Repair Drone: " + spirit +
                            "\nPhantom Builder Drone: " + phantom +
                            "\n Dagger: " + dagger +
                            "\nCrawlers: " + crawler +
                            "\nTitan: " + titan +
                            "\nFortress: " + fortress +
                            "\nWraith Fighter: " + wraith +
                            "\nGhoul Bomber: " + ghoul +
                            "\nRevenant: " + revenant +
                            "\nLich: " + lich +
                            "\nReaper: " + reaper +
                            "\nEradicator: " + eradicator +
                            "\nTotal: " + All +
                            "\n");
        });
        //Lists players and their respective id's
        handler.<Player>register("players", "List of people and ID.", (args, player) -> {
            StringBuilder builder = new StringBuilder();
            builder.append("[accent]List of players: \n");
            for (Player p : playerGroup.all()) {
                String name = p.name;
                if(p.isAdmin) {
                    builder.append("[white]>>> \uE828 [lightgray]");
                } else{
                    name = name.replaceAll("\\[", "[[");
                    builder.append("[white]");
                }
                builder.append(name).append("[accent] : [lightgray]").append(p.id).append("\n");
            }
            player.sendMessage(builder.toString());
        });
        //Buffs all players. TODO: Make it only affect one player.
        /*
        handler.<Player>register("buff","[I/O]", "Buffs player.", (arg, player) -> {
            if (arg.length == 1) {
                if (player.isAdmin) {
                    if (arg[0].equals("on")) {
                        buffEnable = true;
                        player.sendMessage("Buff turned [lightgray]on[white].");
                    } else if (arg[0].equals("off")) {
                        buffEnable = false;
                        player.sendMessage("Buff turned [lightgray]off[white].");
                    } else {
                        player.sendMessage("Arg must be on or off");
                    }
                } else {
                    player.sendMessage(mba);
                }
                return;
            }
            if (!buffEnable) {
                player.sendMessage("Buff is disabled.");
                return;
            }
            boolean buff = false;
            if (buffList.containsKey(player.uuid)) {
                if (buffList.get(player.uuid).equals(player.mech.name)) {
                    player.sendMessage("As you [scarlet]OverDrive[white], a anomaly causes all primary systems to fail.");
                    player.dead = true;
                    buff = false;
                } else {
                    buffList.remove(player.uuid);
                    buffList.put(player.uuid,player.mech.name);
                    buff = true;
                }
            } else {
                buffList.put(player.uuid,player.mech.name);
                buff = true;
            }

            if (buff) {

                switch (player.mech.name) {
                    case "alpha-mech":
                        player.mech.health = 350f;
                        player.mech.buildPower = 1.75f;
                        player.mech.speed = 0.75f;
                        player.mech.weapon.bullet.damage = 12.0f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      ALPHA -----" + "\n-- HEALTH: 250 -> 350 -" + "\n-- BUILD:     1.2 -> 1.75  ---" + "\n-- SPEED:    0.5 -> 0.75  -" + "\n-- DAMAGE: 9.0 -> 12.0  -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                    case "delta-mech":
                        player.mech.health = 275f;
                        player.mech.buildPower = 1.35f;
                        player.mech.speed = 1.0f;
                        player.mech.weapon.bullet.damage = 15.0f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      DELTA -----" + "\n-- HEALTH: 150 -> 275   -" + "\n-- BUILD:     0.9 -> 1.35    -" + "\n-- SPEED:    0.75 -> 1.0   -" + "\n-- DAMAGE: 12.0 -> 15.0 -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                    case "tau-mech":
                        player.mech.health = 325;
                        player.mech.buildPower = 2.25f;
                        player.mech.speed = 0.75f;
                        player.mech.weapon.bullet.damage = 15.0f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      TAU --------" + "\n-- HEALTH: 200 -> 325 -" + "\n-- BUILD:     1.6 -> 2.25   --" + "\n-- SPEED:    0.4 -> 0.75  -" + "\n-- DAMAGE: 13.0 -> 15.0 -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                    case "omega-mech":
                        player.mech.health = 425f;
                        player.mech.buildPower = 1.75f;
                        player.mech.speed = 0.65f;
                        player.mech.weapon.bullet.damage = 15.0f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      OMEGA -----" + "\n-- HEALTH: 350 -> 425 -" + "\n-- BUILD:     1.5 -> 1.75  ---" + "\n-- SPEED:    0.4 -> 0.65  -" + "\n-- DAMAGE: 12.0 -> 15.0 -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                    case "dart-ship":
                        player.mech.health = 325f;
                        player.mech.buildPower = 1.85f;
                        player.mech.speed = 0.85f;
                        player.mech.weapon.bullet.damage = 12.0f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      DART  ------" + "\n-- HEALTH: 200 -> 325 -" + "\n-- BUILD:     1.1 -> 1.85 ----" + "\n-- SPEED:    0.5 -> 0.85 -" + "\n-- DAMAGE: 9.0 -> 12.0  -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                    case "javelin-ship":
                        player.mech.health = 250f;
                        player.mech.buildPower = 1.5f;
                        player.mech.speed = 0.15f;
                        player.mech.weapon.bullet.damage = 13.0f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      JAVELIN ---" + "\n-- HEALTH: 170 -> 250 -" + "\n-- BUILD:     1 -> 1.5    ----" + "\n-- SPEED:    0.11 -> 0.15  -" + "\n-- DAMAGE: 10.5 -> 13.0 -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                    case "trident-ship":
                        player.mech.health = 350f;
                        player.mech.buildPower = 2.5f;
                        player.mech.speed = 0.25f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      TRIDENT  ---" + "\n-- HEALTH: 250 -> 350 -" + "\n-- BUILD:     1.75 -> 2.5 ---" + "\n-- SPEED:    0.15 -> 0.25 -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                    case "glaive-ship":
                        player.mech.health = 325f;
                        player.mech.buildPower = 1.75f;
                        player.mech.speed = 0.65f;
                        player.mech.weapon.bullet.damage = 10f;
                        player.sendMessage("[lightgray]///-\\\\\\SYSTEM///-\\\\\\" + "\n-- MECH:      GLAIVE -----" + "\n-- HEALTH: 250 -> 350 -" + "\n-- BUILD:     1.2 -> 1.65  ---" + "\n-- SPEED:    0.3 -> 0.65  -" + "\n-- DAMAGE: 7.5 -> 10.0   -" + "\n\\\\\\-///=========\\\\\\-///");
                        break;
                }
            }
        });

         */
        //Shows player info.
        handler.<Player>register("myinfo","Shows player info", (args, player) -> {
            String name = player.name;
            String rname;
            String dv = "false";
            if (database.get(player.uuid).getVerified())  dv = "true";
            rname = name.replaceAll("\\[", "[[");
            player.sendMessage("Name: " + name +
                    "\nName Raw: " + rname +
                    "\nTimes Joined: " + player.getInfo().timesJoined +
                    "\nTimes Kicked: " + player.getInfo().timesKicked +
                    "\nCurrent ID: " + player.id +
                    "\nCurrent IP: " + player.getInfo().lastIP +
                    "\nUUID: " + player.uuid +
                    "\nRank: " + database.get(player.uuid).getRank() +
                    "\nMinutes Played: " + database.get(player.uuid).getTP() +
                    "\nGames Played: " + database.get(player.uuid).getGP() +
                    "\nDiscord Verified?: " + dv);
        });
        //Shows info.
        handler.<Player>register("info","Shows the player info.", (args, player) -> {
            player.sendMessage("INFO:" +
                    "\n//About Us:" +
                    "\nChaotic neutral is a Mindustry server located in East US." +
                    "\nWe host 3 servers, 1111 survival, 2222 sandbox and a secret test server." +
                    "\nWe have a discord server, join us through website cn-discord.ddns.net or using discord code xQ6gGfQ" +
                    "\n\n//Game tricks:" +
                    "\n1) Pressing 9 will show arrows to upgrade pads." +
                    "\n2) to use colors in chat, you can type something like" +
                    "\n[[red]this is red text" +
                    "\n3) Different mechs build at different speeds, Trident builds the fastest.");
        });
        //finds a player discord tag
        handler.<Player>register("contact", "<id>","Verified Only - Finds a player's discord tag.", (arg, player) ->{
            if (database.get(player.uuid).getVerified()) {
                String a2 = arg[1];
                String pid= a2.replaceAll("[^0-9]", "");
                if (pid.equals("")) {
                    player.sendMessage("[salmon]GPI[white]: player ID must contain numbers!");
                    return;
                }
                Player p = playerGroup.getByID(Integer.parseInt(pid));
                if (p == null) {
                    player.sendMessage("[salmon]GPI[white]: Could not find player ID '[lightgray]" + pid + "[white]'.");
                    return;
                }
                if (database.containsKey(p.uuid)) {
                    player.sendMessage(database.get(p.uuid).getDiscordTag());
                    p.sendMessage(player.name + " has received your discord tag. do `/contact " + player.id + "` to get his contact.");
                }
            } else {
                player.sendMessage("You must be [sky]Verified [white]to use this command.");
            }
        });

        //-----ADMINS-----//

        handler.<Player>register("a","<Info> [1] [2] [3...]", "[scarlet]<Admin> [lightgray]- Admin commands", (arg, player) -> {
            if (database.get(player.uuid).getRank() >= 4) {
            } else if(!player.isAdmin){
                player.sendMessage(mba);
                return;
            }
            switch (arg[0]) {
                //un admin player - un-admins uuid, even if player is offline.
                case "uap": //Un-Admin Player
                    if (arg.length > 1 && arg[1].length() > 0) {
                        netServer.admins.unAdminPlayer(arg[1]);
                        player.sendMessage("unAdmin: " + arg[1]);
                        break;
                    } else {
                        player.sendMessage("[salmon]CT[white]: Un Admins Player, do `/a uap <UUID>`.");
                    }
                    break;

                //gameover - triggers gameover for admins team.
                case "gameover": //Game is over
                    if (GOW.contains(player.uuid)) {
                        Events.fire(new EventType.GameOverEvent(player.getTeam()));
                        Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + "[white] has ended the game.");
                        Log.info(player.name + " has ended the game.");
                    } else {
                        GOW.add(player.uuid);
                        player.sendMessage("This command will trigger a [gold]game over[white], use again to continue.");
                    }
                    break;

                case "inf": //Infinite resources, kinda.
                    if (arg.length > 1) {
                        if (IW.contains(player.uuid)) {
                            if (arg[1].contains("on")) {
                                state.rules.infiniteResources = true;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + " [white] has [lime]Enabled [white]Sandbox mode.");
                            } else if (arg[1].contains("off")) {
                                state.rules.infiniteResources = false;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + " [white] has [lime]Disabled [white]Sandbox mode.");
                            } else {
                                player.sendMessage("Turn Infinite Items [lightgray]on [white]or [lightgray]off[white].");
                            }
                        } else {
                            IW.add(player.uuid);
                            player.sendMessage("This command will change Sandbox Status, use again to continue.");
                        }
                    } else {
                        player.sendMessage("[salmon]INF[white]: Triggers sandbox, on/off");
                    }
                    break;

                case "10k":
                    Teams.TeamData teamData = state.teams.get(player.getTeam());
                    CoreBlock.CoreEntity core = teamData.cores.first();
                    core.items.add(Items.copper, 10000);
                    core.items.add(Items.lead, 10000);
                    core.items.add(Items.metaglass, 10000);
                    core.items.add(Items.graphite, 10000);
                    core.items.add(Items.titanium, 10000);
                    core.items.add(Items.thorium, 10000);
                    core.items.add(Items.silicon, 10000);
                    core.items.add(Items.plastanium, 10000);
                    core.items.add(Items.phasefabric, 10000);
                    core.items.add(Items.surgealloy, 10000);
                    Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + " [white] has given 10k resources to core.");
                    break;

                case "team": //Changes Team of user
                    if (arg.length > 1) {
                        String setTeamColor = "[#ffffff]";
                        Team setTeam;
                        switch (arg[1]) {
                            case "sharded":
                                setTeam = Team.sharded;
                                setTeamColor = "[accent]";
                                break;
                            case "blue":
                                setTeam = Team.blue;
                                setTeamColor = "[royal]";
                                break;
                            case "crux":
                                setTeam = Team.crux;
                                setTeamColor = "[scarlet]";
                                break;
                            case "derelict":
                                setTeam = Team.derelict;
                                setTeamColor = "[gray]";
                                break;
                            case "green":
                                setTeam = Team.green;
                                setTeamColor = "[lime]";
                                break;
                            case "purple":
                                setTeam = Team.purple;
                                setTeamColor = "[purple]";
                                break;
                            default:
                                player.sendMessage("[salmon]CT[lightgray]: Available teams: [accent]Sharded, [royal]Blue[lightgray], [scarlet]Crux[lightgray], [lightgray]Derelict[lightgray], [lime]Green[lightgray], [purple]Purple[lightgray].");
                                return;
                        }
                        player.setTeam(setTeam);
                        player.sendMessage("[salmon]CT[white]: Changed team to " + setTeamColor + arg[1] + "[white].");
                        break;
                    } else {
                        player.sendMessage("[salmon]CT[white]: Change Team, do `/a team info` to see all teams");
                    }
                    break;

                case "gpi": //Get Player Info
                    if (arg.length > 2 && arg[1].equals("id")) {
                        if (arg[2].length() > 0) {
                            String a2 = arg[2];
                            String pid= a2.replaceAll("[^0-9]", "");
                            if (pid.equals("")) {
                                player.sendMessage("[salmon]GPI[white]: player ID must contain numbers!");
                                return;
                            }
                            Player p = playerGroup.getByID(Integer.parseInt(pid));
                            if (p == null) {
                                player.sendMessage("[salmon]GPI[white]: Could not find player ID '[lightgray]" + pid + "[white]'.");
                                return;
                            }
                            String dv = "false";
                            if (database.get(p.uuid).getVerified()) dv = "true";
                            String name = p.name;
                            String rname = name.replaceAll("\\[", "[[");
                            player.sendMessage(
                                    "Name: " + name + "[white]" +
                                    "\nName Raw: " + rname +
                                    "\nTimes Joined: " + p.getInfo().timesJoined +
                                    "\nTimes Kicked: " + p.getInfo().timesKicked +
                                    "\nCurrent ID: " + p.id +
                                    "\nCurrent IP: " + p.getInfo().lastIP +
                                    "\nUUID: " + p.uuid +
                                    "\nRank: " + database.get(p.uuid).getRank() +
                                    "\nMinutes Played: " + database.get(p.uuid).getTP() +
                                    "\nGames Played: " + database.get(p.uuid).getGP() +
                                    "\nDiscord Verified?: " + dv);
                            if (database.get(p.uuid).getVerified()) player.sendMessage("Discord Tag: " + database.get(p.uuid).getDiscordTag());
                        } else {
                            player.sendMessage("[salmon]GPI[white]: Get Player Info, use ID, not UUID, to get a player's info");
                        }
                    } else if (arg.length > 2 && arg[1].equals("uuid") && netServer.admins.getInfo(arg[2]).timesJoined > 0) {
                        Administration.PlayerInfo p = netServer.admins.getInfo(arg[2]);
                        String name = p.lastName;
                        String rname = name.replaceAll("\\[", "[[");
                        String dv = "false";
                        if (database.get(arg[2]).getVerified()) dv = "true";
                        player.sendMessage("Name: " + name + "[white]" +
                                "\nName Raw: " + rname +
                                "\nTimes Joined: " + p.timesJoined +
                                "\nTimes Kicked: " + p.timesKicked +
                                "\nCurrent IP: " + p.lastIP +
                                "\nUUID: " + arg[2] +
                                "\nRank: " + database.get(arg[2]).getRank() +
                                "\nMinutes Played: " + database.get(arg[2]).getTP() +
                                "\nGames Played: " + database.get(arg[2]).getGP() +
                                "\nDiscord Verified?: " + dv);
                        if (database.get(arg[2]).getVerified()) player.sendMessage("Discord Tag: " + database.get(arg[2]).getDiscordTag());
                    } else {
                        player.sendMessage("[salmon]GPI[white]: Get Player Info, use ID or UUID, to get a player's info" +
                                "\n[salmon]GPI[white]: use arg id or uuid. example `/a gpi uuid abc123==`");
                    }
                    break;

                case "pardon": //Un-Bans players
                    if (arg.length > 1) {
                        if (arg.length > 2 && arg[2].equals("kick")) {
                            netServer.admins.getInfo(arg[1]).timesKicked = 0;
                            netServer.admins.getInfo(arg[1]).timesJoined = 0;
                            player.sendMessage("[salmon]pardon[white]: Set `times kicked` to 0 for UUID " + arg[1] + ".");
                        }
                        if (netServer.admins.isIDBanned(arg[1])) {
                            netServer.admins.unbanPlayerID(arg[1]);
                            player.sendMessage("[salmon]pardon[white]: Unbanned player UUID " + arg[1] + ".");
                        } else {
                            player.sendMessage("[salmon]pardon[white]: UUID [lightgray]" + arg[1] + "[white] wasn't found or isn't banned.");
                        }
                    } else {
                        player.sendMessage("[salmon]pardon[white]: Pardon, uses uuid to un-ban players. use arg kick to reset kicks.");
                    }
                    break;

                case "rpk":
                    if (arg.length > 2)  {
                        if (arg[1].equals("id")) {
                            String a2 = arg[2];
                            String pid= a2.replaceAll("[^0-9]", "");
                            if (pid.equals("")) {
                                player.sendMessage("[salmon]GPI[white]: player ID must contain numbers!");
                                return;
                            }
                            Player p = playerGroup.getByID(Integer.parseInt(pid));
                            if (p == null) {
                                player.sendMessage("[salmon]GPI[white]: Could not find player ID `" + pid + "`.");
                                return;
                            }
                            p.getInfo().timesKicked = 0;
                            p.getInfo().timesJoined = 0;
                            player.sendMessage("[salmon]RPK[white]: Times kicked set to zero for player " + p.getInfo().lastName);
                            Log.info("<Admin> " + player.name + " has reset times kicked for " + p.name + " ID " + pid);
                            return;
                        } else if (arg[1].equals("uuid")) {
                            if (netServer.admins.getInfo(arg[2]).timesKicked > 0) {
                                netServer.admins.getInfo(arg[2]).timesKicked = 0;
                                player.sendMessage("[salmon]RPK[white]: Times Kicked set to zero for player uuid [lightgray]" + arg[2]);
                                Log.info("<Admin> " + player.name + " has reset times kicked for " + netServer.admins.getInfo(arg[2]).lastName + " UUID " + arg[2]);
                            } else {
                                player.sendMessage("Player UUID `" + arg[2] + "` not found or kicks = 0");
                            }
                        } else {
                            player.sendMessage("[salmon]RPK[white]: Use arguments id or uuid.");
                        }
                    } else {
                        player.sendMessage("[salmon]RPK[white]: Reset Player Kicks, uses player ID or UUID, to reset player kicks." +
                                "\n[salmon]RPK[white]: use arg id or uuid. example `/a rpk uuid abc123==`");
                    }
                    break;

                case "bl":
                    player.sendMessage("Banned Players:");
                    Array<Administration.PlayerInfo> bannedPlayers = netServer.admins.getBanned();
                    bannedPlayers.each(pi -> player.sendMessage("[lightgray]" + pi.id +"[white] / Name: [lightgray]" + pi.lastName + "[white] / IP: [lightgray]" + pi.lastIP + "[white] / # kick: [lightgray]" + pi.timesKicked) );
                    break;

                case "pcc": //Player close connection
                    if (arg.length > 1 && arg[1].length() > 0) {
                        String pid= arg[1].replaceAll("[^0-9]", "");
                        if (pid.equals("")) {
                            player.sendMessage("[salmon]GPI[white]: player ID must contain numbers!");
                            return;
                        }
                        Player p = playerGroup.getByID(Integer.parseInt(pid));
                        if (p == null) {
                            player.sendMessage("[salmon]GPI[white]: Could not find player ID '[lightgray]" + pid + "[white]'.");
                            return;
                        }
                        String reason = "[white]Connection Closed.";
                        if (arg.length > 3) {
                            reason = arg[2] +" "+ arg[3];
                        } else if (arg.length > 2) {
                            reason = arg[2];
                        }
                        p.getInfo().timesKicked--;
                        p.con.kick(reason, 1);
                    } else {
                        player.sendMessage("[salmon]PCC[white]: Player Connection Closed, use ID, not UUID, to close a players connection.");
                    }
                    break;
                case "unkick":
                    if (arg.length > 1) {
                        if (netServer.admins.getInfo(arg[1]).lastKicked > Time.millis()) {
                            netServer.admins.getInfo(arg[1]).lastKicked = Time.millis();
                            player.sendMessage("[salmon]pardon[white]: Un-Kicked player UUID " + arg[1] + ".");
                        } else {
                            player.sendMessage("[salmon]pardon[white]: UUID [lightgray]" + arg[1] + "[white] wasn't found or isn't kicked.");
                        }
                    } else {
                        player.sendMessage("[salmon]UK[white]: Un-Kick, uses uuid to un-kick players.");
                    }
                    break;

                case "tp":
                    if (arg.length > 1) {
                        if (arg.length == 2) player.sendMessage("[salmon]TP[white]: You need y coordinate.");
                        if (arg.length < 3) return;
                        String x2= arg[1].replaceAll("[^0-9]", "");
                        String y2= arg[2].replaceAll("[^0-9]", "");
                        if (x2.equals("") || y2.equals("")) {
                            player.sendMessage("[salmon]TP[white]: Coordinates must contain numbers!");
                            return;
                        }

                        float x2f = Float.parseFloat(x2);
                        float y2f = Float.parseFloat(y2);

                        if (x2f > world.getMap().width) {
                            player.sendMessage("[salmon]TP[white]: Your x coordinate is too large. Max: " + world.getMap().width);
                            return;
                        }
                        if (y2f >= world.getMap().height) {
                            player.sendMessage("[salmon]TP[white]: y must be: 0 <= y <= " + world.getMap().height);
                            return;
                        }
                        player.sendMessage("[salmon]TP[white]: Moved [lightgray]" + player.name + " [white]from ([lightgray]" + player.x / 8+ " [white], [lightgray]" + player.y / 8 + "[white]) to ([lightgray]" + x2 + " [white], [lightgray]" + y2 + "[white]).");
                        player.set(Integer.parseInt(x2),Integer.parseInt(y2));
                        player.setNet(8 * x2f,8 * y2f);
                        player.set(8 * x2f,8 * y2f);
                    } else {
                        player.sendMessage("\"[salmon]TP[white]: Teleports player to given coordinates");
                    }
                    break;

                case "ac":
                    if (arg.length > 1) {
                        String string = null;
                        string = arg[1];
                        if (arg.length > 2) {
                            if (arg.length > 3) {
                                string = arg[1] + " " + arg[2] + " " + arg[3];
                            }
                            string = arg[1] + " " + arg[2];
                        }
                        String finalString = string;
                        playerGroup.all().each(p -> p.isAdmin, o -> o.sendMessage(finalString, player, "[salmon]<AC>[white] " + NetClient.colorizeName(player.id, player.name)));
                    } else {
                        player.sendMessage("");
                    }
                    break;

                case "cr": //Changer player rank
                    if (arg.length > 2) {
                        if (database.containsKey(arg[1])) {
                            String a2 = arg[2];
                            String pid= a2.replaceAll("[^0-9]", "");
                            if (pid.equals("")) {
                                player.sendMessage("[salmon]CR[white]: rank must contain numbers!");
                                return;
                            }
                            if (database.get(player.uuid).getRank() < Integer.parseInt(pid)) {
                                player.sendMessage("You dont have permission to change ranks above " + database.get(player.uuid).getRank());
                                return;
                            }
                            database.get(arg[1]).changeRank(Integer.parseInt(pid));
                            player.sendMessage("Changed rank of `" + arg[1] + "` to " + pid + ".");
                            return;
                        } else {
                            player.sendMessage("[salmon]CR[white]: Player UUID `" + arg[2] + "` not found in database.");
                        }
                    } else {
                        player.sendMessage("Too few argument. use /a cr UUID rank");
                    }
                    break;

                case "setTag":
                    if (arg.length > 2) {
                        if (database.containsKey(arg[1])) {
                            if (!arg[2].contains("#")) {
                                player.sendMessage("[salmon]ST[white]: Discord tag must contain `#`! example: abc123#4567");
                                return;
                            } else if (arg[2].length() <= 5) {
                                player.sendMessage("[salmon]ST[white]: Discord tag must be at least 6 digits! example: abc123#4567");
                                return;
                            }
                            database.get(arg[1]).setDiscordTag(arg[2]);
                            database.get(arg[1]).verify();
                            player.sendMessage("[salmon]ST[white]: Discord tag set to `[lightgray]" + arg[2] + "[white]` for `[lightgray]" + arg[1] +"[white]`.");
                        } else {
                            player.sendMessage("[salmon]ST[white]: Player UUID `" + arg[2] + "` not found in database.");
                        }
                        return;
                    } else {
                        player.sendMessage("[salmon]ST[white]: Too few argument. use /a setTag UUID Discord#Tag");
                    }
                    break;
                case "test": //test commands;
                    HashMap<String, pi> database = new HashMap<>();
                    database.put("GycadrGQyRBAAAAAVdsxaQ==", new pi());
                    database.get("GycadrGQyRBAAAAAVdsxaQ==").addGP();
                    //output file
                    try {
                        FileOutputStream fileOut = new FileOutputStream("PDF.cn");
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(database);
                        out.close();
                        fileOut.close();
                        player.sendMessage("done");
                    } catch (IOException i) {
                        i.printStackTrace();
                    }
                    player.sendMessage("\uE800\uE801\uE802\uE804\uE805\uE806\uE807\uE808\uE809\uE80A\uE80B \uE80C\uE80D\uE80E\uE80F\uE810\uE811\uE812\uE813\uE814\uE815\uE816\uE818\uE819\uE81A\uE81C\uE81D\uE81E\uE81F\uE820\uE821\uE822\uE824\uE828\uE829\uE82A\uE82C\uE82D\uE82E\uE82F\uE830\uE831\uE832\uE834\uE838\uE839\uE83A\uE83C\uE83D\uE83E\uE83F\uE840\uE841\uE842\uE844\uE848\uE849\uE84A\uE84C\uE84D\uE84E\uE84F\uE850\uE851\uE852\uE854\uE858\uE859\uE85A\uE85C\uE85D\uE85E\uE85F\uE860\uE861\uE862\uE864\uE868\uE869\uE86A\uE86C\uE86D\uE86E\uE86F\uE870\uE871\uE872\uE874\uE878\uE879\uE87A\uE87C\uE87D\uE87E\uE87F");
                    player.sendMessage("\uE800\uE801\uE802\uE803\uE804\uE805\uE806\uE807\uE808\uE809\uE810\uE811\uE812\uE813\uE814\uE815\uE816\uE817\uE818\uE819\uE820\uE821\uE822\uE823\uE824\uE825\uE826\uE827\uE828\uE829\uE830\uE831\uE832\uE833\uE834\uE835\uE836\uE837\uE838\uE839\uE840\uE841\uE842\uE843\uE844\uE845\uE846\uE847\uE848\uE849\uE850\uE851\uE852\uE853\uE854\uE855\uE856\uE857\uE858\uE859\uE860\uE861\uE862\uE863\uE864\uE865\uE866\uE867\uE868\uE869\uE870\uE871\uE872\uE873\uE874\uE875\uE876\uE877\uE878\uE879\uE880\uE881\uE882\uE883\uE884\uE884\uE885\uE886\uE887\uE888\uE889\uE890\uE891\uE892\uE893\uE894\uE895\uE896\uE897\uE898\uE899\uE80A\uE80B\uE80C\uE80D\uE80E\uE80F\uE81A\uE81B\uE81C\uE81D\uE81E\uE81F\uE82A\uE82B\uE82C\uE82D\uE82E\uE82F\uE83A\uE83B\uE83C\uE83D\uE83E\uE83F\uE84A\uE84B\uE84C\uE84D\uE84E\uE84F\uE85A\uE85B\uE85C\uE85D\uE85E\uE85F\uE86A\uE86B\uE86C\uE86D\uE86E\uE86F\uE87A\uE87B\uE87C\uE87D\uE87E\uE87F\uE88A\uE88B\uE88C\uE88D\uE88E\uE88F\uE89A\uE89B\uE89C\uE89d\uE89e\uE89F");
                    break;

                case "info": //all commands
                    player.sendMessage("\tAvailable Commands:" +
                            "\nuap              - Un Admins Player, [uud]" +
                            "\ngameover         - Triggers game over." +
                            "\ninf              - Infinite Items." +
                            "\n10k              - Adds 10k of every resource to core." +
                            "\nteam             - Changes team, team" +
                            "\ngpi              - Gets Player Info, ID/UUID - ###" +
                            "\npardon           - Un-Bans a player, UUID" +
                            "\nrpk              - Resets player kick count, ID/UUID - ###" +
                            "\nbl               - Shows Ban List." +
                            "\npcc              - Closes a player connection." +
                            "\nunkick           - Un-Kicks a player, UUID." +
                            "\ntp               - Teleports player, x - y" +
                            "\nac               - Admin Chat" +
                            "\ncr               - Changes player rank." +
                            "\ninfo             - Shows all commands and brief description, uuid");
                    break;

                case "mms": //DON'T TRY IT!
                    int y = -200;
                    for (int i = 0; i <= 400; i = i + 1) {
                        y = y + 1;
                        Call.onInfoMessage(player.con, String.valueOf(y));
                    }
                    break;
                //if none of the above commands used.
                default:
                    player.sendMessage(arg[0] + " Is not a command. Do `/a info` to see all available commands");
            }
        });
    }

}

