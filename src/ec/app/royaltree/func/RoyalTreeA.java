/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.app.royaltree.func;

/*
 * RoyalTreeA.java
 *
 */

/**
 * @author James McDermott
 */

public class RoyalTreeA extends RoyalTreeNode {
    public int expectedChildren() {
        return 1;
    }

    public char value() {
        return 'A';
    }
}
