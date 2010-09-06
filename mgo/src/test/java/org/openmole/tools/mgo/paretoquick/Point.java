/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openmole.tools.mgo.paretoquick;

import java.util.ArrayList;
import org.openmole.tools.mgo.model.IPoint;


/**
 *
 * @author salmamesmoudi
 */
public class Point implements IPoint {
   
    public Comparable[] coordonates;

    public Point(Comparable... coordonnes){
         coordonates = coordonnes;
    }

    public Comparable getComparable(int dim){
        return coordonates[dim];
    }

    public int getSize() {
        return coordonates.length;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for(Comparable coord : coordonates) {
            builder.append(coord.toString());
            builder.append(" ");
        }

        return builder.toString();
    }

    @Override
    public int compareTo(IPoint t) {
        int compare = size() - t.size();

        if(compare != 0) return compare;

        for(int i = 0; i < size(); i++) {
            compare = getComparable(i).compareTo(t.getComparable(i));
            if(compare != 0) return compare;
        }

        return 0;
    }

    @Override
    public int size() {
        return coordonates.length;
    }

}
