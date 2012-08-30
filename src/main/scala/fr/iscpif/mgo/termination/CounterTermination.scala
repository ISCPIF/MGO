/*
 * Copyright (C) 2011 Sebastien Rey
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

package fr.iscpif.mgo.termination

import fr.iscpif.mgo._

/**
 * Stop after a finite number of state
 */
trait CounterTermination extends Termination {
  
  /// Number of steps before the algorithm stops
  def steps: Int
  
  type STATE = Int
  
  def initialState(p: Population[G, MF]) = 0
    
  def terminated(population: Population[G, MF], step: STATE) = 
    (step >= steps, step + 1)
}
