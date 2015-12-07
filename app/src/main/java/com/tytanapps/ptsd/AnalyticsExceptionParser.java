package com.tytanapps.ptsd;

import android.content.Context;

import com.google.android.gms.analytics.StandardExceptionParser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * Used by Google Analytics to submit the full stack trace with any crashes
 */
public class AnalyticsExceptionParser extends StandardExceptionParser {

    public AnalyticsExceptionParser(Context context, Collection<String> additionalPackages) {
        super(context, additionalPackages);
    }

    @Override
    protected String getDescription(Throwable cause,
                                    StackTraceElement element, String threadName) {

        StringBuilder descriptionBuilder = new StringBuilder();
        final Writer writer = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(writer);
        cause.printStackTrace(printWriter);
        descriptionBuilder.append(writer.toString());
        printWriter.close();

        return descriptionBuilder.toString();
    }
}