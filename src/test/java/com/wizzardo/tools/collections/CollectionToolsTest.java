package com.wizzardo.tools.collections;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by wizzardo on 14.09.15.
 */
public class CollectionToolsTest {

    @Test
    public void group() {
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(4);
            add(5);
        }};

        Map<Boolean, List<Integer>> groups = CollectionTools.group(list, new CollectionTools.Closure<Boolean, Integer>() {
            @Override
            public Boolean execute(Integer it) {
                return it % 2 == 0;
            }
        }, new CollectionTools.Closure<Integer, Integer>() {
            @Override
            public Integer execute(Integer it) {
                return it;
            }
        });

        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(2, groups.get(true).size());
        Assert.assertEquals(3, groups.get(false).size());
    }
}
