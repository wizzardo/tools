/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.evaluation;

import org.junit.Assert;
import org.junit.Test;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Moxa
 */
public class EvalUtilsTest {

    public EvalUtilsTest() {
    }

    /**
     * Test of evaluate method, of class EvalUtils.
     */
    @Test
    public void testEvaluateLine() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();


        assertEquals(1, EvalUtils.evaluate("java.lang.Math.abs(-1)", null));

        assertEquals(1, EvalUtils.evaluate("(1)", new HashMap<String, Object>()));
        assertEquals(1, EvalUtils.evaluate("((1))", new HashMap<String, Object>()));
        assertEquals(2, EvalUtils.evaluate("1+1", new HashMap<String, Object>()));
        assertEquals(2.0, EvalUtils.evaluate("1+1.0", new HashMap<String, Object>()));
        assertEquals(5, EvalUtils.evaluate("1+1+3", new HashMap<String, Object>()));
        assertEquals("olo123", EvalUtils.evaluate("\"olo\"+1+(1+1)+3", new HashMap<String, Object>()));
        assertEquals("olo123", EvalUtils.evaluate("'olo'+1+(1+1)+3", new HashMap<String, Object>()));
        assertEquals("OLO123", EvalUtils.evaluate("(\"olo\"+1+(1+1)+3).toUpperCase()", new HashMap<String, Object>()));
        assertEquals("ololo", EvalUtils.evaluate("\"olo\".concat(\"lo\")", new HashMap<String, Object>()));

        model.put("ololo", "qwerty");
        model.put("qwe", "  ololo  ");
        model.put("length111", "111");
        assertEquals("QWERTYOLOLO123", EvalUtils.evaluate("ololo.concat(qwe.trim().substring(2)).concat(qwe.trim().substring(length111.length()) + 123).toUpperCase()", model));

