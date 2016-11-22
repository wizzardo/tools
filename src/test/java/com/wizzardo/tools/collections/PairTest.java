package com.wizzardo.tools.collections;

import org.junit.Assert;
import org.junit.Test;

public class PairTest {
    @Test
    public void test() {
        Pair<Integer, Integer> pair = new Pair<Integer, Integer>(0, 1);

        Assert.assertEquals("0: 1", pair.toString());
        Assert.assertEquals(Integer.valueOf(0), pair.key);
        Assert.assertEquals(Integer.valueOf(1), pair.value);


        Assert.assertEquals(0, new Pair<Integer, Integer>(null, null).hashCode());
        Assert.assertNotEquals(0, new Pair<Integer, Integer>(1, null).hashCode());
        Assert.assertNotEquals(0, new Pair<Integer, Integer>(null, 1).hashCode());

        Assert.assertTrue(pair.equals(pair));
        Assert.assertFalse(pair.equals(null));
        Assert.assertFalse(pair.equals(""));

        Assert.assertFalse(new Pair<Integer, Integer>(null, 2).equals(new Pair<Integer, Integer>(null, 1)));
        Assert.assertFalse(new Pair<Integer, Integer>(null, 2).equals(new Pair<Integer, Integer>(1, 1)));
        Assert.assertFalse(new Pair<Integer, Integer>(1, 2).equals(new Pair<Integer, Integer>(null, 1)));
        Assert.assertFalse(new Pair<Integer, Integer>(1, 2).equals(new Pair<Integer, Integer>(1, 1)));

        Assert.assertFalse(new Pair<Integer, Integer>(1, null).equals(new Pair<Integer, Integer>(2, null)));
        Assert.assertFalse(new Pair<Integer, Integer>(1, null).equals(new Pair<Integer, Integer>(2, 2)));
        Assert.assertFalse(new Pair<Integer, Integer>(1, 2).equals(new Pair<Integer, Integer>(1, null)));
        Assert.assertFalse(new Pair<Integer, Integer>(1, 2).equals(new Pair<Integer, Integer>(1, 1)));
        Assert.assertTrue(new Pair<Integer, Integer>(null, null).equals(new Pair<Integer, Integer>(null, null)));
    }

}
