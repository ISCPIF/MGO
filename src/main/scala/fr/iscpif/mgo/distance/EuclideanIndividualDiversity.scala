/*
 * Copyright (C) 18/01/14 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.iscpif.mgo.distance

import fr.iscpif.mgo._
import fr.iscpif.mgo.tools.Lazy
import fr.iscpif.mgo.tools.distance.EuclideanDistance

import scala.util.Random

trait EuclideanIndividualDiversity <: GA with EuclideanDistance with IndividualPosition {
  def genomeDiversity(g: Seq[G]) = {
    g.map {
      i1 =>
        Lazy(
          g.map {
            i2 => distance(values.get(i1), values.get(i2))
          }.sum
        )
    }
  }

  def individualDistance(individuals: Seq[Individual[G, P, F]])(implicit rng: Random): Seq[Lazy[Double]] =
    individuals map {
      i1 =>
        val i1Pos = individualPosition(i1)
        Lazy(
          individuals.map {
            i2 => distance(i1Pos, individualPosition(i2))
          }.sum
        )
    }

}
