package blunivers.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.awt.Color;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;

import javax.security.auth.login.LoginException;

public class Bot {

    private char prefix = '.';
    private char argPrefix = '/';
    public static void main(String[] args) throws LoginException, InterruptedException {

        JDABuilder.createDefault(Token.token)
            .setEventManager(new AnnotatedEventManager())
            .addEventListeners(new Bot())
            .setActivity(Activity.watching("vás."))
            .enableIntents(GatewayIntent.MESSAGE_CONTENT)
            .build().awaitReady();
    }


    private static MessageChannel currentMessageChannel;

    @SubscribeEvent
    public void processMessages(MessageReceivedEvent event){
        if (event.getAuthor().isBot()) return;
        String command = event.getMessage().getContentDisplay();
        if (command.charAt(0) != prefix) return;
        String[] fullCommand = command.split(Character.toString(argPrefix));
        String[] args = {};
        if (fullCommand.length > 1) args = Arrays.copyOfRange(fullCommand, 1, fullCommand.length);
        command = fullCommand[0].substring(1);
        int numberOfArgs = args.length;

        setMessageChannel(event.getChannel());

        String[] commands = {

            "help",
            "calendar/arg/arg",

        };

        switch(command)
        {   
            case "help":
                String response = "**COMMAND LIST**\n\n`COMMAND PREFIX = {"+prefix+"}`\t`ARGUMENT PREFIX = {" + argPrefix + "}`\n\n";
                for (String item : commands) {
                    response += "." + item.replace("/arg", argPrefix + "*{arg}*") + "\n";
                }
                sendMessage(response);
                break;

            case "calendar":
                
                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM. yyyy");
                int numberOfDays = 1;
                int dayShift = 0;

                try {
                    if (numberOfArgs > 0) {
                        numberOfDays = Integer.valueOf(args[0]);
                        if (numberOfDays == 0) {
                            event.getMessage().addReaction(Emoji.fromUnicode("❔")).queue();
                            break;
                        }
                    }
                    if (numberOfArgs > 1) {
                        dayShift = Integer.valueOf(args[1]);
                    }
                    if (numberOfArgs > 2) {
                        invalidArgumentsErrorMessage_Number();

                        break;
                    }
                } catch (NumberFormatException e) {
                    invalidArgumentsErrorMessage_Type();
                    break;
                }
                
                event.getMessage().delete().queue();

                today = today.plusDays(dayShift);
                String time = "";
                int dayIndex;
                DayOfWeekCzech day;
                MessageEmbed messageEmbed;
                Random rand = new Random();
                for (int i = 0; i < numberOfDays; i++){
                    dayIndex = today.getDayOfWeek().getValue()-1;
                    if (dayIndex == 5 || dayIndex == 4) time = "20:00 ~ 23:30";
                    else time = "20:00 ~ 22:00";
                    day = DayOfWeekCzech.values()[dayIndex];
                    messageEmbed = new EmbedBuilder()
                        .setTitle(day + " | " + today.format(formatter), null)
                        .addField(time, "*Čas je jen orientační. *", false)
                        .setColor(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)))
                        
                        .build();
                    currentMessageChannel.sendMessageEmbeds(messageEmbed).queue(
                        message -> {
                            message.addReaction(Emoji.fromUnicode("✅")).queue();
                            message.addReaction(Emoji.fromUnicode("❌")).queue();
                            message.addReaction(Emoji.fromUnicode("🤷")).queue();
                        }
                    );
                    today = today.plusDays(1);
                }

                break;
        }
    }

    public void setMessageChannel(MessageChannel channel){
        currentMessageChannel = channel;
    }

    public void sendMessage(String message){ // Když je potřeba zprávu jen vyprintovat.
        currentMessageChannel.sendMessage(message).queue();
    }

    public void invalidArgumentsErrorMessage_Number(){
        System.out.println("invalidArgumentsErrorMessage_Number");
        sendMessage("Neplatný formát příkazu - nesprávný počet argumentů.");
    }

    public void invalidArgumentsErrorMessage_Type(){
        System.out.println("invalidArgumentsErrorMessage_Type");
        sendMessage("Neplatný formát příkazu - nesprávný typ argumentu.");
    }
}