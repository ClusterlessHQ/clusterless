/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.printer;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import picocli.CommandLine;

import java.io.*;
import java.util.Collection;
import java.util.Map;

import static clusterless.printer.Printer.Format.table;
import static com.github.jknack.handlebars.internal.lang3.Validate.notNull;

/**
 *
 */
public class Printer {
    public enum Format {
        table,
        json,
        csv,
        tsv
    }

    private static final Handlebars handlebars = new Handlebars()
            .prettyPrint(false);

    static {
        StringHelpers.register(handlebars);
        handlebars.registerHelper("indent", Printer::indent);
    }

    @CommandLine.Option(
            names = {"--output"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "print results using given format"
    )
    Format format = table;
    private PrintStream out = System.out;

    public Printer() {
    }

    public Format format() {
        return format;
    }

    public void println(Collection<String> strings) {
        strings.forEach(this::println);
    }

    public void println(String string) {
        out.println(string);
    }

    public BufferedWriter writer() {
        return new BufferedWriter(new OutputStreamWriter(out)) {
            @Override
            public void close() {
                // do nothing
            }
        };
    }

    public void writeWithTemplate(String template, Map<String, Object> params, Writer writer) {
        try {
            Context context = Context
                    .newBuilder(params)
                    .resolver(MapValueResolver.INSTANCE)
                    .build();

            Template compile = handlebars.compile(template);

            compile.apply(context, writer);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected static CharSequence indent(final Object value, final Options options) {
        Integer width = options.param(0, 4);
        notNull(width, "found 'null', expected 'indent'");

        return value.toString()
                .trim()
                .indent(width);
    }
}
