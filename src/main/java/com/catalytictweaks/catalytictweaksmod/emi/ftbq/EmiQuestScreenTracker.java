package com.catalytictweaks.catalytictweaksmod.emi.ftbq;

import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;

public class EmiQuestScreenTracker
{
    private static QuestScreen q_screen = null;

    public static void storeQuestScreen(QuestScreen screen)
    {
        q_screen = screen;
    }

    public static QuestScreen getAndClearQuestScreen()
    {
        QuestScreen screen = q_screen;
        q_screen = null;
        return screen;
    }

    public static boolean hasStoredQuestScreen()
    {
        return q_screen != null;
    }

    public static void clear()
    {
        q_screen = null;
    }
}