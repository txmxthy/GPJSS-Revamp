/*
  Copyright 2012 by James McDermott
  Licensed under the Academic Free License version 3.0
  See the file "license.md" for more information
*/


package ec.app.gpsemantics.func;

/*
 * SemanticNode.java
 *
 */

/**
 * @author James McDermott
 */

public class SemanticExtra extends SemanticNode {
    final char value;
    final int index;

    public SemanticExtra(char v, int i) {
        value = v;
        index = i;
    }

    public char value() {
        return value;
    }

    public int index() {
        return index;
    }
}
