/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openmole.tools.mgo.ga.selection

import org.openmole.tools.mgo._
import org.openmole.tools.mgo.ga.GAFitness
import org.openmole.tools.mgo.ga.domination._
import org.openmole.tools.mgo.ga.selection._

object Ranking {
  
  def pareto[F <: GAFitness](individuals: IndexedSeq [Individual[_, F]], dominanceType: Dominant): IndexedSeq[Ranking] = { 
    import dominanceType._
    
    individuals.zipWithIndex.map { 
      case (indiv, index) =>
        new Ranking {
          val rank = 
            individuals.zipWithIndex.filterNot {
              case (_, index2) => index != index2
            }.count { 
              case(indiv2, _) => indiv.isDominated(indiv2)
            }
        }
    }
  }
}


trait Ranking {
  def rank: Int
}
