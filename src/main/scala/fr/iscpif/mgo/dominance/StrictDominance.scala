/*
 * Copyright (C) 2012 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.iscpif.mgo.dominance

import fr.iscpif.mgo._

/**
 * A point dominates another if all its objective are better
 */
trait StrictDominance <: Dominance {

  override def isDominated(p1: Seq[Double], p2: Seq[Double]): Boolean =
    (p1 zip p2).forall { case (g1, g2) => g2 < g1 }

}
