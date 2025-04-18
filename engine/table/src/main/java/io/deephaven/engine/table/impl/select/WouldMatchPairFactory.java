//
// Copyright (c) 2016-2025 Deephaven Data Labs and Patent Pending
//
package io.deephaven.engine.table.impl.select;

import io.deephaven.engine.table.WouldMatchPair;
import io.deephaven.api.expression.AbstractExpressionFactory;
import io.deephaven.api.expression.ExpressionParser;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import static io.deephaven.api.expression.SelectFactoryConstants.*;

/**
 * Parses strings of the form "Column1=expression" into a {@link WouldMatchPair} (or array of them).
 */
public class WouldMatchPairFactory {
    private static final ExpressionParser<WouldMatchPair> parser = new ExpressionParser<>();
    static {
        parser.registerFactory(new AbstractExpressionFactory<WouldMatchPair>(
                START_PTRN + "(" + ID_PTRN + ")\\s*=\\s*(" + ANYTHING + ")" + END_PTRN) {
            @Override
            public WouldMatchPair getExpression(String expression, Matcher matcher, Object... args) {
                return new WouldMatchPair(matcher.group(1), matcher.group(2));
            }
        });
    }

    public static WouldMatchPair getExpression(String match) {
        return parser.parse(match);
    }

    public static WouldMatchPair[] getExpressions(String... matches) {
        return getExpressions(Arrays.stream(matches));

    }

    public static WouldMatchPair[] getExpressions(Collection<String> matches) {
        return getExpressions(matches.stream());
    }

    private static WouldMatchPair[] getExpressions(Stream<String> matchesStream) {
        return matchesStream.map(WouldMatchPairFactory::getExpression)
                .toArray(WouldMatchPair[]::new);
    }
}