        assertEquals(5f, EvalUtils.evaluate("10f/2", new HashMap<String, Object>()));
        assertEquals(7, EvalUtils.evaluate("1+2*3", new HashMap<String, Object>()));
        assertEquals(9, EvalUtils.evaluate("1+2*(1+3)", new HashMap<String, Object>()));


        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalUtils.evaluate("i++", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(2, EvalUtils.evaluate("i++ + 1", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(3, EvalUtils.evaluate("++i + 1", model));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(4, EvalUtils.evaluate("++i + i++", model));
        assertEquals(3, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(5, EvalUtils.evaluate("++i + ++i", model));
        assertEquals(3, model.get("i"));


        assertEquals(true, EvalUtils.evaluate("true", new HashMap<String, Object>()));
        assertEquals(false, EvalUtils.evaluate("false", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("!false", new HashMap<String, Object>()));


        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalUtils.evaluate("i--", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(0, EvalUtils.evaluate("i-- - 1", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(-1, EvalUtils.evaluate("--i - 1", model));
        assertEquals(0, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(0, EvalUtils.evaluate("--i - i--", model));
        assertEquals(-1, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 1);
        assertEquals(1, EvalUtils.evaluate("--i - --i", model));
        assertEquals(-1, model.get("i"));


        assertEquals(true, EvalUtils.evaluate("1>0", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("1>=1", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("5>\"123\".length()", new HashMap<String, Object>()));
        assertEquals(false, EvalUtils.evaluate("1>\"123\".length()", new HashMap<String, Object>()));
        assertEquals(false, EvalUtils.evaluate("1<0", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("2<=1*2", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("2>=1*2", new HashMap<String, Object>()));
        assertEquals(true, EvalUtils.evaluate("2== 1 +(3-2)*2-1", new HashMap<String, Object>()));


        model = new HashMap<String, Object>();
        model.put("a", "ololo");
        assertEquals(6, EvalUtils.evaluate("a!=null?a.length()+1:1+\"ololo\"", model));
        assertEquals("1ololo", EvalUtils.evaluate("a==null?a.length()+1:1+\"ololo\"", model));
        assertEquals(15, EvalUtils.evaluate("4+(a!=null?a.length()+1:1+\"ololo\")+5", model));


        model = new HashMap<String, Object>();
        model.put("i", 0);
        assertEquals(5, EvalUtils.evaluate("i+=5", model));
        assertEquals(10, EvalUtils.evaluate("i*=2", model));
        assertEquals(2, EvalUtils.evaluate("i/=5", model));
        assertEquals(0, EvalUtils.evaluate("i-=2", model));
        assertEquals(3, EvalUtils.evaluate("i+=1+2", model));


        model = new HashMap<String, Object>();
        model.put("i", 0);
        assertEquals(3, EvalUtils.evaluate("i=1+2", model));
        assertEquals(3, model.get("i"));


        System.out.println("test logic!");
        model = new HashMap<String, Object>();
        model.put("i", 0);
        int i = 0;
        boolean b = i++ > 1 || i++ > 1 || i++ > 1 || i++ > 1;
        assertEquals(b, EvalUtils.evaluate("i++>1||i++>1||i++>1||i++>1", model));
        assertEquals(i, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ > 1 || i++ > 1 || i++ > 1 | i++ > 1;
        assertEquals(b, EvalUtils.evaluate("i++>1||i++>1||i++>1|i++>1", model));
        assertEquals(i, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        assertEquals(true, EvalUtils.evaluate("x<++x", model));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        model.put("i", 1);
        model.put("n", 1);
        assertEquals(true, EvalUtils.evaluate("i<n&&x++<x?false:true", model));
        assertEquals(0, model.get("x"));

        assertEquals("c", EvalUtils.evaluate("false ? \"a\" : false ? \"b\" : \"c\"", model));
        assertEquals("a", EvalUtils.evaluate("true ? \"a\" : false ? \"b\" : \"c\"", model));
        assertEquals("b", EvalUtils.evaluate("false ? \"a\" : true ? \"b\" : \"c\"", model));
        assertEquals("a", EvalUtils.evaluate("true ? \"a\" : true ? \"b\" : \"c\"", model));
        assertEquals("b", EvalUtils.evaluate("true ? true ? \"b\" : \"c\" : \"a\"", model));
        assertEquals("c", EvalUtils.evaluate("true ? false ? \"b\" : \"c\" : \"a\"", model));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ < 1 && i++ < 1 && i++ < 1 && i++ < 1;
        assertEquals(b, EvalUtils.evaluate("i++ < 1 && i++ < 1 && i++ < 1 && i++ < 1", model));
        assertEquals(i, model.get("i"));
        assertEquals(2, model.get("i"));

        model = new HashMap<String, Object>();
        model.put("i", 0);
        i = 0;
        b = i++ < 1 & i++ < 1 & i++ < 1 & i++ < 1;
        assertEquals(b, EvalUtils.evaluate("i++ < 1 & i++ < 1 & i++ < 1 & i++ < 1", model));
        assertEquals(i, model.get("i"));
        assertEquals(4, model.get("i"));


        assertEquals(true, EvalUtils.evaluate("true&&(false|!true) | 3>2 ", model));

        System.out.println("test static methods");
        assertEquals(1, EvalUtils.evaluate("java.lang.Math.abs(-1)", null));
        assertEquals(1, EvalUtils.evaluate("Math.abs(-1)", null));
        assertEquals(2, EvalUtils.evaluate("Math.abs(-1)+Math.abs(-1)", null));
        assertEquals(2d, EvalUtils.evaluate("Math.sqrt(2+2)", null));
        assertEquals(4d, EvalUtils.evaluate("Math.pow(2,(2*2)/(2))", null));

        System.out.println("test constructors");
        assertEquals("ololo", EvalUtils.evaluate("new String(\"ololo\")", null));

        System.out.println("test fields");
        assertEquals(1, EvalUtils.evaluate("new java.awt.Point(1,2).x", null));
        assertEquals(3, EvalUtils.evaluate("new java.awt.Point(1,2).x + new java.awt.Point(1,2).y", null));


        System.out.println("test user functions");
        UserFunction y = new UserFunction("y", "x*2", "x");
        functions = new HashMap<String, UserFunction>();
        functions.put(y.getName(), y);
        assertEquals(10, EvalUtils.evaluate("y(5)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        assertEquals(8, EvalUtils.evaluate("y(2*(10/5))", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(15, EvalUtils.evaluate("z(5)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(11, EvalUtils.evaluate("z(5) - y(2)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "x*2", "x"));
        functions.put("z", new UserFunction("z", "y(x)+x", "x"));
        assertEquals(9, EvalUtils.evaluate("z(5) - z(2)", null, functions));

        functions = new HashMap<String, UserFunction>();
        functions.put("y", new UserFunction("y", "2*2", null));
        assertEquals(4, EvalUtils.evaluate("y()", null, functions));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        model.put("g", 0);
        functions = new HashMap<String, UserFunction>();
        functions.put("it", new UserFunction("it", "i<end&&(g=++x)==g?it(i+1,end):g", "i", "end"));
        assertEquals(10, EvalUtils.evaluate("it(0,10)", model, functions));
        assertEquals(10, model.get("x"));

        assertEquals(true, EvalUtils.evaluate("(Math.sin((3.1416/2))) > 0", new HashMap<String, Object>()));

        model = new HashMap<String, Object>();
        model.put("x", 0);
        assertEquals(0, EvalUtils.evaluate("x = x++", model));


        System.out.println("test variables definition");
        model = new HashMap<String, Object>();
        assertEquals(1, EvalUtils.evaluate("def x = 1", model));
        assertEquals(1, model.get("x"));


        System.out.println("test collections");
        model = new HashMap<String, Object>();
        assertTrue(EvalUtils.evaluate("def x = []", model) instanceof List);
        assertEquals("[1]", EvalUtils.evaluate("x << 1", model).toString());
        assertEquals(1, ((List) model.get("x")).size());
        assertEquals(1, ((List) model.get("x")).get(0));
        assertEquals("[1, 2, 3]", EvalUtils.evaluate("x << 2 << 3", model).toString());
        assertEquals("[1, 2, 3, 4]", EvalUtils.evaluate("x + 4", model).toString());
        assertEquals("[1, 2, 3, 4, 5]", EvalUtils.evaluate("x += 5", model).toString());

        assertEquals(1, ((List) EvalUtils.evaluate("[1,[2,3]]", null)).get(0));
        assertEquals("[2, 3]", ((List) EvalUtils.evaluate("[1,[2,3]]", null)).get(1).toString());

        assertEquals("qwerty", ((Map) EvalUtils.evaluate("[qwe:\"qwerty\"]", null)).get("qwe").toString());
        assertEquals("qwe\",rty", ((Map) EvalUtils.evaluate("[qwe:\"qwe\\\",rty\"]", null)).get("qwe").toString());
        assertEquals(1, ((Map) ((Map) EvalUtils.evaluate("[qwe:\"qwe\\\",rty\",olo:[asd:1]]", null)).get("olo")).get("asd"));
        assertEquals(1, EvalUtils.evaluate("[qwe:\"qwe\\\",rty\",olo:[asd:1]].olo.asd", null));

        model = new HashMap<String, Object>();
        model.put("x", 1);
        assertEquals("[1]", EvalUtils.evaluate("[x]", model).toString());

        assertTrue(EvalUtils.evaluate("[:]", model) instanceof Map);

        model = new HashMap<String, Object>();
        assertTrue(EvalUtils.evaluate("def x = [:]", model) instanceof Map);
        assertEquals("value", EvalUtils.evaluate("x.key = \"value\"", model));
        assertEquals("value", ((Map) model.get("x")).get("key"));
        assertEquals(1, EvalUtils.evaluate("x.key = 1", model));
        assertEquals(1, EvalUtils.evaluate("x.key++", model));
        assertEquals(2, ((Map) model.get("x")).get("key"));
        assertEquals(4, EvalUtils.evaluate("x.key+=2", model));
        assertEquals(12, EvalUtils.evaluate("x.key*=3", model));
        assertEquals(3, EvalUtils.evaluate("x.key/=4", model));
        assertEquals(0, EvalUtils.evaluate("x.key-=3", model));
        assertEquals(1, EvalUtils.evaluate("++x.key", model));
        assertEquals(0, EvalUtils.evaluate("--x.key", model));
        assertEquals(0, EvalUtils.evaluate("x.key--", model));
        assertEquals(-1, ((Map) model.get("x")).get("key"));

        model = new HashMap<String, Object>();
        Point p = new Point(0, 0);
        model.put("p", p);
        assertEquals(0, EvalUtils.evaluate("p.x", model));
        assertEquals(0, EvalUtils.evaluate("p.x++", model));
        assertEquals(2, EvalUtils.evaluate("++p.x", model));
        assertEquals(2, p.x);


        model = new HashMap<String, Object>();
        assertTrue(EvalUtils.evaluate("def x = [:]", model) instanceof Map);
        assertEquals(1, EvalUtils.evaluate("x[\"key\"] = 1", model));
        assertEquals(1, EvalUtils.evaluate("x[\"key\"]", model));
        assertEquals(1, ((Map) model.get("x")).get("key"));
        assertEquals(3, EvalUtils.evaluate("x[\"key\"] +=2", model));

        model = new HashMap<String, Object>();
        model.put("arr", new String[1]);
        assertEquals("ololo", EvalUtils.evaluate("arr[0] = \"ololo\"", model));

        model = new HashMap<String, Object>();
        assertTrue(EvalUtils.evaluate("def l = []", model) instanceof List);
        assertEquals(1, EvalUtils.evaluate("l[0] = 1", model));
        assertEquals(1, EvalUtils.evaluate("l[0]", model));
        assertEquals(2, EvalUtils.evaluate("l[2]=2", model));
        assertEquals("[1, null, 2]", EvalUtils.evaluate("l", model).toString());

        model = new HashMap<String, Object>();
        assertTrue(EvalUtils.evaluate("def m = [:]", model) instanceof Map);
        assertTrue(EvalUtils.evaluate("m.a = [:]", model) instanceof Map);
        assertEquals(1, EvalUtils.evaluate("m[\"a\"][\"b\"] = 1", model));
        assertEquals(1, EvalUtils.evaluate("m.a.b = 1", model));


        assertEquals(5, EvalUtils.evaluate("(0..5).size()", null));
        assertEquals(5, EvalUtils.evaluate("(1+2-3 .. 5).size()", null));

        model = new HashMap<String, Object>();
        EvalUtils.evaluate("def x=0..10", model);

        assertEquals(5, EvalUtils.evaluate("x.get(5)", model));
        boolean exception = false;
        try {
            EvalUtils.evaluate("x.put(5)", model);
        } catch (RuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            EvalUtils.evaluate("x.b", model);
        } catch (RuntimeException e) {
            exception = true;
        }
        assertTrue(exception);

        model.clear();
        model.put("it", 1);
        model.put("pageNumber", 0);
        assertEquals("", EvalUtils.evaluate(" it == pageNumber ? 'style=\"font-size: 20px;\"' : ''", model));
        model.put("it", 0);
        assertEquals("style=\"font-size: 20px;\"", EvalUtils.evaluate(" it == pageNumber ? 'style=\"font-size: 20px;\"' : ''", model));

        assertNotNull(EvalUtils.evaluate("Math.sin((1+2)/(3))"));
    }

    @Test
    public void testClone() throws Exception {
        String exp = "1+\"ololo\".substring(2)";
        Expression eh = EvalUtils.prepare(exp, null);
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
        Assert.assertEquals("sin((1+2)/(3))", EvalUtils.trimBrackets("sin((1+2)/(3))"));
        Assert.assertEquals("sin((1+2)/3)", EvalUtils.trimBrackets("(sin((1+2)/3))"));
        Assert.assertEquals("sin((1+2)/3)", EvalUtils.trimBrackets("((sin((1+2)/3)))"));
    }

    @Test
    public void testGetParts() throws Exception {
        List<String> l = EvalUtils.getParts("sin(1)+2");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("sin", l.get(0));
        Assert.assertEquals("(1)", l.get(1));
        Assert.assertEquals("+2", l.get(2));

        l = EvalUtils.getParts("sin(1)");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("sin", l.get(0));
        Assert.assertEquals("(1)", l.get(1));

        l = EvalUtils.getParts("'(olo)'+1");
        Assert.assertEquals(1, l.size());
        Assert.assertEquals("'(olo)'+1", l.get(0));

        l = EvalUtils.getParts("(1+2)*3");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("(1+2)", l.get(0));
        Assert.assertEquals("*3", l.get(1));

        l = EvalUtils.getParts("'qwe'.concat('rty')");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("'qwe'", l.get(0));
        Assert.assertEquals(".concat", l.get(1));
        Assert.assertEquals("('rty')", l.get(2));

        l = EvalUtils.getParts("('qwe').concat('rty')");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("('qwe')", l.get(0));
        Assert.assertEquals(".concat", l.get(1));
        Assert.assertEquals("('rty')", l.get(2));

        l = EvalUtils.getParts("k[0]");
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));

        l = EvalUtils.getParts("k[0].x");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));
        Assert.assertEquals(".x", l.get(2));

        l = EvalUtils.getParts("k[0][1]");
        Assert.assertEquals(3, l.size());
        Assert.assertEquals("k", l.get(0));
        Assert.assertEquals("[0]", l.get(1));
        Assert.assertEquals("[1]", l.get(2));
    }

    @Test
    public void testClosure() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        Assert.assertTrue(EvalUtils.prepare("{ it.toUpperCase() }") instanceof ClosureExpression);

        model.put("it", "upper");
        Assert.assertEquals("UPPER", EvalUtils.prepare("{ it.toUpperCase() }").get(model));


        Assert.assertEquals("UP", EvalUtils.prepare("{ " +
                "it=it.substring(0,2)\n" +
                "it.toUpperCase()\n" +
                " }").get(model));
        Assert.assertEquals("up", model.get("it"));


        model.clear();
        EvalUtils.evaluate("def l = ['a','b','c']", model);
        Assert.assertEquals("[A, B, C]", EvalUtils.evaluate("l.collect({it.toUpperCase()})", model).toString());


        model.clear();
        model.put("s", "upper");
        Assert.assertEquals("UPPER", EvalUtils.prepare("{s -> s.toUpperCase() }").get(model));
        Assert.assertEquals("UPPER", EvalUtils.prepare("{String s -> s.toUpperCase() }").get(model));
        Assert.assertEquals("UPPER", EvalUtils.prepare("{def s -> s.toUpperCase() }").get(model));
    }

    @Test
    public void testCollections() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();

        model.clear();
        EvalUtils.evaluate("def l = ['a','b','c']", model);
        Assert.assertEquals("[A, B, C]", EvalUtils.evaluate("l.collect({it.toUpperCase()})", model).toString());

        model.clear();
        EvalUtils.evaluate("def l = ['a','b','c']", model);
        Assert.assertEquals("a", EvalUtils.evaluate("l.find({it=='a' || it=='b'})", model).toString());

        model.clear();
        EvalUtils.evaluate("def l = ['a','b','c']", model);
        Assert.assertEquals("[a, b]", EvalUtils.evaluate("l.findAll({it=='a' || it=='b'})", model).toString());

        model.clear();
        EvalUtils.evaluate("def l = ['a','b','c']", model);
        Assert.assertEquals("[a, b, c, d]", EvalUtils.evaluate("l << 'd'", model).toString());
        Assert.assertEquals("[a, b, c, d, e]", EvalUtils.evaluate("l += 'e'", model).toString());
        Assert.assertEquals("[a, b, c, d, e, [f]]", EvalUtils.evaluate("l << ['f']", model).toString());
        Assert.assertEquals("[a, b, c, d, e, [f], g]", EvalUtils.evaluate("l += ['g']", model).toString());


//        Assert.assertEquals("[a, b, c, d, e]", EvalUtils.evaluate("['a','b'] + 'c' + ['d','e']", model).toString());

        //TODO [1,2,3]*.multiply(2) == [2,4,6]

        model.clear();
        EvalUtils.evaluate("def l = ['a','b','c']", model);
        EvalUtils.evaluate("def r = []", model);
        EvalUtils.evaluate("l.each({r << it.toUpperCase()})", model);
        Assert.assertEquals("[A, B, C]", model.get("r").toString());

        model.clear();
        EvalUtils.evaluate("def l = ['a','b','c']", model);
        EvalUtils.evaluate("def r = []", model);
        EvalUtils.evaluate("l.eachWithIndex({it,i -> r << i+'_'+ it.toUpperCase()})", model);
        Assert.assertEquals("[0_A, 1_B, 2_C]", model.get("r").toString());

        model.clear();
        EvalUtils.evaluate("def l = [1,2,3]", model);
        Assert.assertTrue((Boolean) EvalUtils.evaluate("l.every({it < 5})", model));
        Assert.assertTrue(!(Boolean) EvalUtils.evaluate("l.every({it < 3})", model));
        Assert.assertTrue((Boolean) EvalUtils.evaluate("l.any({it > 2})", model));
        Assert.assertTrue(!(Boolean) EvalUtils.evaluate("l.any({it > 3})", model));

        Assert.assertEquals(1, EvalUtils.evaluate("l.findIndexOf({it == 2})", model));
        Assert.assertEquals(-1, EvalUtils.evaluate("l.findIndexOf({it == 4})", model));

        //TODO [1,2,3].sum() == 6

        Assert.assertEquals("1-2-3", EvalUtils.evaluate("l.join('-')", model).toString());
//        Assert.assertEquals("1-2-3", EvalUtils.evaluate("l.inject('counting')", model).toString());
    }

    @Test
    public void testGetLines() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, UserFunction> functions = new HashMap<String, UserFunction>();

        String s = "ololo";
        List<String> l = EvalUtils.getLines(s);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(s, l.get(0));

        s = "int i = 1; i++;";
        l = EvalUtils.getLines(s);
        Assert.assertEquals(2, l.size());
        Assert.assertEquals("int i = 1", l.get(0));
        Assert.assertEquals("i++", l.get(1));

        s = "String s = \"abc;\"";
        l = EvalUtils.getLines(s);
        Assert.assertEquals(1, l.size());
        Assert.assertEquals(s, l.get(0));

        s = "int i = 1; i++; \n i=\";;\n\n\".length()";
        l = EvalUtils.getLines(s);
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
        assertEquals(2, EvalUtils.evaluate(s, model));

    }
}
