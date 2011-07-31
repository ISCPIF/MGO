package org.openmole.tools.mgo.ga.operators

import org.openmole.tools.mgo._
import ga._
import org.openmole.tools.mgo.genomefactory.GenomeSigmaFactory
import tools.Math._
import tools.Random._
import java.util.Random
import scala.math._

class CoEvolvingSigmaValuesMutation [G <: GAGenome with SigmaParameters, 
                                     F <: GenomeSigmaFactory [G]] (
  implicit val factory : F) extends Mutation [G, F] {
  

  override def operate (genomes : IndexedSeq [G]) (implicit aprng : Random) : G = {
    val genome = genomes.random
    
    //http://www.nashcoding.com/2010/07/07/evolutionary-algorithms-the-little-things-youd-never-guess-part-1/#fnref-28-1
    var indexedSeqSigma:IndexedSeq[Double] = genome.sigma.map{(0. max _ * exp(aprng.nextGaussian))}
    
    val newValues:IndexedSeq[Double] = (genome.values zip indexedSeqSigma) map {
      case (v, s) => clamp (aprng.nextGaussian * s + v, 0, 1)}
    
    return (factory.buildGenome(newValues ++ indexedSeqSigma))
  }
}