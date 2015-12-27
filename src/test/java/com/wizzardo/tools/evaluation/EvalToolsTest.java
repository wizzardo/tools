/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @author Moxa
 */
public class EvalToolsTest {

    public EvalToolsTest() {
    }

    /**
     * Test of evaluate method, of class EvalTools.
     */
    @Test
    public void testEvaluateLine() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();


        assertEquals(1, EvalTools.evaluate("java.lang.Math.abs(-1)"));

        assertEquals(1, EvalTools.evaluate("(1)"));
        assertEquals(1, EvalTools.evaluate("((1))"));
        assertEquals(2, EvalTools.evaluate("1+1"));
        assertEquals(2.0, EvalTools.evaluate("1+1.0"));
        assertEquals(5, EvalTools.evaluate("1+1+3"));
        assertEquals("olo123", EvalTools.evaluate("\"olo\"+1+(1+1)+3"));
        assertEquals("olo123", EvalTools.evaluate("'olo'+1+(1+1)+3"));
        assertEquals("OLO123", EvalTools.evaluate("(\"olo\"+1+(1+1)+3).toUpperCase()"));
        assertEquals("ololo", EvalTools.evaluate("\"olo\".concat(\"lo\")"));

        model.put("ololo", "qwerty");
        model.put("qwe", "  ololo  ");
        model.put("length111", "111");
        assertEquals("QWERTYOLOLO123", EvalTools.evaluate("ololo.concat(qwe.trim().substring(2)).concat(qwe.trim().substring(length111.length()) + 123).toUpperCase()", model));

