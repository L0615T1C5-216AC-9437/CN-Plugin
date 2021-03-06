package CN.dCommands;

import CN.Main;
import CN.byteCode;
//mindustry + arc
import CN.key;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.entities.type.Player;
//javacord

import mindustry.net.Administration;
import mindustry.world.modules.ItemModule;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import org.json.JSONObject;

public class discordCommands implements MessageCreateListener {

    private JSONObject data;


    public discordCommands(){}

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        data = byteCode.get("settings");

        if (data.has("prefix"+Administration.Config.port.num()) && data.has("bot_channel_id") && event.getChannel().getIdAsString().equals(data.getString("bot_channel_id"))){
            String[] arg = event.getMessageContent().split(" ", 4);
            //playerlist
            if (event.getMessageContent().equalsIgnoreCase("//players") || event.getMessageContent().startsWith(data.getString("prefix"+Administration.Config.port.num()) + "players")) {
                StringBuilder lijst = new StringBuilder();
                lijst.append("players: " + Vars.playerGroup.size() + "\n");
                //lijst.append("online admins: " + Vars.playerGroup.all().count(p->p.isAdmin)+"\n");
                for (Player p : Vars.playerGroup.all()) {
                    lijst.append("* " + p.name.trim() + "\n");
                }
                new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
            }
            //info
            else if (event.getMessageContent().equalsIgnoreCase("//info") || event.getMessageContent().startsWith(data.getString("prefix"+Administration.Config.port.num()) + "info")) {
                try {
                    StringBuilder lijst = new StringBuilder();
                    lijst.append("map: " + Vars.world.getMap().name() + "\n" + "author: " + Vars.world.getMap().author() + "\n");
                    lijst.append("wave: " + Vars.state.wave + "\n");
                    lijst.append("enemies: " + Vars.state.enemies + "\n");
                    lijst.append("players: " + Vars.playerGroup.size() + '\n');
                    //lijst.append("admins (online): " + Vars.playerGroup.all().count(p -> p.isAdmin));
                    new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
            //resoirces in core
            else if (event.getMessageContent().equalsIgnoreCase("//infores") || event.getMessageContent().startsWith(data.getString("prefix"+Administration.Config.port.num()) + "infores")) {
                //event.getChannel().sendMessage("not implemented yet...");
                if (!Vars.state.rules.waves) {
                    event.getChannel().sendMessage("Only available when playing survival mode!");
                    return;
                } else if (Vars.playerGroup.isEmpty()) {
                    event.getChannel().sendMessage("No players online!");
                } else {
                    StringBuilder lijst = new StringBuilder();
                    lijst.append("amount of items in the core\n\n");
                    ItemModule core = Vars.playerGroup.all().get(0).getClosestCore().items;
                    lijst.append("copper: " + core.get(Items.copper) + "\n");
                    lijst.append("lead: " + core.get(Items.lead) + "\n");
                    lijst.append("graphite: " + core.get(Items.graphite) + "\n");
                    lijst.append("metaglass: " + core.get(Items.metaglass) + "\n");
                    lijst.append("titanium: " + core.get(Items.titanium) + "\n");
                    lijst.append("thorium: " + core.get(Items.thorium) + "\n");
                    lijst.append("silicon: " + core.get(Items.silicon) + "\n");
                    lijst.append("plastanium: " + core.get(Items.plastanium) + "\n");
                    lijst.append("phase fabric: " + core.get(Items.phasefabric) + "\n");
                    lijst.append("surge alloy: " + core.get(Items.surgealloy) + "\n");

                    new MessageBuilder().appendCode("", lijst.toString()).send(event.getChannel());
                }
            }
            //get verified
            else if (event.getMessageContent().startsWith(data.getString("prefix"+Administration.Config.port.num()) + "verify")) {
                if (byteCode.has("discord_accounts")) {
                    JSONObject da = byteCode.get("discord_accounts");
                    if (da == null){
                        Log.err("mind_db/ does not contain `da.cn`");
                        event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ERROR da.cn -> Please contact a Admin");
                        return;
                    }
                    if (da.has(event.getMessage().getAuthor().getIdAsString())) {
                        event.getChannel().sendMessage("Discord Account already in use!");
                        return;
                    } else {
                        if (arg.length > 1) {
                            JSONObject login = byteCode.get("login_info");
                            if (login == null){
                                Log.err("mind_db/ does not contain `login_info.cn`");
                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ERROR login_info.cn -> Please contact a Admin");
                                return;
                            }
                            if (login.has(arg[1])) {
                                JSONObject user = login.getJSONObject(arg[1]);
                                JSONObject data = byteCode.get(user.getString("dataID"));
                                if (data == null){
                                    Log.err("mind_db/ does not contain dataID for " + arg[1]);
                                    event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, ERROR dataID -> Please contact a Admin");
                                    return;
                                }
                                if (data.has("verified") && data.getInt("verified") == 1) {
                                    event.getChannel().sendMessage("Username not found in database or already verified");
                                    return;
                                }
                                String hash = byteCode.hash(8);
                                Main.keyList.put(hash, new key(arg[1],"verify", event.getMessage().getAuthor().getDiscriminatedName()));
                                byteCode.putStr("discord_accounts", event.getMessage().getAuthor().getIdAsString(), arg[1]);
                                String userID = event.getMessage().getAuthor().getIdAsString();
                                event.getChannel().sendMessage("By getting verified, you agree to the following:" +
                                        "\n```1) having your discord tag saved on server" +
                                        "\n2) having your discord tag sharable in-game" +
                                        "\n3) having your <verified> revoked at any given time ```" +
                                        "\n\ndo ||/key " + hash + "|| to get verified! You have 30s!");
                                new Object() {
                                    private Timer.Task task;

                                    {
                                        task = Timer.schedule(() -> {
                                            if (Main.keyList.containsKey(hash)) {
                                                Main.keyList.remove(hash);
                                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Key Expired");
                                                byteCode.remove("discord_accounts", userID);
                                                task.cancel();
                                            } else {
                                                event.getChannel().sendMessage("<@" + event.getMessage().getAuthor().getIdAsString() + ">, Successfully verified your account!");
                                                task.cancel();
                                            }
                                        }, 30, 1);
                                    }
                                };
                            } else {
                                event.getChannel().sendMessage("Username not found in database or already verified");
                            }
                        } else {
                            event.getChannel().sendMessage("Provide the username of the account that's being activated.");
                            return;
                        }
                    }
                } else {
                    Log.err("============");
                    Log.err("ERROR - 404");
                    Log.err("/mind_db/ does not contain `discord_accounts.cn`");
                    Log.err("============");
                    event.getChannel().sendMessage("ERROR: Please contact a mindustry admin\nERROR: Missing login_info.cn");
                }
            }
            else if (event.getMessageContent().equalsIgnoreCase("/key")) {
                event.getChannel().sendMessage("You are supposed to do that in game chat!");
            }
        } else if (!data.has("prefix"+ Administration.Config.port.num())) {
            Log.err("============");
            Log.err("CRITICAL ERROR - 404");
            Log.err("settings.cn does not contain `prefix"+Administration.Config.port.num()+"`");
            Log.err("============");
        }
    }
}
