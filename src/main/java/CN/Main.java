package CN;

//-----imports-----//
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
import mindustry.type.UnitType;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.content.Items;
import mindustry.content.UnitTypes;
import mindustry.entities.type.BaseUnit;

import java.util.HashMap;

import static mindustry.Vars.*;


public class Main extends Plugin {
    public static Array<String> GOW = new Array<>();
    public static Array<String> IW = new Array<>();
    public static HashMap<String, String> buffList = new HashMap<>();

    private boolean summonEnable = true;
    private boolean reaperEnable = true;
    private boolean lichEnable = true;
    private boolean buffEnable = true;
    private String mba = "[white]You must be [scarlet]<Admin> [white]to use this command.";
    private boolean autoBan = true;

    public Main() throws InterruptedException {
        Events.on(EventType.PlayerJoin.class, event -> {
            Player player = event.player;
            if (autoBan) {
                if (player.getInfo().timesKicked > (player.getInfo().timesJoined / 5)) {
                    String playerID = player.getInfo().id;
                    netServer.admins.banPlayer(playerID);
                    Log.info("[B] Banned " + playerID + " for (Kick) > (join)/5");
                    player.con.kick("Banned for being kicked most of the time. If you want to appeal, give the previous as reason.");
                } else if (player.getInfo().timesKicked > 15) {
                    String playerID = player.getInfo().id;
                    netServer.admins.banPlayer(playerID);
                    Log.info("[B] Banned " + playerID + " for Kick > 15.");
                    player.con.kick("Banned for being kicked than 15. If you want to appeal, give the previous as reason.");
                }
            }
            if(player.getInfo().timesKicked == 10) {
                Call.onInfoMessage(player.con,"You've been kicked 10 times, 15 kicks and you're banned.");
            }
        });

        Events.on(EventType.WorldLoadEvent.class, event -> {
            IW.clear();
            GOW.clear();
            buffList.clear();
        });

    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
        //-----USERS-----//
;
        //ping the pong
        handler.<Player>register("ping", "Pings the server", (args, player) -> {
            player.sendMessage("Got Ping!");
        });

        //Summons Entities
        handler.<Player>register("summon","[unit] [Info]", "Summons a [royal]Unit [lightgray]at a cost. do /reaper info", (arg, player) -> {
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
                                Call.onInfoMessage(player.con,"[accent]Resources needed[white]:\n3.5k \uF838 [#d99d73]copper\n[white]3.5k \uF837 [#8c7fa9]lead\n[white]2k \uF836 [#ebeef5]metaglass[white]\n[white]1.3k \uF835 [#b2c6d2]graphite[white]\n[white]1.3k \uF832 [#8da1e3]titanium[white]\n1.5k \uF831 [#f9a3c7]thorium[white]\n1.3k \uF82F [#53565c]Silicon[white]\n500 \uF82E [#cbd97f]plastanium[white]\n500 \uF82C [#f3e979]Surge Alloy");
                                return;
                            } else {
                                player.sendMessage(mba);
                            }
                        } else {
                            unit = "UnitTypes.lich";
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
                        break;
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
                    default:
                        player.sendMessage("ERROR");
                        break;
                }
            } else if (arg.length == 1){
                player.sendMessage("[salmon]Summon[white]: [salmon]Summon[white] is disabled.");
            }
        });

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
                            "\nWraith Fighter: " + wraith +
                            "\nGhoul Bomber: " + ghoul +
                            "\nRevenant: " + revenant +
                            "\nLich: " + lich +
                            "\nReaper: " + reaper +
                            "\nTotal: " + All +
                            "\n");
        });

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
        //-----ADMINS-----//

        handler.<Player>register("a","<Info> [1] [2] [3...]", "[scarlet]<Admin> [lightgray]- Admin commands", (arg, player) -> {
            if(!player.isAdmin){
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
                    if (IW.contains(player.uuid)) {
                        Teams.TeamData teamData = state.teams.get(player.getTeam());
                        CoreBlock.CoreEntity core = teamData.cores.first();
                        core.items.add(Items.copper, 100000000);
                        core.items.add(Items.lead, 100000000);
                        core.items.add(Items.metaglass, 100000000);
                        core.items.add(Items.graphite, 100000000);
                        core.items.add(Items.titanium, 100000000);
                        core.items.add(Items.thorium, 100000000);
                        core.items.add(Items.silicon, 100000000);
                        core.items.add(Items.plastanium, 100000000);
                        core.items.add(Items.phasefabric, 100000000);
                        core.items.add(Items.surgealloy, 100000000);
                        Call.sendMessage("[scarlet]<Admin> [lightgray]" + player.name + " [white] has given 100 mil resources to core.");
                    } else {
                        IW.add(player.uuid);
                        player.sendMessage("This command will add 1 mil items, use again to continue.");
                    }
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
                            player.sendMessage("[white]Player Name: " + p.getInfo().lastName +
                                    "\n[white]Names Used: " + netServer.admins.getInfo(arg[2]).names +
                                    "\n[white]IP: " + p.getInfo().lastIP +
                                    "\n[white]Times Joined: " + p.getInfo().timesJoined +
                                    "\n[white]Times Kicked: " + p.getInfo().timesKicked);
                        } else {
                            player.sendMessage("[salmon]GPI[white]: Get Player Info, use ID, not UUID, to get a player's info");
                        }
                    } else if (arg.length > 2 && arg[1].equals("uuid")) {
                        player.sendMessage("[white]Player Name: " + netServer.admins.getInfo(arg[2]).lastName +
                                "\n[white]Names Used: " + netServer.admins.getInfo(arg[2]).names +
                                "\n[white]IP: " + netServer.admins.getInfo(arg[2]).lastIP +
                                "\n[white]Times Joined: " + netServer.admins.getInfo(arg[2]).timesJoined +
                                "\n[white]Times Kicked: " + netServer.admins.getInfo(arg[2]).timesKicked);
                    } else {
                        player.sendMessage("[salmon]GPI[white]: Get Player Info, use ID or UUID, to get a player's info" +
                                "\n[salmon]GPI[white]: use arg id or uuid. example `/a gpi uuid abc123==`");
                    }
                    break;

                case "pardon": //Un-Bans players
                    if (arg.length > 1) {
                        if (arg.length > 2 && arg[2].equals("kick")) {
                            netServer.admins.getInfo(arg[1]).timesKicked = 0;
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
                            reason = arg[2] + arg[3];
                        } else if (arg.length > 2) {
                            reason = arg[2];
                        }
                        p.getInfo().timesKicked =  p.getInfo().timesKicked - 1;
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
                        if (x2.equals("")) {
                            player.sendMessage("[salmon]TP[white]: Coordinates must contain numbers!");
                            return;
                        } else if (y2.equals("")) {
                            player.sendMessage("[salmon]TP[white]: Coordinates must contain numbers!");
                            return;
                        }

                        float x2f = Float.parseFloat(x2);
                        float y2f = Float.parseFloat(y2);
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

                case "test": //test commands;
                    player.sendMessage(player.mech.name);
                    break;

                case "info": //all commands
                    player.sendMessage("\tAvailable Commands:" +
                            "\nuap              - Un Admins Player, [uud]" +
                            "\ngameover         - Triggers game over." +
                            "\ninf              - Gives 100mil of every resource to core." +
                            "\nteam             - Changes team, team" +
                            "\ngpi              - Gets Player Info, ID/UUID - ###" +
                            "\npardon           - Un-Bans a player, UUID" +
                            "\nrpk              - Resets player kick count, ID/UUID - ###" +
                            "\nbl               - Shows Ban List." +
                            "\npcc              - Closes a player connection." +
                            "\nunkick           - Un-Kicks a player, UUID." +
                            "\ntp               - Teleports player, x - y" +
                            "\nac               - Admin Chat" +
                            "\ngod              - Make yourself a god" +
                            "\ninfo             - Shows all commands and brief description.");
                    break;

                case "mms":
                    int y = -200;
                    for (int i = 0; i <= 400; i = i + 1) {
                        y = y + 1;
                        Call.onInfoMessage(player.con, String.valueOf(y));
                    }
                    break;
                case "god":
                    player.mech.health = 1000000000000000000000f;
                    player.mech.buildPower = 1000000000000000000000f;
                    player.mech.weapon.bullet.damage = 1000000000000000000000f;
                    break;
                //if none of the above commands used.
                default:
                    player.sendMessage(arg[0] + " Is not a command. Do `/a info` to see all available commands");
            }
        });
    }
}

