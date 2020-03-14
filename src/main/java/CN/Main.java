package CN;

//-----imports-----//
import arc.Core;
import arc.Events;
import arc.struct.Array;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Strings;
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
import sun.management.counter.perf.PerfLongArrayCounter;

import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Date;

import static mindustry.Vars.*;


public class Main extends Plugin {
    public static Array<String> GOW = new Array<>();
    public static Array<String> IW = new Array<>();
    public static HashMap<String, String> buffList = new HashMap<>();
    public static HashMap<String, pi> database = new HashMap<>();
    public static Array<String> pjl = new Array<>();

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
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //output PI save file
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
                            Main.database.get(p.uuid).addTp(1);
                            Call.onInfoToast(p.con,"+1 minutes played.", 3);
                            //auto congratulations
                            switch (Main.database.get(p.uuid).getTP()) {
                                case 1* 60:
                                case 2* 60:
                                case 3* 60:
                                case 4* 60:
                                case 5* 60:
                                case 6* 60:
                                case 7* 60:
                                case 8* 60:
                                case 9* 60:
                                case 10* 60:
                                case 11* 60:
                                case 12* 60:
                                    Call.sendMessage("Congratulations to " + p.name + " [white]for staying active for " + (Main.database.get(p.uuid).getTP()/60) + " Hours!");
                                default:
                                    break;
                            }
                            }
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
                    Log.info("[B] Banned \"{0}\" [{1}] for (Kick) > (join)/5", player.name, player.uuid);
                    byteCode.ban(player.uuid,"Kicked > Joined/5");
                    player.con.kick("(AutoBan) Banned for being kicked most of the time. If you want to appeal, give the previous as reason.");
                } else if (player.getInfo().timesKicked > 15) {
                    byteCode.ban(player.uuid,"Kick > 15");
                    Log.info("[B] Banned \"{0}\" [{1}] for Kick > 15.", player.name, player.uuid);
                    player.con.kick("(AutoBan) Banned for being kicked than 15. If you want to appeal, give the previous as reason.");
                } else if (database.containsKey(player.uuid) && database.get(player.uuid).getRank() == 6 && !player.getInfo().lastIP.equals("127.0.0.1")) {
                    byteCode.ban(player.uuid,"Rank 6");
                    Log.info("[B] Banned \"{0}\" [{1}] for Rank 6.", player.name, player.uuid);
                    player.con.kick("(AutoBan) Banned for ==Rank 6== . If you want to appeal, give the previous as reason.");
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
                    "\nFor \uE801 Info, do /info" +
                    "\n[white]======================================================================");
            //Remove all <> in name
            player.name = player.name.replaceAll("\\<(.*)\\>", "").replace("<","").replace(">","").replace("\n","");
            StringBuilder builder = new StringBuilder();
            builder.append(player.name);
            for (int i = player.name.length(); i == 40; i++) {
                builder.append(" ");
            }

            //add tags
            if (database.containsKey(player.uuid)) {
                //Admin/Mod
                switch (database.get(player.uuid).getRank()) {
                    case 6:
                        builder.append("\n [accent]").append(byteCode.rankI(6));
                        break;
                    case 5:
                        builder.append("\n [accent]").append(byteCode.rankI(5));
                        break;
                    case 4:
                        builder.append("\n [accent]").append(byteCode.rankI(4));
                        break;
                    case 3:
                        builder.append("\n [accent]").append(byteCode.rankI(3));
                }

                //Verified Icon

                if (database.get(player.uuid).getVerified() && database.get(player.uuid).getRank() >= 3) {
                    builder.append(" ").append(byteCode.verifiedI());
                } else if (database.get(player.uuid).getVerified()) {
                    builder.append(" [accent]").append(byteCode.verifiedI());
                }
                player.name = builder.toString();
            } else {
                Call.sendMessage("[white]Welcome " + player.name + ", [white]first time on the server!");
                player.sendMessage("[white]======================================================================\n" +
                        "[scarlet]READ THE RULES!!![white]\n" +
                        "General Rules\n" +
                        "Do Not:\n" +
                        "1) Grief or Spam.\n" +
                        "2) Ban/Kick evade. \n" +
                        "3) Use exploits. \n" +
                        "4) Nuke core, that's why we have RTV.\n" +
                        "5) Use multiple accounts.\n\n" +
                        "Survival:\n" +
                        "1) Pixel art permitted but must be less that 16x16. Pixel art may be removed at any point by anyone. Spamming will result in ban.\n" +
                        "2) Belts going to core must take the least complicated yet most direct route to core. Don't snake belts and/or place on cramped places.\n" +
                        "3) Thorium reactors must be far away from core. \n" +
                        "4) No more than 50 draught miners per map.\n" +
                        "5) Absolutely no exploits allowed; whether beneficial or not.\n" +
                        "6) High power sources must be dioded." +
                        "[white]======================================================================\n");
                database.put(player.uuid, new pi());
            }
            //pjl
            Date thisDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");

            pjl.add("[lime][+] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
        });

        Events.on(EventType.PlayerLeave.class, event -> {
            Player player = event.player;
            //pjl
            Date thisDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
            pjl.add("[scarlet][-] [white]" + dateFormat.format(thisDate) + byteCode.nameR(player.name) + " | " + player.uuid + " | " +player.getInfo().lastIP);
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

            Vars.netServer.admins.addActionFilter(action -> {
                return action.type != Administration.ActionType.rotate; //thx fuzz
            });
        });

        Events.on(EventType.WaveEvent.class, event -> {
            //Sandbox
            if(sandbox && state.wave!=2222) state.wave=2222;
        });

        Events.on(EventType.GameOverEvent.class, event -> {
            for (Player p : playerGroup.all()) {
                if (database.containsKey(p.uuid)) {
                    database.get(p.uuid).addGp(1);
                    Call.onInfoToast(p.con,"Games Played: +1",10);
                }
            }
        });

        Events.on(EventType.BlockBuildEndEvent.class, event -> {
            Player player = event.player;
            if (player == null) return;
            if (event.breaking) return;
            if (database.containsKey(player.uuid)) {
                database.get(player.uuid).addBb(1);
                if (database.get(player.uuid).getBB() > 10000) {
                    Call.sendMessage("");
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
                database.get(arg[0]).setRank(Integer.parseInt(pid));
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
                Log.info("done");
            } catch (IOException | NullPointerException i) {
                i.printStackTrace();
            }
        });
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        //-----USERS-----//

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
            int eruptor = 0;
            int chaosArray = 0;
            int eradicator = 0;
            int wraith = 0;
            int ghoul = 0;;
            int revenant = 0;
            int lich = 0;
            int reaper = 0;

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
                    if (u.getTypeID().name.equals("eruptor")) eruptor = eruptor + 1;
                    if (u.getTypeID().name.equals("chaos-array")) chaosArray = chaosArray + 1;
                    if (u.getTypeID().name.equals("eradicator")) eradicator = eradicator + 1;
                    if (u.getTypeID().name.equals("wraith")) wraith = wraith + 1;
                    if (u.getTypeID().name.equals("ghoul")) ghoul = ghoul + 1;
                    if (u.getTypeID().name.equals("revenant")) revenant = revenant + 1;
                    if (u.getTypeID().name.equals("lich")) lich = lich + 1;
                    if (u.getTypeID().name.equals("reaper")) reaper = reaper + 1;
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
                            "\nEruptor: " + eruptor +
                            "\nChaos Array: " + chaosArray +
                            "\nEradicator: " + eradicator +
                            "\nWraith Fighter: " + wraith +
                            "\nGhoul Bomber: " + ghoul +
                            "\nRevenant: " + revenant +
                            "\nLich: " + lich +
                            "\nReaper: " + reaper +
                            "\nTotal: " + All +
                            "\n");
        });
        //Lists players and their respective id's
        handler.<Player>register("players", "List of people and ID.", (args, player) -> {
            StringBuilder builder = new StringBuilder();
            builder.append("[accent]List of players: \n");
            for (Player p : playerGroup.all()) {
                String name = p.name;
                if (Main.database.containsKey(p.uuid)) {
                    if (!p.isAdmin) {
                        name = name.replaceAll("\\[", "[[");
                        builder.append("[white]");
                    }
                    for (int i = 0; i < database.get(p.uuid).getRank(); i = i + 1) {
                        builder.append(">");
                    }
                    if (database.get(p.uuid).getVerified()) builder.append(" [sky]\uE848 ");
                    if (database.get(p.uuid).getRank() == 6) builder.append(" ").append(byteCode.rankI(6));
                    if (database.get(p.uuid).getRank() == 5) builder.append(" ").append(byteCode.rankI(5));
                    if (database.get(p.uuid).getRank() == 4) builder.append(" ").append(byteCode.rankI(4));
                    if (database.get(p.uuid).getRank() == 3) builder.append(" ").append(byteCode.rankI(3));
                    if (p.isAdmin) builder.append(" [white]\uE828 ");
                }
                builder.append("[lightgray]").append(name).append("[accent] : #[lightgray]").append(p.id).append("\n[accent]");
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
            player.sendMessage("Name: " + name + " [white]"+
                    "\nName Raw: " + rname +
                    "\nTimes Joined: " + player.getInfo().timesJoined +
                    "\nTimes Kicked: " + player.getInfo().timesKicked +
                    "\nCurrent ID: " + player.id +
                    "\nCurrent IP: " + player.getInfo().lastIP +
                    "\nUUID: " + player.uuid +
                    "\nRank: " + database.get(player.uuid).getRank() +
                    "\nMinutes Played: " + database.get(player.uuid).getTP() +
                    "\nThings Built: " + database.get(player.uuid).getBB() +
                    "\nGames Played: " + database.get(player.uuid).getGP() +
                    "\nDiscord Verified?: " + dv);
        });
        //Shows info.
        handler.<Player>register("info","[colors]","Shows the player info.", (arg, player) -> {
            if (arg.length == 1) {
                switch (arg[0]) {
                    case "colors":
                        player.sendMessage("[clear]clear  [black]black  [white]white  [lightgray]lightgray  [gray]gray  [darkgray]darkgray  [blue]blue  [navy]navy  [royal]royal  [slate]slate  [sky]sky  [cyan]cyan  [teal]teal  [green]green  [acid]acid  [lime]lime  [forest]forest  [olive]olive  [yellow]yellow  [gold]gold  [goldenrod]goldenrod  [orange]orange  [brown]brown  [tan]tan  [brick]brick  [red]red  [scarlet]scarlet  [coral]coral  [salmon]salmon  [pink]pink  [magenta]magenta  [purple]purple  [violet]violet  [maroon]maroon");
                        break;
                    default:
                        player.sendMessage("N/A");
                        break;
                }
                return;
            } else {
                player.sendMessage("INFO:" +
                        "\n//About Us:" +
                        "\nChaotic neutral is a Mindustry server located in East US." +
                        "\nWe host 3 servers, 1111 survival, 2222 sandbox and a secret test server." +
                        "\nWe have a discord server, join us through website cn-discord.ddns.net or using discord code xQ6gGfQ" +
                        "\n\n//ranks:\n[accent]" +
                        byteCode.rankI(6) + " - Owner\n" +
                        byteCode.rankI(5) + " - Admin\n" +
                        byteCode.rankI(4) + " - Moderator\n" +
                        byteCode.rankI(3) + " - Semi Moderator[white]" +
                        "\n\n//Game tricks:" +
                        "\n1) Pressing 9 will show arrows to upgrade pads." +
                        "\n2) to use colors in chat, you can type something like" +
                        "\n[[red]this is red text" +
                        "\n3) Different mechs build at different speeds, Trident builds the fastest.");
            }
        });
        //finds a player discord tag
        handler.<Player>register("contact", "<id>","Verified Only - Finds a player's discord tag.", (arg, player) ->{
            if (database.get(player.uuid).getVerified()) {
                String pid= arg[0].replaceAll("[^0-9]", "");
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
                    if (database.get(p.uuid).getVerified()) {
                        player.sendMessage(database.get(p.uuid).getDiscordTag());
                        p.sendMessage(player.name + " [white]has received your discord tag. do `/contact " + player.id + "` to get his contact.");
                    } else {
                        player.sendMessage("You can only contact [sky]Verified [white]players.");
                    }
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
                    if (database.containsKey(player.uuid) && database.get(player.uuid).getRank() == 6) {
                        if (arg.length > 1 && arg[1].length() > 0) {
                            netServer.admins.unAdminPlayer(arg[1]);
                            player.sendMessage("unAdmin: " + arg[1]);
                            break;
                        } else {
                            player.sendMessage("[salmon]CT[white]: Un Admins Player, do `/a uap <UUID>`.");
                        }
                    } else {
                        player.sendMessage("[salmon]UAP[]: You don't have permission to use this command!");
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
                            if (arg[1].equals("on")) {
                                state.rules.infiniteResources = true;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + " [white] has [lime]Enabled [white]Sandbox mode.");
                                for (Player p : playerGroup.all()) {
                                    Call.onWorldDataBegin(p.con);
                                    netServer.sendWorldData(p);
                                    Call.onInfoToast(p.con, "Auto Sync completed.", 5);
                                }
                            } else if (arg[1].equals("off")) {
                                state.rules.infiniteResources = false;
                                Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + " [white] has [lime]Disabled [white]Sandbox mode.");
                                for (Player p : playerGroup.all()) {
                                    Call.onWorldDataBegin(p.con);
                                    netServer.sendWorldData(p);
                                    Call.onInfoToast(p.con, "Auto Sync completed.", 5);
                                }
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
                    if (arg.length > 1 && arg[1].length() > 3) {
                        if (arg[1].startsWith("#") && Strings.canParseInt(arg[1].substring(1))) {
                            int id = Strings.parseInt(arg[1].substring(1));
                            Player p = playerGroup.getByID(id);
                            if (p == null) {
                                player.sendMessage("[salmon]GPI[white]: Could not find player ID '[lightgray]" + id + "[white]'.");
                                return;
                            }
                            String rname = byteCode.nameR(p.name);
                            String dv = "false";
                            if (database.containsKey(p.uuid) && database.get(p.uuid).getVerified()) dv = "true";
                            player.sendMessage("Name: " + p.name + "[white]" +
                                    "\nName Raw: " + rname +
                                    "\nTimes Joined: " + p.getInfo().timesJoined +
                                    "\nTimes Kicked: " + p.getInfo().timesKicked +
                                    "\nCurrent IP: " + p.getInfo().lastIP +
                                    "\nUUID: " + p.uuid +
                                    "\nRank: " + database.get(p.uuid).getRank() +
                                    "\nBuildings Built: " + database.get(p.uuid).getBB() +
                                    "\nMinutes Played: " + database.get(p.uuid).getTP() +
                                    "\nGames Played: " + database.get(p.uuid).getGP() +
                                    "\nDiscord Verified?: " + dv +
                                    "\nDiscord Tag: " + database.get(p.uuid).getDiscordTag());
                        } else if (arg[1].startsWith("#")) {
                            player.sendMessage("ID can only contain numbers!");
                            return;
                        } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                            Administration.PlayerInfo p = netServer.admins.getInfo(arg[1]);
                            String rname = byteCode.nameR(p.lastName);
                            String dv = "false";
                            if (database.containsKey(arg[1]) && database.get(arg[1]).getVerified()) dv = "true";
                            player.sendMessage("Name: " + p.lastName + "[white]" +
                                    "\nName Raw: " + rname +
                                    "\nTimes Joined: " + p.timesJoined +
                                    "\nTimes Kicked: " + p.timesKicked +
                                    "\nCurrent IP: " + p.lastIP +
                                    "\nUUID: " + arg[1] +
                                    "\nRank: " + database.get(arg[1]).getRank() +
                                    "\nBuildings Built: " + database.get(arg[1]).getBB() +
                                    "\nMinutes Played: " + database.get(arg[1]).getTP() +
                                    "\nGames Played: " + database.get(arg[1]).getGP() +
                                    "\nDiscord Verified?: " + dv +
                                    "\nDiscord Tag: " + database.get(arg[1]).getDiscordTag());
                        } else {
                            player.sendMessage("UUID [lightgray]" + arg[1] + " []not found!");
                            return;
                        }
                    } else {
                        player.sendMessage("[salmon]GPI[white]: Get Player Info, to get a player's info" +
                                "\n[salmon]GPI[white]: use #id or uuid. example `/a gpi abc123==`");
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
                    if (arg.length > 1 && arg[1].length() > 3) {
                        if (arg[1].startsWith("#") && Strings.canParseInt(arg[1].substring(1))) {
                            int id = Strings.parseInt(arg[1].substring(1));
                            Player p = playerGroup.getByID(id);
                            if (p == null) {
                                player.sendMessage("[salmon]RPK[white]: Could not find player ID '[lightgray]" + id + "[white]'.");
                                return;
                            }
                            p.getInfo().timesKicked = 0;
                            p.getInfo().timesJoined = 0;
                            player.sendMessage("[salmon]RPK[white]: Times kicked set to zero for player " + p.getInfo().lastName);
                            Log.info("<Admin> " + player.name + " has reset times kicked for " + p.name + " ID " + id);
                        } else if (arg[1].startsWith("#")) {
                            player.sendMessage("ID can only contain numbers!");
                        } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                            Administration.PlayerInfo p = netServer.admins.getInfo(arg[1]);
                            p.timesKicked = 0;
                            p.timesJoined = 0;
                            player.sendMessage("[salmon]RPK[white]: Times Kicked set to zero for player uuid [lightgray]" + arg[1]);
                            Log.info("<Admin> " + player.name + " has reset times kicked for " + p.lastName + " UUID " + arg[1]);
                        } else {
                            player.sendMessage("UUID [lightgray]" + arg[1] + " []not found!");
                        }
                    } else {
                        player.sendMessage("[salmon]RPK[white]: Get Player Info, use ID or UUID, to get a player's info" +
                                "\n[salmon]RPK[white]: use arg id or uuid. example `/a RPK abc123==`");
                    }
                    break;
                case "bl":
                    player.sendMessage("Banned Players:");
                    Array<Administration.PlayerInfo> bannedPlayers = netServer.admins.getBanned();
                    bannedPlayers.each(pi -> {
                        player.sendMessage("[white]======================================================================\n" +
                                "[lightgray]" + pi.id +"[white] / Name: [lightgray]" + pi.lastName + "[white]\n" +
                                " / IP: [lightgray]" + pi.lastIP + "[white] / # kick: [lightgray]" + pi.timesKicked);
                    });
                    break;

                case "pcc": //Player close connection
                    //setup

                    //run
                    if (arg.length > 2) {
                        if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                            //run
                            int id = Strings.parseInt(arg[1].substring(1));
                            Player p = playerGroup.getByID(id);
                            if (p == null) {
                                player.sendMessage("[salmon]PCC[white]: Could not find player ID '[lightgray]" + id + "[white]'.");
                                return;
                            }
                            String reason = arg[2];
                            switch (arg.length-1) {
                                case 3:
                                    reason = arg[2] + " " + arg[3];
                                    break;
                                case 4:
                                    reason = arg[2] + " " + arg[3] + " " + arg[4];
                                    break;
                            }
                            p.getInfo().timesKicked--;
                            p.con.kick(reason, 1);

                        } else if (arg[1].startsWith("#")) {
                            player.sendMessage("[salmon]PCC[white]: ID can only contain numbers!");
                            return;
                        }
                    } else if (arg.length > 1) {
                        player.sendMessage("[salmon]PCC[white]: You must provide a reason!");
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
                        if (y2f > world.getMap().height) {
                            player.sendMessage("[salmon]TP[white]: y must be: 0 <= y <= " + world.getMap().height);
                            return;
                        }
                        player.sendMessage("[salmon]TP[white]: Moved [lightgray]" + player.name + " [white]from ([lightgray]" + player.x / 8+ " [white], [lightgray]" + player.y / 8 + "[white]) to ([lightgray]" + x2 + " [white], [lightgray]" + y2 + "[white]).");
                        player.set(Integer.parseInt(x2),Integer.parseInt(y2));
                        player.setNet(8 * x2f,8 * y2f);
                        player.set(8 * x2f,8 * y2f);
                    } else {
                        player.sendMessage("[salmon]TP[white]: Teleports player to given coordinates");
                    }
                    break;

                case "ac":
                    if (arg.length > 1) {
                        String string = null;
                        switch (arg.length - 1) {
                            case 1:
                            case 2:
                            case 3:
                            case 4:
                        }

                        String finalString = string;
                        playerGroup.all().each(p -> p.isAdmin, o -> o.sendMessage(finalString, player, "[#" + player.getTeam().color.toString() + "]<AC>" + NetClient.colorizeName(player.id, player.name)));
                    } else {
                        player.sendMessage("Admin chat, to /a ac enter-your-text-here");
                    }
                    break;

                case "cr": //Changer player rank
                    if (arg.length > 2) {
                        //setup
                        String uid = null;
                        boolean proceed = false;
                        //run
                        if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))) {
                            int id = Strings.parseInt(arg[1].substring(1));
                            Player p = playerGroup.getByID(id);
                            if (p == null) {
                                player.sendMessage("[salmon]CR[white]: Could not find player ID `" + id + "`.");
                                return;
                            }
                            //run
                            uid = p.uuid;
                            proceed = true;
                        } else if (arg[1].startsWith("#")) {
                            player.sendMessage("[salmon]CR[white]: ID can only contain numbers!");
                        } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                            //run
                            uid = arg[1];
                            proceed = true;
                        } else {
                            player.sendMessage("[salmon]CR[white]: UUID not found!");
                        }
                        if (proceed && arg[2].length() == 1 && Strings.canParseInt(arg[2])) {
                            int rank = Strings.parseInt(arg[2]);
                            if (database.containsKey(player.uuid) && database.containsKey(uid)) {
                                if (database.get(uid).getRank() >= database.get(player.uuid).getRank()) {
                                    player.sendMessage("[salmon]CR[white]: You don't have permission to change rank!");
                                } else if (database.get(player.uuid).getRank() > rank) {
                                    database.get(uid).setRank(rank);
                                    player.sendMessage("[salmon]CR[white]: Changed rank of `" + uid + "` to " + rank + ".");

                                } else if (database.get(player.uuid).getRank() < rank) {
                                    player.sendMessage("[salmon]CR[white]: You don't have permission to change rank to " + rank +
                                            "\nYou may only change ranks up to " + (rank - 1) + ".");

                                }
                            }
                        } else {
                            player.sendMessage("[salmon]CR[white]: Rank can only contain numbers!");
                        }
                    } else {
                        player.sendMessage("[salmon]CR[white]: Change Rank; Changes rank for selected player. example: /a cr #123 2");
                    }
                    break;

                case "setTag":
                    if (arg.length > 2) {
                        //setup
                        boolean proceed = false;
                        String uid = null;
                        String tag = null;
                        //run
                        if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))){
                            int id = Strings.parseInt(arg[1].substring(1));
                            Player p = playerGroup.getByID(id);
                            if (p == null) {
                                player.sendMessage("[salmon]ST[white]: Could not find player ID `" + id + "`.");
                                return;
                            }
                            uid = p.uuid;
                            tag = arg[2];
                            proceed = true;
                        } else if (arg[1].startsWith("#")){
                            player.sendMessage("[salmon]ST[]: ID can only contain numbers!");
                        } else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
                            //run
                            uid = arg[1];
                            tag = arg[2];
                            proceed = true;
                        } else {
                            player.sendMessage("[salmon]ST[]: UUID not found!");
                        }

                        if (proceed) {
                            if (database.containsKey(uid)) {
                                if (!tag.contains("#")) {
                                    player.sendMessage("[salmon]ST[white]: Discord tag must contain `#`! example: abc123#4567");
                                    return;
                                } else if (tag.length() <= 5) {
                                    player.sendMessage("[salmon]ST[white]: Discord tag must be at least 6 digits! example: abc123#4567");
                                    return;
                                }
                                database.get(uid).setDiscordTag(tag);
                                database.get(uid).setVerified(true);
                                if (database.get(uid).getRank() == 0) database.get(uid).setRank(1);
                                player.sendMessage("[salmon]ST[white]: Discord tag set to `[lightgray]" + tag + "[white]` for `[lightgray]" + uid +"[white]`.");
                            } else {
                                player.sendMessage("[salmon]ST[white]: Player not found in database.");
                            }
                        }
                    } else if (arg.length > 1) {
                        player.sendMessage("[salmon]ST[]: You must provide discord tag!");
                    } else {
                        player.sendMessage("[salmon]ST[]: Verifies player and adds discord tag using String. example /a #123 abc1234#1234");
                    }
                    break;

                case "ban": //bans player
                    if (arg.length > 2) {
                        String reason = arg[2];
                        switch (arg.length-1) {
                            case 3:
                                reason = arg[2] + " " + arg[3];
                                break;
                            case 4:
                                reason = arg[2] + " " + arg[3] + " " + arg[4];
                                break;
                        }
                        player.sendMessage(byteCode.ban(arg[1], reason));
                    } else if (arg.length > 1) {
                        player.sendMessage("You must give reason!");
                    }
                    break;
                case "pjl": //list of players joining and leaving
                    if (pjl.size > 50) {
                        for (int i = pjl.size - 50; i < pjl.size; i++) {
                            player.sendMessage(pjl.get(i));
                        }
                    } else {
                        for (int i = 0; i < pjl.size; i++) {
                            player.sendMessage(pjl.get(i));
                        }
                    }
                    break;

                case "test": //test commands;
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
                            "\nsetTag           - Sets discord tag for player, id/uuid - ###" +
                            "\nban              - Bans player, id/uuid - ### - reason" +
                            "\npjl              - List of last 50 player joins and leaves." +
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

