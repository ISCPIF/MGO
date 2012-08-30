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

package fr.iscpif.mgo.modifier

import fr.iscpif.mgo._

/**
 * Compute a meta-fitness with a rank an a diversity
 */
trait RankDiversityModifier extends RankModifier with DiversityModifier with Ranking with DiversityMetric {
  
  override type MF = Diversity with Rank
  
  override def modify(evaluated: IndexedSeq[Individual[G]]) = {  
    val ranks = rank(evaluated)
    val distances = diversity(evaluated zip ranks)
      
    (evaluated zip ranks zip distances) map {
      case ((i, r), d) => 
        PopulationElement(
            i,
            new Diversity with Rank {
              val diversity = d
              val rank = r
              override def toString = diversity + " " + rank
            }
        )
    }
    
  }
}
