 /*
 * Copyright (C) 2011 srey
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
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.iscpif.mgo

import annotation.tailrec
import fr.iscpif.mgo.crossover.CrossOver
import fr.iscpif.mgo._
import fr.iscpif.mgo.modifier.Modifier
import fr.iscpif.mgo.mutation.Mutation
import fr.iscpif.mgo.selection.Selection
import fr.iscpif.mgo.termination.Termination
import java.util.Random
import Individual._

trait Evolution extends Mutation with CrossOver with Termination with Selection with Modifier { self =>

  type G <: Genome
  type MF 
    
  type P = Population[G, MF]
  
  //Perform N step
  @tailrec private def evolveStep(
    population: P,
    evaluator: G => Fitness,
    state: STATE = initialState)(implicit aprng:Random, factory: Factory[G]): P = {
    val nextPop = evolve(population, evaluator)
    stepListner(nextPop, state)
    val (end, newState) = terminated(population, nextPop, state)
    if (end) nextPop
    else evolveStep(nextPop, evaluator, newState)
  }
  
  def run(population: P, evaluator: G => Fitness) (implicit aprng: Random, factory: Factory[G]): P = evolveStep(population, evaluator)
  def run(populationSize: Int, evaluator: G => Fitness)(implicit aprng: Random, factory: Factory[G]): P = evolveStep(randomPopulation(populationSize, evaluator), evaluator)
  
  def evolve(population: P, evaluator: G => Fitness)(implicit aprng: Random, factory: Factory[G]): P
  
  def randomPopulation(size: Int, evaluator: G => Fitness)(implicit aprng: Random, factory: Factory[G]): P =
    toPopulation((0 until size).map{ _ => factory.random }.map{ g => Individual(g, evaluator)})
  
  def stepListner(population: P, state: STATE) = {}
  
}