/*
 * Copyright (C) 2014 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
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

package fr.iscpif.mgo.crossover

import fr.iscpif.mgo._
import monocle.syntax._

import scala.util.Random

trait BLXCrossover <: Crossover with GA {

  def alpha: Double = 0.5

  override def crossover(g1: G, g2: G, population: Seq[Individual[G, P, F]], archive: A)(implicit rng: Random): Seq[G] = {
    val (newG1, newG2) =
      (values.get(g1) zip values.get(g2)).map {
        case (c1, c2) =>
          val cmin = math.min(c1, c2)
          val cmax = math.max(c1, c2)
          val i = cmax - cmin
          def generate = rng.nextDouble().scale(cmin - alpha * i, cmax + alpha * i)
          (generate, generate)
      }.unzip
    Seq(g1 |-> values set newG1, g2 |-> values set newG2)
  }
}
