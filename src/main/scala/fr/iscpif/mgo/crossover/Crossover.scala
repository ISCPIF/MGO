/*
 * Copyright (C) 2012 reuillon
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

package fr.iscpif.mgo.crossover

import fr.iscpif.mgo._
import tools._
import genome.G
import util.Random

/**
 * Implement a crossover operation between 2 genomes
 */
trait Crossover <: G with P with F with A {

  type Crossover = (Seq[G], Population[G, P, F], A, Random) => Seq[G]
  def crossovers: Vector[Crossover]

  /**
   * Crossover g1 and g2
   *
   *  @param genomes
   *  @param population last computed population
   *  @param archive last archive
   *  @return the result of the crossover
   */
  def crossover(genomes: Seq[G], population: Population[G, P, F], archive: A)(implicit rng: Random): Seq[G] =
    if (crossovers.isEmpty) genomes
    else crossovers.random(rng)(genomes, population, archive, rng)

}