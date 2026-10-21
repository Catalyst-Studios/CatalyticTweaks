package com.catalytictweaks.catalytictweaksmod;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public final class Logs
{
    private static final String SUMMARY_NAME = "CatalyticTweaks/EmiSummary";

    public static final Logger SUMMARY = LoggerFactory.getLogger(SUMMARY_NAME);

    private static boolean filterInstalled = false;

    private Logs() {}

    public static void apply()
    {
        applyGroup(Config.emiJemiLoggers, Config.silenceEmiLogs);
        applyGroup(Config.jeiLoggers, Config.silenceJeiLogs);
        applyGroup(Config.recipeManagerLoggers, Config.silenceRecipeManagerLogs);
        applyGroup(Config.addonLoggers, Config.silenceAddonLogs);

        if(Config.extraSilencedLoggers != null && !Config.extraSilencedLoggers.isEmpty())
        {
            applyGroup(Config.extraSilencedLoggers, true);
        }

        if(Config.ownLoggers != null)
        {
            for(String name : Config.ownLoggers)
            {
                Configurator.setLevel(name, Config.silenceEmiLogs ? Level.OFF : Level.INFO);
            }
        }

        Configurator.setLevel(SUMMARY_NAME, Level.INFO);

        installPatternFilter();
    }

    private static void installPatternFilter()
    {
        if (filterInstalled) return;

        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        config.addFilter(new AbstractFilter() {
            private boolean shouldFilter(String msg)
            {
                if (msg == null || msg.isBlank()) return false;
                List<String> patterns = Config.filteredPatterns;
                if (patterns == null || patterns.isEmpty()) return false;

                for (String pattern : patterns)
                {
                    if (pattern != null && !pattern.isBlank() && msg.contains(pattern)) return true;
                }
                return false;
            }

            @Override
            public Result filter(LogEvent event)
            {
                if (event == null || event.getMessage() == null) return Result.NEUTRAL;
                return shouldFilter(event.getMessage().getFormattedMessage()) ? Result.DENY : Result.NEUTRAL;
            }

            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, Message msg, Throwable t)
            {
                if (msg == null) return Result.NEUTRAL;
                return shouldFilter(msg.getFormattedMessage()) ? Result.DENY : Result.NEUTRAL;
            }

            @Override
            public Result filter(org.apache.logging.log4j.core.Logger logger, Level level, Marker marker, String msg, Object... params)
            {
                if (msg == null) return Result.NEUTRAL;
                return shouldFilter(msg) ? Result.DENY : Result.NEUTRAL;
            }
        });

        ctx.updateLoggers();
        filterInstalled = true;
    }

    private static void applyGroup(Iterable<String> loggers, boolean silence)
    {
        if (loggers == null) return;

        Level targetLevel = silence ? Level.OFF : null;
        for (String name : loggers)
        {
            if (name == null || name.isBlank()) continue;
            Configurator.setLevel(name, targetLevel);
        }
    }
}