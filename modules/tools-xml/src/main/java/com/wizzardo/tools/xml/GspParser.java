package com.wizzardo.tools.xml;

/**
 * Created by wizzardo on 21.07.15.
 */
public class GspParser<T extends GspParser.GspParserContext> extends HtmlParser<T> {

    public class GspParserContext extends HtmlParser.HtmlParserContext {
        protected boolean inGroovy = false;
        protected int brackets = 0;
        protected boolean inStringInGroovy = false;
        protected char quote = 0;

        @Override
        protected boolean onChar(char[] s, Node xml) {
            if (inGroovy) {
                switch (ch) {
                    case '}': {
                        sb.append('}');
                        if (!inStringInGroovy) {
                            brackets--;
                            inGroovy = brackets != 0;
                            if (!inGroovy && !inString && inTag) {
                                xml.attribute(sb.toString(), null);
                                sb.setLength(0);
                            }
                        }
                        break;
                    }
                    case '\"':
                    case '\'': {
                        if (inStringInGroovy) {
                            if (quote == ch && s[i - 1] != '\\')
                                inStringInGroovy = false;
                        } else {
                            quote = ch;
                            inStringInGroovy = true;
                        }
                        sb.append(ch);
                        break;
                    }
                    case '{': {
                        sb.append(ch);
                        if (!inStringInGroovy) {
                            brackets++;
                        }
                        break;
                    }
                    default:
                        sb.append(ch);
                }
                i++;
                return true;
            }

            if (!comment && ch == '%' && i < s.length - 3 && s[i + 1] == '{' && s[i + 2] == '-' && s[i + 3] == '-') {
                String t;
                if (!inString && !(t = trimRight(sb).toString().trim()).isEmpty()) {
                    xml.add(new TextNode(t));
                    sb.setLength(0);
                }

                comment = true;
                i += 4;
                return true;
            }

            if (comment) {
                if (ch == '-' && i < s.length - 3 && s[i + 1] == '-' && s[i + 2] == '}' && s[i + 3] == '%') {
                    if (!inTag) {
                        xml.add(addLineNumber(new GspComment(sb.toString())));
                        sb.setLength(0);
                    }
                    comment = false;
                    i += 4;
                    return true;
                } else {
                    if (!inTag)
                        sb.append(ch);
                    i++;
                    return true;
                }
            }

            return super.onChar(s, xml);
        }

        protected void onCurlyBracketOpen(char[] s, Node xml) {
            if (i > 0 && s[i - 1] == '$') {
                inGroovy = true;
                brackets++;
            }
            super.onCurlyBracketOpen(s, xml);
        }

    }

    @Override
    protected T createContext() {
        return (T) new GspParserContext();
    }

    public static class GspComment extends TextNode {
        public GspComment(String text) {
            super(text);
        }

        @Override
        public String toString() {
            return "gsp comment: " + text;
        }

        @Override
        protected String ownText() {
            return "%{--" + text + "--}%";
        }

        @Override
        public boolean isComment() {
            return true;
        }
    }
}
