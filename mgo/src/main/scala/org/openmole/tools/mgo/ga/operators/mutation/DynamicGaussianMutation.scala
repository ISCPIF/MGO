/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openmole.tools.mgo.ga.operators.mutation

import org.openmole.tools.mgo._
import ga._
import tools.Math._
import tools.Random._
import java.util.Random

class DynamicGaussianMutation [G <: GAGenome with SigmaParameters, 
                               F <: GAGenomeFactory[G] with GASigmaParametersFactory [G]] 
  extends Mutation [G, F] {

  //http://c-faq.com/lib/gaussian.html
  //http://www.developpez.net/forums/d331848/autres-langages/algorithmes/contribuez/generation-nombre-aleatoire-suivant-loi-gaussienne/
  //http://www.taygeta.com/random/gaussian.html
  override def mutate (genomes : IndexedSeq [G], factory: F) (implicit aprng : Random) : G = {
    val genome = genomes.random 
    val newValues = (genome.values zip genome.sigma) map {
      case (v, s) => clamp (aprng.nextGaussian * s + v, 0, 1)
    }
    factory.buildFromValues (genome, newValues)
  }
}