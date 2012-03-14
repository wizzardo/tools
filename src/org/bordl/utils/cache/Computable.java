/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bordl.utils.cache;

/**
 *
 * @author Moxa
 */
public interface Computable<K, V> {

    public V compute(K k) throws InterruptedException;
}
