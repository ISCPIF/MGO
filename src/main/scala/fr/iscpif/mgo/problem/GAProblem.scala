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

package fr.iscpif.mgo.problem

import fr.iscpif.mgo._

/**
 * Cake to define a problem for a genetic algorithm
 */
trait GAProblem extends Problem with Scaling {
  type G = GAGenome
  type F = MGFitness

  /**
   * Compute the fitness value from a genome values.
   *
   * @param g the genome to evaluate
   * @return the fitness for this genome
   */
  def apply(g: G) = new MGFitness {
    val values = apply(scale(g.values).toIndexedSeq)
  }

  /**
   * Scale the genenome from [0.0, 1.0] to the correct scale for the fitness
   * evaluation
   *
   * @param g the genome to scale
   * @return the scaled genome
   */
  def scale(g: G): Seq[Double] = scale(g.values)

  /**
   * Scale a population element genome from [0.0, 1.0] to the correct scale
   *
   * @param i the population element to scale
   * @return the scaled population element
   */
  def scale[MF](i: PopulationElement[G, F, MF]): (Seq[Double], F, MF) = (scale(i.genome), i.fitness, i.metaFitness)

  /**
   * Compute the fitness for a point
   *
   * @param x the point to evaluate
   * @return the fitness of this point
   */
  def apply(x: IndexedSeq[Double]): Seq[Double]
}