        assertEquals(5f, EvalTools.evaluate("10f/2"));
        assertEquals(7, EvalTools.evaluate("1+2*3"));
        assertEquals(9, EvalTools.evaluate("1+2*(1+3)"));


        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalTools.evaluate("i++", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(2, EvalTools.evaluate("i++ + 1", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(3, EvalTools.evaluate("++i + 1", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(4, EvalTools.evaluate("++i + i++", model));
        assertEquals(3, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(5, EvalTools.evaluate("++i + ++i", model));
        assertEquals(3, model.get("i"));


        assertEquals(true, EvalTools.evaluate("true"));
        assertEquals(false, EvalTools.evaluate("false"));
        assertEquals(true, EvalTools.evaluate("!false"));


        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalTools.evaluate("i--", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(0, EvalTools.evaluate("i-- - 1", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(-1, EvalTools.evaluate("--i - 1", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(0, EvalTools.evaluate("--i - i--", model));
        assertEquals(-1, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalTools.evaluate("--i - --i", model));
        assertEquals(-1, model.get("i"));


        assertEquals(true, EvalTools.evaluate("1>0"));
        assertEquals(true, EvalTools.evaluate("1>=1"));
        assertEquals(true, EvalTools.evaluate("5>\"123\".length()"));
        assertEquals(false, EvalTools.evaluate("1>\"123\".length()"));
        assertEquals(false, EvalTools.evaluate("1<0"));
        assertEquals(true, EvalTools.evaluate("2<=1*2"));
        assertEquals(true, EvalTools.evaluate("2>=1*2"));
        assertEquals(true, EvalTools.evaluate("2== 1 +(3-2)*2-1"));


        model = new HashMap<String, Object>();
        model.put("a", "ololo");
        assertEquals(6, EvalTools.evaluate("a!=null?a.length()+1:1+\"ololo\"", model));
        assertEquals("1ololo", EvalTools.evaluate("a==null?a.length()+1:1+\"ololo\"", model));
        assertEquals(15, EvalTools.evaluate("4+(a!=null?a.length()+1:1+\"ololo\")+5", model));


        model = new HashMap<String, Object>();
        model.put("i", 0);
        assertEquals(5, EvalTools.evaluate("i+=5", model));
        assertEquals(10, EvalTools.evaluate("i*=2", model));
        assertEquals(2, EvalTools.evaluate("i/=5", model));
        assertEquals(0, EvalTools.evaluate("i-=2", model));
        assertEquals(3, EvalTools.evaluate("i+=1+2", model));


        model = new HashMap<String, Object>();
        model.put("i", 0);
        assertEquals(3, EvalTools.evaluate("i=1+2", model));
        assertEquals(3, model.get("i"));


        System.out.println("test logic!");
        model = new HashMap<String, Object>();
        model.put("i", 0);
        int i = 0;
        boolean b = i++ > 1 || i++ > 1 || i++ > 1 || i++ > 1;
        assertEquals(b, EvalTools.evaluate("i++>1||i++>1||i++>1||i++>1", model));
        assertEquals(i, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ > 1 || i++ > 1 || i++ > 1 | i++ > 1;
        assertEquals(b, EvalTools.evaluate("i++>1||i++>1||i++>1|i++>1", model));
        assertEquals(i, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        assertEquals(true, EvalTools.evaluate("x<++x", model));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        model.put("i", 1);
        model.put("n", 1);
        assertEquals(true, EvalTools.evaluate("i<n&&x++<x?false:true", model));
        assertEquals(0, model.get("x"));

        assertEquals("c", EvalTools.evaluate("false ? \"a\" : false ? \"b\" : \"c\"", model));
        assertEquals("a", EvalTools.evaluate("true ? \"a\" : false ? \"b\" : \"c\"", model));
        assertEquals("b", EvalTools.evaluate("false ? \"a\" : true ? \"b\" : \"c\"", model));
        assertEquals("a", EvalTools.evaluate("true ? \"a\" : true ? \"b\" : \"c\"", model));
        assertEquals("b", EvalTools.evaluate("true ? true ? \"b\" : \"c\" : \"a\"", model));
        assertEquals("c", EvalTools.evaluate("true ? false ? \"b\" : \"c\" : \"a\"", model));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ < 1 && i++ < 1 && i++ < 1 && i++ < 1;
        assertEquals(b, EvalTools.evaluate("i++ < 1 && i++ < 1 && i++ < 1 && i++ < 1", model));
        assertEquals(i, model.get("i"));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ < 1 & i++ < 1 & i++ < 1 & i++ < 1;
        assertEquals(b, EvalTools.evaluate("i++ < 1 & i++ < 1 & i++ < 1 & i++ < 1", model));
        assertEquals(i, model.get("i"));
        assertEquals(4, model.get("i"));


        assertEquals(true, EvalTools.evaluate("true&&(false|!true) | 3>2 ", model));

        System.out.println("test static methods");
        assertEquals(1, EvalTools.evaluate("java.lang.Math.abs(-1)"));
        assertEquals(1, EvalTools.evaluate("Math.abs(-1)"));
        assertEquals(2, EvalTools.evaluate("Math.abs(-1)+Math.abs(-1)"));
        assertEquals(2d, EvalTools.evaluate("Math.sqrt(2+2)"));
        assertEquals(4d, EvalTools.evaluate("Math.pow(2,(2*2)/(2))"));
        assertEquals(Math.PI, EvalTools.evaluate("Math.PI"));

        System.out.println("test constructors");
        assertEquals("ololo", EvalTools.evaluate("new String(\"ololo\")"));

        System.out.println("test fields");
        assertEquals(1, EvalTools.evaluate("new java.awt.Point(1,2).x"));
        assertEquals(3, EvalTools.evaluate("new java.awt.Point(1,2).x + new java.awt.Point(1,2).y"));


        System.out.println("test user functions");
        UserFunction y = new UserFunction("y", "x*2", "x");
        functions = new HashMap<String, UserFunction>();
        functions.put(y.getName(), y);
        assertEquals(10, EvalTools.evaluate("y(5)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        assertEquals(8, EvalTools.evaluate("y(2*(10/5))", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(15, EvalTools.evaluate("z(5)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(11, EvalTools.evaluate("z(5) - y(2)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(9, EvalTools.evaluate("z(5) - z(2)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "2*2"));
        assertEquals(4, EvalTools.evaluate("y()", null, functions));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        model.put("g", 0);
        functions = new HashMap<String, UserFunction>();
        functions.put("it", new UserFunction("it", "i<end&&(g=++x)==g?it(i+1,end):g", "i", "end"));
        assertEquals(10, EvalTools.evaluate("it(0,10)", model, functions));
        assertEquals(10, model.get("x"));

        assertEquals(true, EvalTools.evaluate("(Math.sin((3.1416/2))) > 0"));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        assertEquals(0, EvalTools.evaluate("x = x++", model));


        System.out.println("test variables definition");
        model = new HashMap<String, Object>();
        assertEquals(1, EvalTools.evaluate("def x = 1", model));
        assertEquals(1, model.get("x"));


        System.out.println("test collections");
        model = new HashMap<String, Object>();
        assertTrue(EvalTools.evaluate("def x = []", model) instanceof List);
        assertEquals("[1]", EvalTools.evaluate("x << 1", model).toString());
        assertEquals(1, ((List) model.get("x")).size());
        assertEquals(1, ((List) model.get("x")).get(0));
        assertEquals("[1, 2, 3]", EvalTools.evaluate("x << 2 << 3", model).toString());
        assertEquals("[1, 2, 3, 4]", EvalTools.evaluate("x + 4", model).toString());
        assertEquals("[1, 2, 3, 5]", EvalTools.evaluate("x += 5", model).toString());

        assertEquals(1, ((List) EvalTools.evaluate("[1,[2,3]]")).get(0));
        assertEquals("[2, 3]", ((List) EvalTools.evaluate("[1,[2,3]]")).get(1).toString());

        assertEquals("qwerty", ((Map) EvalTools.evaluate("[qwe:\"qwerty\"]")).get("qwe").toString());
        assertEquals("qwe\",rty", ((Map) EvalTools.evaluate("[qwe:\"qwe\\\",rty\"]")).get("qwe").toString());
        assertEquals(1, ((Map) ((Map) EvalTools.evaluate("[qwe:\"qwe\\\",rty\",olo:[asd:1]]")).get("olo")).get("asd"));
        assertEquals(1, EvalTools.evaluate("[qwe:\"qwe\\\",rty\",olo:[asd:1]].olo.asd"));

        model = new HashMap<String, Object>();
        model.put("x", 1);
        assertEquals("[1]", EvalTools.evaluate("[x]", model).toString());

        assertTrue(EvalTools.evaluate("[:]", model) instanceof Map);

        model = new HashMap<String, Object>();
        assertTrue(EvalTools.evaluate("def x = [:]", model) instanceof Map);
        assertEquals("value", EvalTools.evaluate("x.key = \"value\"", model));
        assertEquals("value", ((Map) model.get("x")).get("key"));
        assertEquals(1, EvalTools.evaluate("x.key = 1", model));
        assertEquals(1, EvalTools.evaluate("x.key++", model));
        assertEquals(2, ((Map) model.get("x")).get("key"));
        assertEquals(4, EvalTools.evaluate("x.key+=2", model));
        assertEquals(12, EvalTools.evaluate("x.key*=3", model));
        assertEquals(3, EvalTools.evaluate("x.key/=4", model));
        assertEquals(0, EvalTools.evaluate("x.key-=3", model));
        assertEquals(1, EvalTools.evaluate("++x.key", model));
        assertEquals(0, EvalTools.evaluate("--x.key", model));
        assertEquals(0, EvalTools.evaluate("x.key--", model));
        assertEquals(-1, ((Map) model.get("x")).get("key"));

        model = new HashMap<String, Object>();
        Point p = new Point(0, 0);
        model.put("p", p);
        assertEquals(0, EvalTools.evaluate("p.x", model));
        assertEquals(0, EvalTools.evaluate("p.x++", model));
        assertEquals(2, EvalTools.evaluate("++p.x", model));
        assertEquals(2, p.x);


        model = new HashMap<String, Object>();
        assertTrue(EvalTools.evaluate("def x = [:]", model) instanceof Map);
        assertEquals(1, EvalTools.evaluate("x[\"key\"] = 1", model));
        assertEquals(1, EvalTools.evaluate("x[\"key\"]", model));
        assertEquals(1, ((Map) model.get("x")).get("key"));
        assertEquals(3, EvalTools.evaluate("x[\"key\"] +=2", model));

        model = new HashMap<String, Object>();
        model.put("arr", new String[1]);
        assertEquals("ololo", EvalTools.evaluate("arr[0] = \"ololo\"", model));

        model = new HashMap<String, Object>();
        assertTrue(EvalTools.evaluate("def l = []", model) instanceof List);
        assertEquals(1, EvalTools.evaluate("l[0] = 1", model));
        assertEquals(1, EvalTools.evaluate("l[0]", model));
        assertEquals(2, EvalTools.evaluate("l[2]=2", model));
        assertEquals("[1, null, 2]", EvalTools.evaluate("l", model).toString());

        model = new HashMap<String, Object>();
        assertTrue(EvalTools.evaluate("def m = [:]", model) instanceof Map);
        assertTrue(EvalTools.evaluate("m.a = [:]", model) instanceof Map);
        assertEquals(1, EvalTools.evaluate("m[\"a\"][\"b\"] = 1", model));
        assertEquals(1, EvalTools.evaluate("m.a.b = 1", model));

        assertEquals("[{id=1}, {id=2}, {id=3}]", EvalTools.evaluate("[[id:1],[id:2],[id:3]]").toString());


        assertEquals(5, EvalTools.evaluate("(0..5).size()"));
        assertEquals(5, EvalTools.evaluate("(1+2-3 .. 5).size()"));

        model = new HashMap<String, Object>();
        EvalTools.evaluate("def x=0..10", model);

        assertEquals(5, EvalTools.evaluate("x.get(5)", model));
        boolean exception = false;
        try {
            EvalTools.evaluate("x.put(5)", model);
        } catch (Exception e) {
            Assert.assertEquals(NoSuchMethodException.class, e.getClass());
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            EvalTools.evaluate("x.b", model);
        } catch (Exception e) {
            Assert.assertEquals(NoSuchFieldException.class, e.getClass());
            exception = true;
        }
        assertTrue(exception);

        model.clear();
        model.put("it", 1);
        model.put("pageNumber", 0);
        assertEquals("", EvalTools.evaluate(" it == pageNumber ? 'style=\"font-size: 20px;\"' : ''", model));
        model.put("it", 0);
        assertEquals("style=\"font-size: 20px;\"", EvalTools.evaluate(" it == pageNumber ? 'style=\"font-size: 20px;\"' : ''", model));

        assertNotNull(EvalTools.evaluate("Math.sin((1+2)/(3))"));


        model = new HashMap<String, Object>();
        assertEquals("false", EvalTools.evaluate("m?.a ?: 'false'", model));
        assertEquals("false", EvalTools.evaluate("m?.a?.b ?: 'false'", model));
        assertEquals("{a={b=true}}", EvalTools.evaluate("m = [a:[b:'true']]", model).toString());
        assertEquals("true", EvalTools.evaluate("m?.a?.b ?: 'false'", model));

        model = new HashMap<String, Object>();
        assertEquals("false", EvalTools.evaluate("a?.concat('b') ?: 'false'", model));
        model.put("a", "a");
        assertEquals("ab", EvalTools.evaluate("a?.concat('b') ?: 'false'", model));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalTools.evaluate("i++?:0", model));
        assertEquals(2, EvalTools.evaluate("i", model));
    }

    @Test
    public void testClone() throws Exception {
        String exp = "1+\"ololo\".substring(2)";
        Expression eh = EvalTools.prepare(exp);
        Object ob1 = eh.get(null);
        Object ob2 = eh.get(null);
        assertTrue(ob1 == ob2);
        eh = eh.clone();
        Object ob3 = eh.get(null);
        assertTrue(ob1 != ob3);
        assertTrue(ob1.equals(ob3));
    }

    @Test
    public void testSimplifying() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Expression eh;

        eh = EvalTools.prepare("1 + a - 3");
        model.put("a", 2);
        Assert.assertEquals(0, eh.get(model));
        Assert.assertEquals(0, eh.get(model));
        Assert.assertTrue(((Operation) eh).leftPart().hardcoded);

        eh = EvalTools.prepare("4 * a / 2");
        model.put("a", 2);
        Assert.assertEquals(4, eh.get(model));
        Assert.assertEquals(4, eh.get(model));
        Assert.assertTrue(((Operation) eh).leftPart().hardcoded);

        eh = EvalTools.prepare("a + 2 - 3");
        model.put("a", 2);
        Assert.assertEquals(1, eh.get(model));
        Assert.assertEquals(1, eh.get(model));
        Assert.assertTrue(((Operation) eh).leftPart().hardcoded);

        eh = EvalTools.prepare("a - 2 + 3");
        model.put("a", 2);
        Assert.assertEquals(3, eh.get(model));
        Assert.assertEquals(3, eh.get(model));
        Assert.assertTrue(((Operation) eh).leftPart().hardcoded);

        eh = EvalTools.prepare("a * 6 / 3");
        model.put("a", 4);
        Assert.assertEquals(8, eh.get(model));
        Assert.assertEquals(8, eh.get(model));
        Assert.assertTrue(((Operation) eh).leftPart().hardcoded);

        eh = EvalTools.prepare("a / 1 * 3");
        model.put("a", 2);
        Assert.assertEquals(6, eh.get(model));
        Assert.assertEquals(6, eh.get(model));
        Assert.assertTrue(((Operation) eh).leftPart().hardcoded);

        eh = EvalTools.prepare("a - 3 + 'b'");
        model.put("a", 2);
        Assert.assertEquals("-1b", eh.get(model));
        Assert.assertEquals("-1b", eh.get(model));
        Assert.assertTrue(((Operation) eh).rightPart().hardcoded);
        Assert.assertEquals("b", ((Operation) eh).rightPart().result);

        eh = EvalTools.prepare("a + 'b' + 2");
        model.put("a", 2);
        Assert.assertEquals("2b2", eh.get(model));
        Assert.assertEquals("2b2", eh.get(model));
        Assert.assertTrue(((Operation) eh).rightPart().hardcoded);
        Assert.assertEquals(2, ((Operation) eh).rightPart().result);
    }

    @Test
    public void testTrimBrackets() throws Exception {
        assertEquals("sin((1+2)/(3))", EvalTools.trimBrackets("sin((1+2)/(3))"));
        assertEquals("sin((1+2)/3)", EvalTools.trimBrackets("(sin((1+2)/3))"));
        assertEquals("sin((1+2)/3)", EvalTools.trimBrackets("((sin((1+2)/3)))"));
    }

    @Test
    public void testVariable() throws Exception {
        Expression eh;

        eh = EvalTools.prepare("a=2");
        Variable a = new Variable("a", 0);
        eh.setVariable(a);
        eh.get();

        Assert.assertEquals(2, a.get());
    }

    @Test
    public void testGetParts() throws Exception {
        List<String> l = EvalTools.getParts("sin(1)+2");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("sin", l.get(0));
        Assert.assertEquals("(1)", l.get(1));
        Assert.assertEquals("+2", l.get(2));

        l = EvalTools.getParts("sin(1)");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("sin", l.get(0));
        Assert.assertEquals("(1)", l.get(1));

        l = EvalTools.getParts("'(olo)'+1");
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("'(olo)'+1", l.get(0));

        l = EvalTools.getParts("(1+2)*3");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("(1+2)", l.get(0));
        Assert.assertEquals("*3", l.get(1));

        l = EvalTools.getParts("'qwe'.concat('rty')");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("'qwe'", l.get(0));
        Assert.assertEquals(".concat", l.get(1));
        Assert.assertEquals("('rty')", l.get(2));

        l = EvalTools.getParts("('qwe').concat('rty')");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("('qwe')", l.get(0));
        Assert.assertEquals(".concat", l.get(1));
        Assert.assertEquals("('rty')", l.get(2));

        l = EvalTools.getParts("k[0]");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));

        l = EvalTools.getParts("k[0].x");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));
        Assert.assertEquals(".x", l.get(2));

        l = EvalTools.getParts("k[0]?.x");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));
        Assert.assertEquals("?.x", l.get(2));

        l = EvalTools.getParts("k[0]?.x?.y");
        Assert.assertEquals(4, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));
        Assert.assertEquals("?.x", l.get(2));
        Assert.assertEquals("?.y", l.get(3));

        l = EvalTools.getParts("k[0][1]");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));
        Assert.assertEquals("[1]", l.get(2));
    }

    @Test
    public void testClosure() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        Assert.assertTrue(EvalTools.prepare("{ it.toUpperCase() }") instanceof ClosureExpression);

        model.put("it", "upper");
        assertEquals("UPPER", EvalTools.prepare("{ it.toUpperCase() }").get(model));


        assertEquals("UP", EvalTools.prepare("{ " +
                "it=it.substring(0,2)\n" +
                "it.toUpperCase()\n" +
                " }").get(model));
        Assert.assertEquals("up", model.get("it"));


        model.clear();
        EvalTools.evaluate("def l = ['a','b','c']", model);
        assertEquals("[A, B, C]", EvalTools.evaluate("l.collect({it.toUpperCase()})", model).toString());


        model.clear();
        model.put("s", "upper");
        assertEquals("UPPER", EvalTools.prepare("{s -> s.toUpperCase() }").get(model));
        assertEquals("UPPER", EvalTools.prepare("{String s -> s.toUpperCase() }").get(model));
        assertEquals("UPPER", EvalTools.prepare("{def s -> s.toUpperCase() }").get(model));
    }

    @Test
    public void testCollections() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        model.clear();
        EvalTools.evaluate("def l = ['a','b','c']", model);
        assertEquals("[A, B, C]", EvalTools.evaluate("l.collect({it.toUpperCase()})", model).toString());

        model.clear();
        EvalTools.evaluate("def l = ['a','b','c']", model);
        assertEquals("a", EvalTools.evaluate("l.find({it=='a' || it=='b'})", model).toString());

        model.clear();
        EvalTools.evaluate("def l = ['a','b','c']", model);
        assertEquals("[a, b]", EvalTools.evaluate("l.findAll({it=='a' || it=='b'})", model).toString());

        model.clear();
        EvalTools.evaluate("def l = ['a','b','c']", model);
        assertEquals("[a, b, c, d]", EvalTools.evaluate("l + 'd'", model).toString());
        assertEquals("[a, b, c, d]", EvalTools.evaluate("l + ['d']", model).toString());
        assertEquals("[a, b, c]", EvalTools.evaluate("l", model).toString());
        assertEquals("[a, b, c, d]", EvalTools.evaluate("l << 'd'", model).toString());
        assertEquals("[a, b, c, d, e]", EvalTools.evaluate("l += 'e'", model).toString());
        assertEquals("[a, b, c, d, e, [f]]", EvalTools.evaluate("l << ['f']", model).toString());
        assertEquals("[a, b, c, d, e, [f], g]", EvalTools.evaluate("l += ['g']", model).toString());
        assertEquals("[a, b, c]", EvalTools.evaluate("['a','b'] + 'c'", model).toString());
        assertEquals("[a, b, c, d, e]", EvalTools.evaluate("['a','b'] + 'c' + ['d','e']", model).toString());


        assertEquals("[2, 4, 6]", EvalTools.evaluate("[1, 2, 3].collect{ it * 2 }", model).toString());
        assertEquals("[2, 4, 6]", EvalTools.evaluate("[1,2,3]*.multiply(2)", model).toString());

        assertEquals("[1, 2, 3]", EvalTools.evaluate("[[id:1],[id:2],[id:3]]*.id", model).toString());

        model.clear();
        EvalTools.evaluate("def l = ['a','b','c']", model);
        EvalTools.evaluate("def r = []", model);
        EvalTools.evaluate("l.each({r << it.toUpperCase()})", model);
        Assert.assertEquals("[A, B, C]", model.get("r").toString());

        model.clear();
        EvalTools.evaluate("def l = ['a','b','c']", model);
        EvalTools.evaluate("def r = []", model);
        EvalTools.evaluate("l.eachWithIndex({it,i -> r << i+'_'+ it.toUpperCase()})", model);
        Assert.assertEquals("[0_A, 1_B, 2_C]", model.get("r").toString());

        model.clear();
        EvalTools.evaluate("def l = [1,2,3]", model);
        Assert.assertTrue((Boolean) EvalTools.evaluate("l.every({it < 5})", model));
        Assert.assertTrue(!(Boolean) EvalTools.evaluate("l.every({it < 3})", model));
        Assert.assertTrue((Boolean) EvalTools.evaluate("l.any({it > 2})", model));
        Assert.assertTrue(!(Boolean) EvalTools.evaluate("l.any({it > 3})", model));

        assertEquals(1, EvalTools.evaluate("l.findIndexOf({it == 2})", model));
        assertEquals(-1, EvalTools.evaluate("l.findIndexOf({it == 4})", model));

        //TODO [1,2,3].sum() == 6

        assertEquals("1-2-3", EvalTools.evaluate("l.join('-')", model).toString());
//        Assert.assertEquals("1-2-3", EvalTools.evaluate("l.inject('counting')", model).toString());

        model.clear();
        EvalTools.evaluate("def l = new java.util.HashSet()", model);
        assertEquals("[1, 2, 3]", EvalTools.evaluate("l << 1 << 2 << 3 << 3", model).toString());
    }

    @Test
    public void testStringTemplates() {
        Map<String, Object> model = new HashMap<String, Object>();

        assertEquals("hello world", EvalTools.evaluate("\"hello world\""));

        model.put("world", "world?");
        assertEquals("hello world?!", EvalTools.evaluate("\"hello $world!\"", model));
        assertEquals("hello world?!", EvalTools.evaluate("\"hello ${world}!\"", model));
        assertEquals("hello world?", EvalTools.evaluate("\"hello ${world}\"", model));
        assertEquals("world?", EvalTools.evaluate("\"${world}\"", model));
        assertEquals("world? hello!", EvalTools.evaluate("\"${world} hello!\"", model));

        model.clear();
        Map m = new HashMap();
        m.put("b", 1);
        model.put("a", m);
        assertEquals("b = 1", EvalTools.evaluate("\"b = $a.b\"", model));
        assertEquals("b = 1", EvalTools.evaluate("\"b = ${a.b}\"", model));
        assertEquals("b + 1 = 2", EvalTools.evaluate("\"b + 1 = ${a.b+1}\"", model));
        assertEquals("a: {b=1}", EvalTools.evaluate("\"a: $a\"", model));

        Expression e = EvalTools.prepareTemplate("a: $a");
        assertEquals("a: {b=1}", e.get(model));
        assertFalse(e.hardcoded);

        e = EvalTools.prepareTemplate("a: hardcode");
        assertEquals("a: hardcode", e.get(model));
        assertTrue(e.hardcoded);
    }

    @Test
    public void testGetLines() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();

        String s;
        List<String> l;

        s = "ololo";
        l = EvalTools.getLines(s);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(s, l.get(0));

        s = "int i = 1; i++;";
        l = EvalTools.getLines(s);
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("int i = 1", l.get(0));
        Assert.assertEquals("i++", l.get(1));

        s = "String s = \"abc;\"";
        l = EvalTools.getLines(s);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(s, l.get(0));

        s = "int i = 1; i++; \n i=\";;\n\n\".length()";
        l = EvalTools.getLines(s);
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("int i = 1", l.get(0));
        Assert.assertEquals("i++", l.get(1));
        Assert.assertEquals("i=\";;\n\n\".length()", l.get(2));

        s = "def a = new A(); a.foo = 'FOO'; a.foo";
        l = EvalTools.getLines(s);
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("def a = new A()", l.get(0));
        Assert.assertEquals("a.foo = 'FOO'", l.get(1));
        Assert.assertEquals("a.foo", l.get(2));
    }


