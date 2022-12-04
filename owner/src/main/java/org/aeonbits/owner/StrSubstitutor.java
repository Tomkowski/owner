/*
 * Copyright (c) 2012-2015, Luigi R. Viggiano
 * All rights reserved.
 *
 * This software is distributable under the BSD license.
 * See the terms of the BSD license in the documentation provided with this software.
 */

package org.aeonbits.owner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * <p>
 * Substitutes variables within a string by values.
 * </p>
 * <p>
 * This class takes a piece of text and substitutes all the variables within it. The definition of a variable is
 * <code>${variableName}</code>.
 * </p>
 * <p>
 * Typical usage of this class follows the following pattern: First an instance is created and initialized with the
 * values that contains the values for the available variables. If a prefix and/or suffix for variables should be used
 * other than the default ones, the appropriate settings can be performed. After that the <code>replace()</code> method
 * can be called passing in the source text for interpolation. In the returned text all variable references (as long as
 * their values are known) will be resolved. The following example demonstrates this:
 * </p>
 * <pre>
 * Map valuesMap = new HashMap();
 * valuesMap.put(&quot;animal&quot;, &quot;quick brown fox&quot;);
 * valuesMap.put(&quot;target&quot;, &quot;lazy dog&quot;);
 * String templateString = &quot;The ${animal} jumped over the ${target}.&quot;;
 * StrSubstitutor sub = new StrSubstitutor(valuesMap);
 * String resolvedString = sub.replace(templateString);
 * </pre>
 * yielding:
 * <pre>
 *      The quick brown fox jumped over the lazy dog.
 * </pre>
 *
 * @author Luigi R. Viggiano
 */
class StrSubstitutor implements Serializable {

    private final Properties values;
    private static final Pattern PATTERN = compile("\\$\\{(.+?)}");

    /**
     * Creates a new instance and initializes it. Uses defaults for variable prefix and suffix and the escaping
     * character.
     *
     * @param values the variables' values, may be null
     */
    StrSubstitutor(Properties values) {
        this.values = values;
    }

    /**
     * Replaces all the occurrences of variables with their matching values from the resolver using the given source
     * string as a template.
     *
     * @param source the string to replace in, null returns null
     * @return the result of the replace operation
     */
    String replace(String source) {
        if (source == null)
            return null;
        StringBuilder sb = new StringBuilder();
        List<String> groups = getVariableExpansions(source);
        if (groups.isEmpty()) return source;
        for (String group : groups) {
            String value = values.getProperty(group);
            String replacement = (value != null) ? replace(value) : (group.matches(PATTERN.pattern()) ? replace(group) : "");
            source = source.replaceFirst(Pattern.quote(String.format("${%s}", group)), Matcher.quoteReplacement(replacement));
        }
        sb.append(source);
        return sb.toString();
    }

    /**
     * Returns a string modified in according to supplied source and arguments.<br/>
     * If the source string has pattern-replacement content like {@code "a.${var}.b"},
     * the pattern is replaced property value of "var".<br/>
     * Otherwise the return string is formatted by source and arguments as with {@link String#format(String, Object...)}
     *
     * @param source A source formatting format string. {@code null} returns {@code null}
     * @param args   Arguments referenced by the format specifiers in the source string.
     * @return formatted string
     */
    String replace(String source, Object... args) {
        if (source == null)
            return null;
        Matcher m = PATTERN.matcher(source);
        return m.find() ? replace(source) : String.format(source, args);
    }

    /**
     * Finds all top level variable expansion expressions and returns it as a list.
     * E.g.: foo.${bar.${baz}}.${biz} -> [bar.${baz}, biz]
     *
     * @param expression the string for which variable expansion expressions are queried, null returns null
     * @return list of top level variable expansion expressions
     */
    private List<String> getVariableExpansions(String expression) {
        if (expression == null) return null;

        final List<String> variables = new ArrayList<String>();
        final String variableExpressionBeginning = "${";
        int indexOfFirstVariableExpansion = expression.indexOf(variableExpressionBeginning);
        if (indexOfFirstVariableExpansion == -1) return variables;

        final int expressionLength = expression.length();
        indexOfFirstVariableExpansion += variableExpressionBeginning.length();
        final int variableStartIndex = indexOfFirstVariableExpansion;
        int bracketCounter = 1;

        for (int index = indexOfFirstVariableExpansion; index < expressionLength; index++) {
            if (expression.charAt(index) == '{') {
                bracketCounter += 1;
            }
            if (expression.charAt(index) == '}') bracketCounter -= 1;
            if (bracketCounter == 0) {
                variables.add(expression.substring(variableStartIndex, index));
                variables.addAll(getVariableExpansions(expression.substring(index + 1, expressionLength)));
                break;
            }
        }
        return variables;
    }
}