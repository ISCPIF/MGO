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

package fr.iscpif.mgo.ranking

import fr.iscpif.mgo._
import fr.iscpif.mgo.tools.Lazy

import scala.util.Random

/**
 * Layer of the cake for algorithm with a ranking scheme.
 */
trait Ranking <: G with P with F {
  /**
   * Compute the rank of a set of individuals.
   *
   * @param values the values to rank
   * @return the ranks of the individuals in the same order
   */
  def rank(values: Population[G, P, F])(implicit rng: Random): Seq[Lazy[Int]]
}