    @Test
    public void testEvaluateBlock() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();

        model.clear();
        model.put("i", 0);
        String s = "i++;" +
                "i*=2;";
        assertEquals(2, EvalTools.evaluate(s, model));

    }

    @Test
    public void testIf() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<EvalTools.Statement> l;
        EvalTools.Statement s;
        String exp;

        l = EvalTools.getStatements("if(true) { System.out.println(\"true\"); }");
        assertEquals(1, l.size());
        s = l.get(0);
        assertEquals("true", s.statement);
        assertEquals(" System.out.println(\"true\"); ", s.body);

        l = EvalTools.getStatements("if(true) System.out.println(\"true\");");
        assertEquals(1, l.size());
        s = l.get(0);
        assertEquals("true", s.statement);
        assertEquals("System.out.println(\"true\")", s.body);

        l = EvalTools.getStatements("if(true)" +
                "System.out.println(\"true\");" +
                "System.out.println(\"other\");");
        assertEquals(2, l.size());
        s = l.get(0);
        assertEquals("true", s.statement);
        assertEquals("System.out.println(\"true\")", s.body);
        assertEquals("System.out.println(\"other\");", l.get(1).statement);

        l = EvalTools.getStatements("if(true) if(!false)" +
                "System.out.println(\"true\");" +
                "System.out.println(\"other\");");
        assertEquals(2, l.size());
        s = l.get(0);
        assertEquals("true", s.statement);
        assertEquals("!false", s.bodyStatement.statement);
        assertEquals("System.out.println(\"true\")", s.bodyStatement.body);
        assertEquals("System.out.println(\"other\");", l.get(1).statement);


        exp = "if(\ni\n>\n0\n)\n" +
                " i=i*2;" +
                "i";

        model.put("i", 1);
        assertEquals(2, EvalTools.evaluate(exp, model));
        model.put("i", 2);
        assertEquals(4, EvalTools.evaluate(exp, model));
        model.put("i", -1);
        assertEquals(-1, EvalTools.evaluate(exp, model));


        exp = "if(i>0){" +
                " i=i*2;" +
                "i=i+3}" +
                "i";

        model.put("i", 1);
        assertEquals(5, EvalTools.evaluate(exp, model));
        model.put("i", 2);
        assertEquals(7, EvalTools.evaluate(exp, model));
        model.put("i", -1);
        assertEquals(-1, EvalTools.evaluate(exp, model));


        exp = "if(i>0)" +
                " i=i*2;" +
                "else " +
                " i*=-1;" +
                "i";

        model.put("i", 2);
        assertEquals(4, EvalTools.evaluate(exp, model));
        model.put("i", -2);
        assertEquals(2, EvalTools.evaluate(exp, model));


        exp = "if(i>0){" +
                " i=i*2;" +
                "i++;}" +
                "else " +
                " i*=-1;" +
                "i";

        model.put("i", 2);
        assertEquals(5, EvalTools.evaluate(exp, model));
        model.put("i", -2);
        assertEquals(2, EvalTools.evaluate(exp, model));


        exp = "if(i>0)" +
                " i=i*2;" +
                "else {" +
                " i*=-1;" +
                " i++;" +
                "}" +
                "i";

        model.put("i", 2);
        assertEquals(4, EvalTools.evaluate(exp, model));
        model.put("i", -2);
        assertEquals(3, EvalTools.evaluate(exp, model));


        exp = "if(i==0)" +
                " i=1;" +
                "else if(i==1)" +
                " i=2;" +
                "else" +
                " i=3;" +
                "i";

        model.put("i", 0);
        assertEquals(1, EvalTools.evaluate(exp, model));
        model.put("i", 1);
        assertEquals(2, EvalTools.evaluate(exp, model));
        model.put("i", -1);
        assertEquals(3, EvalTools.evaluate(exp, model));


        exp = "if(i>0) if(i<5)" +
                " i=2;" +
                "else if(i<10)" +
                " i=3;" +
                "else" +
                " i=4;" +
                "i";

        model.put("i", 1);
        assertEquals(2, EvalTools.evaluate(exp, model));
        model.put("i", 7);
        assertEquals(3, EvalTools.evaluate(exp, model));
        model.put("i", 15);
        assertEquals(4, EvalTools.evaluate(exp, model));
        model.put("i", 0);
        assertEquals(0, EvalTools.evaluate(exp, model));
    }

    @Test
    public void testTruth() {
        // http://groovy.codehaus.org/Groovy+Truth

//        def a = true
//        def b = true
//        def c = false
//        assert a
//        assert a && b
//        assert a || c
//        assert !c

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("a", true);
        model.put("b", true);
        model.put("c", false);
        assertEquals(true, EvalTools.evaluate("a", model));
        assertEquals(true, EvalTools.evaluate("a && b", model));
        assertEquals(true, EvalTools.evaluate("a || c", model));
        assertEquals(true, EvalTools.evaluate("!c", model));


//        def numbers = [1,2,3]
//        assert numbers //true, as numbers in not empty
//        numbers = []
//        assert !numbers //true, as numbers is now an empty collection

        EvalTools.evaluate("def numbers = [1,2,3]", model);
        assertEquals(true, EvalTools.evaluate("true && numbers", model));
        EvalTools.evaluate("numbers = []", model);
        assertEquals(true, EvalTools.evaluate("!numbers", model));


//        assert ![].iterator() // false because the Iterator is empty
//        assert [0].iterator() // true because the Iterator has a next element
//        def v = new Vector()
//        assert !v.elements()  // false because the Enumeration is empty
//        v.add(new Object())
//        assert v.elements()   // true because the Enumeration has more elements

        assertEquals(true, EvalTools.evaluate("![].iterator()", model));
        assertEquals(true, EvalTools.evaluate("true || [0].iterator()", model));
        EvalTools.evaluate("def v = new Vector()", model);
        assertEquals(true, EvalTools.evaluate("!v.elements()", model));
        EvalTools.evaluate("new Object()", model);
        EvalTools.evaluate("v.add(new Object())", model);
        assertEquals(true, EvalTools.evaluate("true && v.elements()", model));


//        assert ['one':1]
//        assert ![:]

        assertEquals(true, EvalTools.evaluate("true && ['one':1]", model));
        assertEquals(true, EvalTools.evaluate("![:]", model));


//        assert 'This is true'
//        assert !''
//        //GStrings
//        def s = ''
//        assert !("$s")
//        s = 'x'
//        assert ("$s")

        assertEquals(true, EvalTools.evaluate("true && 'This is true'", model));
        assertEquals(true, EvalTools.evaluate("!''", model));
        EvalTools.evaluate("def s = ''", model);
        assertEquals(true, EvalTools.evaluate("!\"$s\"", model));
        EvalTools.evaluate("s = 'x'", model);
        assertEquals(true, EvalTools.evaluate("true && \"$s\"", model));


//        assert !0 //yeah, 0s are false, like in Perl
//        assert 1  //this is also true for all other number types

        assertEquals(true, EvalTools.evaluate("!0", model));
        assertEquals(true, EvalTools.evaluate("true && 1", model));


//        assert new Object()
//        assert !null

        assertEquals(true, EvalTools.evaluate("true && new Object()", model));
        assertEquals(true, EvalTools.evaluate("!null", model));
    }

    public static class Foo {

        private String foo = "foo";
        String foofoo = "before get";
        public String bar = "bar";
        public static String foobar = "foobar";

        @Override
        public String toString() {
            return foo;
        }

        public String getFoo() {
            return foo;
        }

        public String getFoofoo() {
            foofoo = "foofoo";
            return foofoo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }
    }

    enum Num {
        ONE, TWO
    }

    @Test
    public void test_imports_1() {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();
        List<String> imports = new ArrayList<String>();
        Expression exp;

        final String s = "def counter = new AtomicInteger()";
        checkException(new Runnable() {
            @Override
            public void run() {
                EvalTools.prepare(s);
            }
        }, ClassNotFoundException.class, "Can not find class 'AtomicInteger'");


        imports.add("java.util.concurrent.atomic.AtomicInteger");
        exp = EvalTools.prepare(s, model, functions, imports, false);
        Assert.assertEquals(AtomicInteger.class, exp.get(model).getClass());

        imports.clear();
        imports.add("java.util.concurrent.atomic.*");
        exp = EvalTools.prepare(s, model, functions, imports, false);
        Assert.assertEquals(AtomicInteger.class, exp.get(model).getClass());

        imports.clear();
        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("new EvalToolsTest.Foo()", model, functions, imports, false);
        Assert.assertEquals(Foo.class, exp.get(model).getClass());
    }

    @Test
    public void test_imports_2() {
        String s = "import " + AtomicInteger.class.getCanonicalName() + ";\n" +
                "i = new AtomicInteger(1)";
        Expression expression = EvalTools.prepare(s);

        Map model = new HashMap();
        expression.get(model);

        Assert.assertEquals(1, model.size());
        Assert.assertEquals(1, ((AtomicInteger) model.get("i")).get());
    }

    @Test
    public void testGetter() {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();
        List<String> imports = new ArrayList<String>();
        Expression exp;

        exp = EvalTools.prepare("def entry=[foo:'bar'].entrySet().iterator().next(); entry.value", model, functions, imports, false);
        Assert.assertEquals("bar", exp.get(model));

        exp = EvalTools.prepare("com.wizzardo.tools.evaluation.EvalToolsTest.Num.ONE", model, functions, imports, false);
        Assert.assertEquals(Num.ONE, exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        model.put("a", Num.TWO);
        exp = EvalTools.prepare("a == EvalToolsTest.Num.TWO", model, functions, imports, false);
        Assert.assertEquals(true, exp.get(model));
        model.clear();

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("new EvalToolsTest.Foo().bar", model, functions, imports, false);
        Assert.assertEquals("bar", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("new EvalToolsTest.Foo().getFoo()", model, functions, imports, false);
        Assert.assertEquals("foo", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("new EvalToolsTest.Foo().foo", model, functions, imports, false);
        Assert.assertEquals("foo", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("new EvalToolsTest.Foo().foofoo", model, functions, imports, false);
        Assert.assertEquals("foofoo", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("new EvalToolsTest.Foo().foobar", model, functions, imports, false);
        Assert.assertEquals("foobar", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("EvalToolsTest.Foo.foobar", model, functions, imports, false);
        Assert.assertEquals("foobar", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("EvalToolsTest.getSimpleName()", model, functions, imports, false);
        Assert.assertEquals("EvalToolsTest", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("EvalToolsTest.simpleName", model, functions, imports, false);
        Assert.assertEquals("EvalToolsTest", exp.get(model));
    }

    @Test
    public void testSetter() {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();
        List<String> imports = new ArrayList<String>();
        Expression exp;

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("new EvalToolsTest.Foo().bar = 'BAR'", model, functions, imports, false);
        Assert.assertEquals("BAR", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("def foo = new EvalToolsTest.Foo(); foo.setFoo('FOO'); foo.foo", model, functions, imports, false);
        Assert.assertEquals("FOO", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("def foo = new EvalToolsTest.Foo(); foo.foo = 'FOO'", model, functions, imports, false);
        Assert.assertEquals("FOO", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("def foo = new EvalToolsTest.Foo(); foo.foobar = 'FOOBAR'", model, functions, imports, false);
        Assert.assertEquals("FOOBAR", exp.get(model));

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        exp = EvalTools.prepare("EvalToolsTest.Foo.foobar = 'FOOBAR'", model, functions, imports, false);
        Assert.assertEquals("FOOBAR", exp.get(model));
        Assert.assertEquals("FOOBAR", Foo.foobar);

        Foo.foobar = "foobar";
    }

    private void checkException(Runnable runnable, Class<? extends Exception> exceptionClass, String message) {
        boolean b = false;
        try {
            runnable.run();
            b = true;
        } catch (Exception e) {
            Assert.assertEquals(exceptionClass, e.getClass());
            Assert.assertEquals(message, e.getMessage());
        }
        Assert.assertFalse(b);
    }

    @Test
    public void testMapParam() {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();
        List<String> imports = new ArrayList<String>();
        Expression exp;

        functions.put("foo", new UserFunction("foo", "args['x']", "args"));

        exp = EvalTools.prepare("foo([x:'bar'])", model, functions, imports, false);
        Assert.assertEquals("bar", exp.get(model));

        exp = EvalTools.prepare("foo(x:'bar')", model, functions, imports, false);
        Assert.assertEquals("bar", exp.get(model));

        exp = EvalTools.prepare("foo(x:'bar', y:'foo')", model, functions, imports, false);
        Assert.assertEquals("bar", exp.get(model));


        functions.put("foobar", new UserFunction("foobar", "foo.x + separator + bar.y", "foo", "separator", "bar"));

        exp = EvalTools.prepare("foobar([x:'bar'], ' - ', [y:'foo'])", model, functions, imports, false);
        Assert.assertEquals("bar - foo", exp.get(model));

        exp = EvalTools.prepare("foobar(x:'bar', ' - ', y:'foo')", model, functions, imports, false);
        Assert.assertEquals("bar - foo", exp.get(model));

        exp = EvalTools.prepare("foobar(x:'bar', y:'BAR', ' - ', y:'foo', x:'FOO')", model, functions, imports, false);
        Assert.assertEquals("bar - foo", exp.get(model));
    }

    @Test
    public void testCast() {
        final Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();
        List<String> imports = new ArrayList<String>();
        Expression exp;

        assertEquals(Integer.class, EvalTools.evaluate("(Integer) 1", model).getClass());

        checkException(new Runnable() {
            @Override
            public void run() {
                EvalTools.evaluate("(String) 1", model);
            }
        }, ClassCastException.class, "java.lang.Integer cannot be cast to java.lang.String");


        model.put("foo", new EvalToolsTest.Foo());
        exp = EvalTools.prepare("(com.wizzardo.tools.evaluation.EvalToolsTest.Foo) foo", model, functions, imports, false);
        Assert.assertEquals(Foo.class, exp.get(model).getClass());

        imports.add("com.wizzardo.tools.evaluation.EvalToolsTest");
        model.put("foo", new EvalToolsTest.Foo());
        exp = EvalTools.prepare("(EvalToolsTest.Foo) foo", model, functions, imports, false);
        Assert.assertEquals(Foo.class, exp.get(model).getClass());

        assertEquals(HashMap.class, EvalTools.evaluate("(Map<Integer, String>) [:]", model).getClass());
        assertEquals(ArrayList.class, EvalTools.evaluate("(List<Integer>) []", model).getClass());
    }

    @Test
    public void testComment() {
        Map<String, Object> model = new HashMap<String, Object>();
        String s = "a = 1\n//a=2";
        Assert.assertEquals(1, EvalTools.prepare(s).get(model));
    }
}
