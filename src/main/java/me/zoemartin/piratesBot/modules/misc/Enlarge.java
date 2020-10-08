package me.zoemartin.piratesBot.modules.misc;

import de.androidpit.colorthief.ColorThief;
import me.zoemartin.piratesBot.core.CommandPerm;
import me.zoemartin.piratesBot.core.exceptions.CommandArgumentException;
import me.zoemartin.piratesBot.core.exceptions.UnexpectedError;
import me.zoemartin.piratesBot.core.interfaces.Command;
import me.zoemartin.piratesBot.core.util.Check;
import me.zoemartin.piratesBot.core.util.Parser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.*;
import java.util.List;

public class Enlarge implements Command {
    @Override
    public @NotNull String name() {
        return "enlarge";
    }

    @Override
    public void run(User user, MessageChannel channel, List<String> args, Message original, String invoked) {
        Check.check(args.size() == 1 && Parser.Emote.isParsable(args.get(0)), CommandArgumentException::new);

        String emoteId = Parser.Emote.parse(args.get(0));

        URL gif, png;
        int gifResponse, pngResponse;

        try {
            png = new URL("https://cdn.discordapp.com/emojis/" + emoteId + ".png?v=1");
            gif = new URL("https://cdn.discordapp.com/emojis/" + emoteId + ".gif?v=1");
            gifResponse = ((HttpURLConnection) gif.openConnection()).getResponseCode();
            pngResponse = ((HttpURLConnection) png.openConnection()).getResponseCode();
        } catch (IOException e) {
            throw new UnexpectedError();
        }

        Check.check(pngResponse == 200, UnexpectedError::new);
        EmbedBuilder eb = new EmbedBuilder();
        try {
            int[] color = ColorThief.getColor(ImageIO.read(png));
            eb.setColor(new Color(color[0], color[1], color[2]));
            //eb.setColor(averageColor(ImageIO.read(png)));
        } catch (IOException e) {
            throw new UnexpectedError();
        }


        if (gifResponse == 200) {
            eb.setImage(gif.toString());
        } else {
            eb.setImage(png.toString());
        }

        channel.sendMessage(eb.build()).queue();
    }

    @Override
    public @NotNull CommandPerm commandPerm() {
        return CommandPerm.EVERYONE;
    }

    @Override
    public @NotNull String usage() {
        return "<emote>";
    }

    @Override
    public @NotNull String description() {
        return "Enlarge an Emote";
    }

    // Unused at the moment
    public static Color averageColor(BufferedImage bi) {
        float r = 0;
        float b = 0;
        float g = 0;
        float total = 0;

        for (int y = 0; y < bi.getHeight(); y++) {
            for (int x = 0; x < bi.getWidth(); x++) {
                Color c = new Color(bi.getRGB(x, y));

                if (c.getAlpha() == 0) continue;
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
                total += c.getAlpha() / 255.0;
            }
        }

        r = r / total;
        g = g / total;
        b = b / total;

        r = r > 255 ? 255 : r;
        g = g > 255 ? 255 : g;
        b = b > 255 ? 255 : b;

        System.out.println(String.format("%s %s %s", r,g,b));

        return new Color(r/255, g/255, b/255);
    }
}
