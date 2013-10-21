/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wizzardo.tools.evaluation;

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


        assertEquals(1, EvalTools.evaluate("java.lang.Math.abs(-1)", null));

        assertEquals(1, EvalTools.evaluate("(1)", new HashMap<String, Object>()));
        assertEquals(1, EvalTools.evaluate("((1))", new HashMap<String, Object>()));
        assertEquals(2, EvalTools.evaluate("1+1", new HashMap<String, Object>()));
        assertEquals(2.0, EvalTools.evaluate("1+1.0", new HashMap<String, Object>()));
        assertEquals(5, EvalTools.evaluate("1+1+3", new HashMap<String, Object>()));
        assertEquals("olo123", EvalTools.evaluate("\"olo\"+1+(1+1)+3", new HashMap<String, Object>()));
        assertEquals("olo123", EvalTools.evaluate("'olo'+1+(1+1)+3", new HashMap<String, Object>()));
        assertEquals("OLO123", EvalTools.evaluate("(\"olo\"+1+(1+1)+3).toUpperCase()", new HashMap<String, Object>()));
        assertEquals("ololo", EvalTools.evaluate("\"olo\".concat(\"lo\")", new HashMap<String, Object>()));

        model.put("ololo", "qwerty");
        model.put("qwe", "  ololo  ");
        model.put("length111", "111");
        assertEquals("QWERTYOLOLO123", EvalTools.evaluate("ololo.concat(qwe.trim().substring(2)).concat(qwe.trim().substring(length111.length()) + 123).toUpperCase()", model));

        assertEquals(5f, EvalTools.evaluate("10f/2", new HashMap<String, Object>()));
        assertEquals(7, EvalTools.evaluate("1+2*3", new HashMap<String, Object>()));
        assertEquals(9, EvalTools.evaluate("1+2*(1+3)", new HashMap<String, Object>()));


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


        assertEquals(true, EvalTools.evaluate("true", new HashMap<String, Object>()));
        assertEquals(false, EvalTools.evaluate("false", new HashMap<String, Object>()));
        assertEquals(true, EvalTools.evaluate("!false", new HashMap<String, Object>()));


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


        assertEquals(true, EvalTools.evaluate("1>0", new HashMap<String, Object>()));
        assertEquals(true, EvalTools.evaluate("1>=1", new HashMap<String, Object>()));
        assertEquals(true, EvalTools.evaluate("5>\"123\".length()", new HashMap<String, Object>()));
        assertEquals(false, EvalTools.evaluate("1>\"123\".length()", new HashMap<String, Object>()));
        assertEquals(false, EvalTools.evaluate("1<0", new HashMap<String, Object>()));
        assertEquals(true, EvalTools.evaluate("2<=1*2", new HashMap<String, Object>()));
        assertEquals(true, EvalTools.evaluate("2>=1*2", new HashMap<String, Object>()));
        assertEquals(true, EvalTools.evaluate("2== 1 +(3-2)*2-1", new HashMap<String, Object>()));


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
        assertEquals(1, EvalTools.evaluate("java.lang.Math.abs(-1)", null));
        assertEquals(1, EvalTools.evaluate("Math.abs(-1)", null));
        assertEquals(2, EvalTools.evaluate("Math.abs(-1)+Math.abs(-1)", null));
        assertEquals(2d, EvalTools.evaluate("Math.sqrt(2+2)", null));
        assertEquals(4d, EvalTools.evaluate("Math.pow(2,(2*2)/(2))", null));
        assertEquals(Math.PI, EvalTools.evaluate("Math.PI"));

        System.out.println("test constructors");
        assertEquals("ololo", EvalTools.evaluate("new String(\"ololo\")", null));

        System.out.println("test fields");
        assertEquals(1, EvalTools.evaluate("new java.awt.Point(1,2).x", null));
        assertEquals(3, EvalTools.evaluate("new java.awt.Point(1,2).x + new java.awt.Point(1,2).y", null));


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
        functions.put("y", new UserFunction("y", "2*2", null));
        assertEquals(4, EvalTools.evaluate("y()", null, functions));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        model.put("g", 0);
        functions = new HashMap<String, UserFunction>();
        functions.put("it", new UserFunction("it", "i<end&&(g=++x)==g?it(i+1,end):g", "i", "end"));
        assertEquals(10, EvalTools.evaluate("it(0,10)", model, functions));
        assertEquals(10, model.get("x"));

        assertEquals(true, EvalTools.evaluate("(Math.sin((3.1416/2))) > 0", new HashMap<String, Object>()));

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
        assertEquals("[1, 2, 3, 4, 5]", EvalTools.evaluate("x += 5", model).toString());

        assertEquals(1, ((List) EvalTools.evaluate("[1,[2,3]]", null)).get(0));
        assertEquals("[2, 3]", ((List) EvalTools.evaluate("[1,[2,3]]", null)).get(1).toString());

        assertEquals("qwerty", ((Map) EvalTools.evaluate("[qwe:\"qwerty\"]", null)).get("qwe").toString());
        assertEquals("qwe\",rty", ((Map) EvalTools.evaluate("[qwe:\"qwe\\\",rty\"]", null)).get("qwe").toString());
        assertEquals(1, ((Map) ((Map) EvalTools.evaluate("[qwe:\"qwe\\\",rty\",olo:[asd:1]]", null)).get("olo")).get("asd"));
        assertEquals(1, EvalTools.evaluate("[qwe:\"qwe\\\",rty\",olo:[asd:1]].olo.asd", null));

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


        assertEquals(5, EvalTools.evaluate("(0..5).size()", null));
        assertEquals(5, EvalTools.evaluate("(1+2-3 .. 5).size()", null));

        model = new HashMap<String, Object>();
        EvalTools.evaluate("def x=0..10", model);

        assertEquals(5, EvalTools.evaluate("x.get(5)", model));
        boolean exception = false;
        try {
            EvalTools.evaluate("x.put(5)", model);
        } catch (RuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            EvalTools.evaluate("x.b", model);
        } catch (RuntimeException e) {
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
    }

    @Test
    public void testClone() throws Exception {
        String exp = "1+\"ololo\".substring(2)";
        Expression eh = EvalTools.prepare(exp, null);
        Object ob1 = eh.get(null);
        Object ob2 = eh.get(null);
        assertTrue(ob1 == ob2);
        eh = eh.clone();
        Object ob3 = eh.get(null);
        assertTrue(ob1 != ob3);
        assertTrue(ob1.equals(ob3));
    }

    @Test
    public void testTrimBrackets() throws Exception {
        assertEquals("sin((1+2)/(3))", EvalTools.trimBrackets("sin((1+2)/(3))"));
        assertEquals("sin((1+2)/3)", EvalTools.trimBrackets("(sin((1+2)/3))"));
        assertEquals("sin((1+2)/3)", EvalTools.trimBrackets("((sin((1+2)/3)))"));
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
        assertEquals("[a, b, c, d]", EvalTools.evaluate("l << 'd'", model).toString());
        assertEquals("[a, b, c, d, e]", EvalTools.evaluate("l += 'e'", model).toString());
        assertEquals("[a, b, c, d, e, [f]]", EvalTools.evaluate("l << ['f']", model).toString());
        assertEquals("[a, b, c, d, e, [f], g]", EvalTools.evaluate("l += ['g']", model).toString());


//        Assert.assertEquals("[a, b, c, d, e]", EvalTools.evaluate("['a','b'] + 'c' + ['d','e']", model).toString());

        //TODO [1,2,3]*.multiply(2) == [2,4,6]

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
    }

    @Test
    public void testGetLines() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();

        String s = "ololo";
        List<String> l = EvalTools.getLines(s);
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
}
