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

package fr.iscpif.mgo.genome

import monocle._
import monocle.Macro._
import scala.util.Random
import monocle.syntax._

object GAGenomeWithSigma {
  case class Genome(values: Seq[Double], sigma: Seq[Double])
}

/**
 * Genome for genetic algorithm with an autoadaptative sigma component
 */
trait GAGenomeWithSigma extends GA with Sigma {

  type G = GAGenomeWithSigma.Genome

  def rawValues = mkLens("values")

  def fullGenome = SimpleLens[G, Seq[Double]](
    v => values.get(v) ++ sigma.get(v),
    (c, v) =>
      (c |-> values set v.slice(0, v.size / 2)) |-> sigma set v.slice(v.size / 2, v.size)
  )

  def sigma = mkLens[G, Seq[Double]]("sigma")

  def randomGenome(implicit rng: Random) = {
    def rnd = Stream.continually(rng.nextDouble).take(genomeSize).toIndexedSeq
    GAGenomeWithSigma.Genome(rnd, rnd)
  }

}

