package com.rebelsoft.console.commands;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiRenderer;

/**
 * Created by IntelliJ IDEA.
 * User: mingfang
 * Date: 3/10/11
 * Time: 12:24 PM
 */
public class Utils {
    public static String ansi(String codes, String text) {
        if (!Ansi.isDetected() || !Ansi.isEnabled()) {
            return text;
        }
        StringBuilder builder = new StringBuilder("@|");
        builder.append(codes).append(" ");
        builder.append(text);
        builder.append("|@");
        String ansi = AnsiRenderer.render(builder.toString());
        return ansi;
    }

    public static String printStep(int indent) {
        String spc = spaces(6 * indent - 4);
        StringBuilder builder = new StringBuilder(spc);
        builder.append("|\n");
        builder.append(spc);
        builder.append("+--");
        return builder.toString();
    }

    private static String spaces(int howMany) {
        return repeat(" ", howMany);
    }

    public static String repeat(String seed, int howManyTimes) {
        StringBuilder builder = new StringBuilder(seed.length() * howManyTimes);
        for (int i = 0; i <= howManyTimes; i++) {
            builder.append(seed);
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        System.out.print(spaces(14));
        System.out.println("root");
        System.out.print(printStep(3));
        System.out.print("Child");
    }

    static void append(StringBuilder builder, String label, Object... args) {
        label = ansi("bold,red", label);
        builder.append(label).append(": ");
        for (int i = 0; i < args.length; i++) {
            String arg = args[i].toString();
            builder.append(arg);
            if (i == (args.length - 1)) {
                builder.append("\n");
            } else {
                builder.append(" ");
            }
        }
    }
}
