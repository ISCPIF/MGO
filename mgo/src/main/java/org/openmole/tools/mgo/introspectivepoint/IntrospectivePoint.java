/*
 *  Copyright (C) 2010 Romain Reuillon <romain.reuillon at openmole.org>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.openmole.tools.mgo.introspectivepoint;

import java.util.Iterator;
import org.openmole.tools.mgo.model.Point;


/**
 *
 * @author Romain Reuillon <romain.reuillon at openmole.org>
 */
public class IntrospectivePoint<T> extends Point {

    final Comparable[] comparables;
    final T object;

    IntrospectivePoint(T object, Comparable[] comparables) {
        this.object = object;
        this.comparables = comparables;
    }

    public T getObject() {
        return object;
    }

  /*  @Override
    public Iterable<? extends Comparable> getComparables() {
        return new Iterable<Comparable>() {

            @Override
            public Iterator<Comparable> iterator() {
                return new Iterator<Comparable>() {

                    int curFields = 0;

                    @Override
                    public boolean hasNext() {
                        return curFields < getDim();
                    }

                    @Override
                    public Comparable next() {
                        Comparable ret = getComparable(curFields);
                        curFields++;
                        return ret;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported.");
                    }
                };

            }
        };
    }*/

    @Override
    public Comparable getComparable(int dim) {
        return comparables[dim];
    }

    @Override
    public int getDim() {
        return comparables.length;
    }

    @Override
    public String toString() {
        return getObject().toString();
    }

}