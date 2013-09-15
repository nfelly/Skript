/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011, 2012 Peter Güttinger
 * 
 */

package ch.njol.skript.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Callback;
import ch.njol.util.CollectionUtils;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import ch.njol.util.iterator.IteratorIterable;

/**
 * @author Peter Güttinger
 */
public class Documentation { // TODO list special expressions for events

	public final static void generate() {
		if (!generate)
			return;
		try {
			final PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(Skript.getInstance().getDataFolder(), "doc.sql")), "UTF-8"));
			asSql(pw);
			pw.flush();
			pw.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
	}
	
	public final static boolean generate = Skript.testing() && new File(Skript.getInstance().getDataFolder(), "generate-doc").exists(); // don't generate the documentation on normal servers
	
	private final static void asSql(final PrintWriter pw) {
		pw.println("-- syntax elements");
//		pw.println("DROP TABLE IF EXISTS syntax_elements;");
//		pw.println("CREATE TABLE syntax_elements (" +
//				"id VARCHAR(20) NOT NULL PRIMARY KEY," +
//				"name VARCHAR(100) NOT NULL," +
//				"type ENUM('condition','effect','expression','event') NOT NULL," +
//				"patterns VARCHAR(2000) NOT NULL," +
//				"description VARCHAR(2000) NOT NULL," +
//				"examples VARCHAR(2000) NOT NULL," +
//				"since VARCHAR(100) NOT NULL" +
//				");");
		pw.println("UPDATE syntax_elements SET patterns=''");
		pw.println();
		pw.println("-- expressions");
		for (final ExpressionInfo<?, ?> e : new IteratorIterable<ExpressionInfo<?, ?>>(Skript.getExpressions())) {
			insertSyntaxElement(pw, e, "expression");
		}
		pw.println();
		pw.println("-- effects");
		for (final SyntaxElementInfo<?> info : Skript.getEffects()) {
			insertSyntaxElement(pw, info, "effect");
		}
		pw.println();
		pw.println("-- conditions");
		for (final SyntaxElementInfo<?> info : Skript.getConditions()) {
			insertSyntaxElement(pw, info, "condition");
		}
		pw.println();
		pw.println("-- events");
		for (final SkriptEventInfo<?> info : Skript.getEvents()) {
			insertEvent(pw, info);
		}
		
		pw.println();
		pw.println();
		pw.println("-- classes");
//		pw.println("DROP TABLE IF EXISTS classes;");
//		pw.println("CREATE TABLE classes (" +
//				"id VARCHAR(20) NOT NULL PRIMARY KEY," +
//				"name VARCHAR(100) NOT NULL," +
//				"description VARCHAR(2000) NOT NULL," +
//				"patterns VARCHAR(2000) NOT NULL," +
//				"`usage` VARCHAR(2000) NOT NULL," +
//				"examples VARCHAR(2000) NOT NULL," +
//				"since VARCHAR(100) NOT NULL" +
//				");");
		pw.println("UPDATE classes SET patterns=''");
		pw.println();
		for (final ClassInfo<?> c : Classes.getClassInfos()) {
			if (c.getDocName() == ClassInfo.NO_DOC)
				continue;
			if (c.getDocName() == null || c.getDescription() == null || c.getUsage() == null || c.getExamples() == null || c.getSince() == null) {
				Skript.warning("Class " + c.getCodeName() + " is missing information");
				continue;
			}
			final String desc = validateHTML(StringUtils.join(c.getDescription(), "<br/>"), "classes");
			final String usage = validateHTML(StringUtils.join(c.getUsage(), "<br/>"), "classes");
			final String since = c.getSince() == null ? "" : validateHTML(c.getSince(), "classes");
			if (desc == null || usage == null || since == null) {
				Skript.warning("Class " + c.getCodeName() + "'s description, usage or 'since' is invalid");
				continue;
			}
			insert(pw, "classes",
					"id, name, description, patterns, `usage`, examples, since",
					escapeHTML(c.getCodeName()),
					escapeHTML(c.getDocName()),
					desc,
					c.getUserInputPatterns() == null ? "" : convertRegex(StringUtils.join(c.getUserInputPatterns(), "\n")),
					usage,
					escapeHTML(StringUtils.join(c.getExamples(), "\n")),
					since);
		}
	}
	
	private final static String convertRegex(final String regex) {
		if (StringUtils.containsAny(regex, ".[]\\*+"))
			Skript.error("Regex '" + regex + "' contains unconverted Regex syntax");
		return escapeHTML(regex
				.replaceAll("\\((.+?)\\)\\?", "[$1]")
				.replaceAll("(.)\\?", "[$1]"));
	}
	
	private final static String cleanPatterns(final String patterns) {
		return StringUtils.replaceAll(
				escapeHTML(patterns) // escape HTML
				.replaceAll("(?<=[\\(\\|]).+?¦", "") // remove marks
				.replace("()", "") // remove empty mark setting groups (mark¦)
				.replaceAll("\\(([^|]+?)\\|\\)", "[$1]") // replace (mark¦x|) groups with [x]
				, "(?<!\\\\)%(.+?)(?<!\\\\)%", new Callback<String, Matcher>() { // link & fancy types
					@Override
					public String run(final Matcher m) {
						String s = m.group(1);
						if (s.startsWith("-"))
							s = s.substring(1);
						String flag = "";
						if (s.startsWith("*") || s.startsWith("~")) {
							flag = s.substring(0, 1);
							s = s.substring(1);
						}
						final int a = s.indexOf("@");
						if (a != -1)
							s = s.substring(0, a);
						final StringBuilder b = new StringBuilder("%");
						b.append(flag);
						boolean first = true;
						for (final String c : s.split("/")) {
							if (!first)
								b.append("/");
							first = false;
							final Pair<String, Boolean> p = Utils.getEnglishPlural(c);
							final ClassInfo<?> ci = Classes.getClassInfo(p.first);
							if (ci.getDocName() != null && ci.getDocName() != ClassInfo.NO_DOC) {
								b.append("<a href='../classes/#").append(p.first).append("'>").append(ci.getName().toString(p.second)).append("</a>");
							} else {
								b.append(c);
								Skript.warning("Used class " + p.first + " has no docName/name defined");
							}
						}
						return b.append("%").toString();
					}
				});
	}
	
	private final static void insertSyntaxElement(final PrintWriter pw, final SyntaxElementInfo<?> info, final String type) {
		if (info.c.getAnnotation(NoDoc.class) != null)
			return;
		if (info.c.getAnnotation(Name.class) == null || info.c.getAnnotation(Description.class) == null || info.c.getAnnotation(Examples.class) == null || info.c.getAnnotation(Since.class) == null) {
			Skript.warning("" + info.c.getSimpleName() + " is missing information");
			return;
		}
		final String desc = validateHTML(StringUtils.join(info.c.getAnnotation(Description.class).value(), "<br/>"), type + "s");
		final String since = validateHTML(info.c.getAnnotation(Since.class).value(), type + "s");
		if (desc == null || since == null) {
			Skript.warning("" + info.c.getSimpleName() + "'s description or 'since' is invalid");
			return;
		}
		final String patterns = cleanPatterns(StringUtils.join(info.patterns, "\n", 0, info.c == CondCompare.class ? 8 : info.patterns.length));
		insertOnDuplicateKeyUpdate(pw, "syntax_elements",
				"id, name, type, patterns, description, examples, since",
				"patterns = TRIM(LEADING '\n' FROM CONCAT(patterns, '\n', '" + escapeSQL(patterns) + "'))",
				escapeHTML(info.c.getSimpleName()),
				escapeHTML(info.c.getAnnotation(Name.class).value()),
				type,
				patterns,
				desc,
				escapeHTML(StringUtils.join(info.c.getAnnotation(Examples.class).value(), "\n")),
				since);
	}
	
	private final static void insertEvent(final PrintWriter pw, final SkriptEventInfo<?> info) {
		if (info.getDescription() == SkriptEventInfo.NO_DOC)
			return;
		if (info.getDescription() == null || info.getExamples() == null || info.getSince() == null) {
			Skript.warning("" + info.getName() + " (" + info.c.getSimpleName() + ") is missing information");
			return;
		}
		for (final SkriptEventInfo<?> i : Skript.getEvents()) {
			if (info.getId().equals(i.getId()) && info != i && i.getDescription() != null) {
				Skript.warning("Duplicate event id '" + info.getId() + "'");
				return;
			}
		}
		final String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "events");
		final String since = validateHTML(info.getSince(), "events");
		if (desc == null || since == null) {
			Skript.warning("description or 'since' of " + info.getName() + " (" + info.c.getSimpleName() + ") is invalid");
			return;
		}
		final String patterns = cleanPatterns(info.getName().startsWith("On ") ? "[on] " + StringUtils.join(info.patterns, "\n[on] ") : StringUtils.join(info.patterns, "\n"));
		insertOnDuplicateKeyUpdate(pw, "syntax_elements",
				"id, name, type, patterns, description, examples, since",
				"patterns = '" + escapeSQL(patterns) + "'",
				escapeHTML(info.getId()),
				escapeHTML(info.getName()),
				"event",
				patterns,
				desc,
				escapeHTML(StringUtils.join(info.getExamples(), "\n")),
				since);
	}
	
	private final static void insert(final PrintWriter pw, final String table, final String fields, final String... values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = escapeSQL(values[i]);
		}
		pw.println("INSERT INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "');");
	}
	
	private final static void insertOnDuplicateKeyUpdate(final PrintWriter pw, final String table, final String fields, final String update, final String... values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = escapeSQL(values[i]);
		}
		pw.println("INSERT IGNORE INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "') ON DUPLICATE KEY UPDATE " + update + ";");
	}
	
	private static ArrayList<Pattern> validation = new ArrayList<Pattern>();
	static {
		validation.add(Pattern.compile("<" + "(?!a href='|/a>|br ?/|/?(i|b|u|code|pre|ul|li)>)"));
		validation.add(Pattern.compile("(?<!</a|'|br ?/|/?(i|b|u|code|pre|ul|li))" + ">"));
	}
	
	private final static String[] urls = {"expressions", "effects", "conditions"};
	
	private final static String validateHTML(String html, final String baseURL) {
		for (final Pattern p : validation) {
			if (p.matcher(html).find())
				return null;
		}
		html = html.replaceAll("&(?!(lt|gt);)", "&amp;");
		final Matcher m = Pattern.compile("<a href='(.*?)'>").matcher(html);
		linkLoop: while (m.find()) {
			final String url = m.group(1);
			final String[] s = url.split("#");
			if (s.length == 1)
				continue;
			if (s[0].isEmpty())
				s[0] = "../" + baseURL + "/";
			if (s[0].startsWith("../") && s[0].endsWith("/")) {
				if (s[0].equals("../classes/")) {
					if (Classes.getClassInfoNoError(s[1]) != null)
						continue;
				} else if (s[0].equals("../events/")) {
					for (final SkriptEventInfo<?> i : Skript.getEvents()) {
						if (s[1].equals(i.getId()))
							continue linkLoop;
					}
				} else {
					final int i = CollectionUtils.indexOf(urls, s[0].substring("../".length(), s[0].length() - 1));
					if (i != -1) {
						try {
							Class.forName("ch.njol.skript." + urls[i] + "." + s[1]);
							continue;
						} catch (final ClassNotFoundException e) {}
					}
				}
			}
			Skript.warning("invalid link '" + url + "' found in '" + html + "'");
		}
		return html;
	}
	
	private final static String escapeSQL(final String s) {
		return s.replace("'", "\\'").replace("\"", "\\\"");
	}
	
	public final static String escapeHTML(final String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
	
}
