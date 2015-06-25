/*
 * Copyright (C) 2015 Guillaume Chérel
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

package fr.iscpif.mgo.breed

import fr.iscpif.mgo._
import util.Random
import fr.iscpif.mgo.tools.StateMonad
import fr.iscpif.mgo.genome.NEATGenome
import fr.iscpif.mgo.genome.NEATGenome._
import fr.iscpif.mgo.archive.NEATArchive
import collection.immutable.IntMap
import collection.immutable.Map
import math.{ max, abs, round }

/**
 * Layer of the cake for the breeding part of the evolution algorithm
 */
trait NEATBreeding <: Breeding with NEATArchive with NEATGenome with Lambda with DoubleFitness with MinimalGenome {

  type BreedingState = (Int, //global innovation number
  Int, //global node number
  Seq[NEATGenome.Innovation], //record of innovations
  IntMap[NEATGenome.Genome]) //index of species

  def interSpeciesMatingProb: Double

  def mutationAddNodeProb: Double

  // = 0.2
  def mutationAddLinkProb: Double // = 0.2
  require(mutationAddNodeProb + mutationAddLinkProb <= 1)

  def mutationAddLinkBiasProb: Double

  def mutationWeightSigma: Double

  // = 1
  def mutationWeightProb0: Double

  // = 0.5
  def mutationDisableProb: Double

  // = 0.2
  def mutationEnableProb: Double // = 0.2

  def genDistDisjointCoeff: Double

  // = 1.0
  def genDistExcessCoeff: Double

  // = 1.0
  def genDistWeightDiffCoeff: Double // = 0.4

  def speciesCompatibilityThreshold: Double // = 3.0

  def crossoverInheritDisabledProb: Double // = 0.75

  def initialWeight(implicit rng: Random): Double = rng.nextGaussian() * mutationWeightSigma

  lazy val initialGenome: Genome = minimalGenome

  def breed(population: Population[G, P, F], archive: A, size: Int)(implicit rng: Random): Seq[G] = {

    val (globalInnovationNumber, globalNodeNumber) = population.foldLeft((0, 0)) {
      case (acc, indiv) =>
        val (gin, gnn) = acc
        (max(gin, indiv.genome.connectionGenes.map {
          _.innovation
        }.max), max(gnn, indiv.genome.lastNodeId))
    }

    if (population.isEmpty) Seq(initialGenome)
    else {
      val parents = selection(population, archive, size).toList

      val (p1, p2) = parents.head
      val breedAll: StateMonad[List[Genome], BreedingState] =
        parents.tail.foldLeft(doBreed(p1, p2, population, archive)(List.empty)) {
          (acc, parents) =>
            val (p1, p2) = parents
            acc bind doBreed(p1, p2, population, archive)
        }

      val (bred, _) = breedAll.runstate((globalInnovationNumber, globalNodeNumber, Seq.empty, archive.indexOfSpecies))
      bred.toSeq

      // parents.foldLeft((List.empty, archive.globalInnovationNumber, archive.recordOfInnovations, archive.indexOfSpecies)) {
      //   (state, parents) =>
      //     val (offsprings, gin, roi, ios) = acc
      //     val (p1, p2) = parents
      //     val (newgenome, (newgin, newroi, newios)) = doBreedOnce(p1, p2).runstate(state)
      //     (newgenome :: offsprings, newgin, newroi, newios)
      // }

      //!!!use state monad!!! (state=gin, roi, ios; result=newgenome)
      // val breeding: Iterator[Genome] =
      //   parents.map {
      //     parents =>
      //       val (i1, i2) = parents
      //       val newgen = mutate(
      //         crossover(i1, i2, population, archive),
      //         population,
      //         archive)
      //       newgen
      //   }

      //postBreeding(offsprings, population, archive)
    }
  }

  def doWithParents(p1: Individual[G, P, F], p2: Individual[G, P, F]): StateMonad[(Individual[G, P, F], Individual[G, P, F]), BreedingState] =
    StateMonad.pure((p1, p2))

  def doCrossover(
    parents: (Individual[G, P, F], Individual[G, P, F]),
    population: Population[G, P, F],
    archive: A)(implicit rng: Random): StateMonad[Genome, BreedingState] =
    StateMonad[Genome, BreedingState]({
      state => //this crossover doesn't actually use state
        val (gin, gnn, roi, ios) = state
        val crossed = crossover(parents._1, parents._2)
        (crossed, (gin, gnn, roi, ios))
    })

  def doMutate(
    g: Genome,
    population: Population[G, P, F],
    archive: A)(implicit rng: Random): StateMonad[Genome, BreedingState] =
    StateMonad[Genome, BreedingState]({
      state =>
        mutate(g, state)
    })

  def doPostBreeding(
    g: Genome,
    population: Population[G, P, F],
    archive: A): StateMonad[Genome, BreedingState] =
    StateMonad[Genome, BreedingState]({
      case (gin, gnn, roi, ios) =>
        val (newGenome, newios) = setSpecies(g, ios)
        (newGenome, (gin, gnn, roi, newios))
    })

  def doBreedOnce(
    p1: Individual[G, P, F],
    p2: Individual[G, P, F],
    population: Population[G, P, F],
    archive: A)(implicit rng: Random): StateMonad[Genome, BreedingState] =
    doWithParents(p1, p2).bind {
      doCrossover(_, population, archive)
    }.bind {
      doMutate(_, population, archive)
    }.bind {
      doPostBreeding(_, population, archive)
    }

  def doBreed(
    p1: Individual[G, P, F],
    p2: Individual[G, P, F],
    population: Population[G, P, F],
    archive: A)(
      curoffsprings: List[Genome])(implicit rng: Random): StateMonad[List[Genome], BreedingState] = {
    StateMonad({
      state =>
        val (thisoffspring, newstate) = doBreedOnce(p1, p2, population, archive).runstate(state)
        (thisoffspring :: curoffsprings, newstate)
    })
  }

  /** Select an individual among the population. */
  def selection(
    population: Population[G, P, F],
    archive: A,
    size: Int)(implicit rng: Random): Iterator[(Individual[G, P, F], Individual[G, P, F])] = {
    val indivsBySpecies: Map[Int, Seq[Individual[G, P, F]]] =
      population.toIndividuals.groupBy { indiv => indiv.genome.species }
    val speciesFitnesses: Seq[(Int, Double)] = indivsBySpecies.iterator.map {
      case (sp, indivs) => (sp, indivs.map {
        _.fitness
      }.sum / indivs.size)
    }.toSeq
    val sumOfSpeciesFitnesses: Double = speciesFitnesses.map {
      _._2
    }.sum
    val speciesOffsprings: Seq[(Int, Int)] = speciesFitnesses.map { case (sp, f) => (sp, round(f * size / sumOfSpeciesFitnesses).toInt) }
    speciesOffsprings.flatMap {
      case (species, nb) =>
        Iterator.fill(nb) {
          val nparents = indivsBySpecies(species).length
          val p1 = indivsBySpecies(species)(rng.nextInt(nparents))
          val p2 =
            // Have a chance to mate between different species
            if ((rng.nextDouble() < interSpeciesMatingProb) && (indivsBySpecies.size > 1)) {
              val otherSpecies = indivsBySpecies.keys.filter { _ != species }.toSeq(rng.nextInt(indivsBySpecies.size - 1))
              val nindivsOtherSpecies = indivsBySpecies(otherSpecies).length
              indivsBySpecies(otherSpecies)(rng.nextInt(nindivsOtherSpecies))
            } else indivsBySpecies(species)(rng.nextInt(nparents))
          (p1, p2)
        }
    }.toIterator
  }

  // //returns new node values if and two new nodes numbers if already present
  // def dontOverride(
  //   cg: ConnectionGene, 
  //   nodes: IntMap[Node],
  //   lastNode: Int): (Int, Int) = {
  //   val n1 = 
  //     if (nodes.contains(cg.inNode)) lastNode + 1
  //     else cg.inNode
  //   val lastNode2 = max(lastNode, n1)
  //   val n2 =
  //     if (nodes.contains(cg.outNode)) lastNode2 + 1
  //     else cg.outNode
  //   (n1, n2)
  // }

  def crossover(
    i1: Individual[Genome, P, Double],
    i2: Individual[Genome, P, Double])(implicit rng: Random): Genome = {
    // - align the 2 parent genomes according to their innovation number
    // - genes that match no other gene in the other genome are disjoint if they occur within the range of the the other genome innovation numbers
    // - they are excess if they occur outside this range.
    val aligned = alignGenomes(i1.genome.connectionGenes, i2.genome.connectionGenes)
    // - a new offspring is composed by chosing each matching genes randomly from either parent.
    // - excess or disjoint genes from the fittest parent are included.
    // - if the parents fitnesses are the same, inherit from the smallest genome or randomly
    val fittest = if (i1.fitness == i2.fitness) 0 else if (i1.fitness > i2.fitness) 1 else 2

    val newconnectiongenes =
      aligned.foldLeft(Seq[ConnectionGene]()) { (acc, pair) =>
        pair match {
          case (None, Some(cg2), _) =>
            if (fittest == 1) acc
            else if (fittest == 2) acc :+ cg2
            else if (rng.nextBoolean()) acc else acc :+ cg2

          case (Some(cg1), None, _) =>
            if (fittest == 1) acc :+ cg1
            else if (fittest == 2) acc
            else if (rng.nextBoolean()) acc :+ cg1 else acc

          case (Some(cg1), Some(cg2), _) =>
            val inherit =
              if (fittest == 1) cg1
              else if (fittest == 2) cg2
              else if (rng.nextBoolean()) cg1 else cg2

            // If one gene is disabled, the offspring gene is more likely to be disabled
            val disable =
              if (!(cg1.enabled && cg2.enabled)) rng.nextDouble() < crossoverInheritDisabledProb
              else false

            if (disable) acc :+ inherit.copy(enabled = false)
            else acc :+ inherit

          case (None, None, _) => acc
        }
      }.toVector

    // Include at least one node from each parent. Also include one for each input, output, and bias node that are not connected.
    val newnodesidx =
      (newconnectiongenes.flatMap { cg => Seq(cg.inNode, cg.outNode) } ++
        (inputNodesIndices ++ biasNodesIndices ++ outputNodesIndices)
      ).toSet
    val newnodes = IntMap(newnodesidx.toSeq.map { i =>
      i -> {
        if (!i1.genome.nodes.contains(i)) i2.genome.nodes(i)
        else if (!i2.genome.nodes.contains(i)) i1.genome.nodes(i)
        else if (rng.nextBoolean()) i1.genome.nodes(i) else i2.genome.nodes(i)
      }
    }: _*)

    //if (newconnectiongenes.exists(cg1 => newconnectiongenes.exists(cg2 => (cg1.outNode == cg2.inNode) && (cg2.outNode == cg1.inNode)))) throw new RuntimeException(s"LOOOOOOP (crossover) $newconnectiongenes $newnodes\n$i1\n$i2")
    if (newconnectiongenes.exists(cg1 => {
      if (newnodes(cg1.inNode).level >= newnodes(cg1.outNode).level) {
        println(cg1)
        true
      } else false
    })) throw new RuntimeException(s"Backwards!! $newconnectiongenes $newnodes\n$i1\n$i2")
    val result = Genome(
      connectionGenes = newconnectiongenes,
      nodes = newnodes,
      // Give the offspring its parents species. Species will be reattributed at the postBreeding 
      // stage anyway. This will just be used as a hint to speed up species attribution
      species = i1.genome.species,
      lastNodeId = newnodes.lastKey)

    result
  }

  object AlignmentInfo extends Enumeration {
    type AlignmentInfo = Value
    val Aligned, Excess, Disjoint = Value
  }

  import AlignmentInfo._

  /**
   * Returns a list of aligned connection genes. The first two elements of each tuple give aligned genes, (or None for unmatching genes) and the third is set to 0 when the genes are
   * aligned, 1 for an excess genes, and 2 for disjoint genes
   */
  def alignGenomes(
    cg1: Seq[ConnectionGene],
    cg2: Seq[ConnectionGene]): List[(Option[ConnectionGene], Option[ConnectionGene], AlignmentInfo)] = {
    if (cg1.isEmpty && cg2.isEmpty)
      List.empty
    else if (cg1.isEmpty)
      (None, Some(cg2.head), Disjoint) :: alignGenomes(Seq.empty, cg2.tail)
    else if (cg2.isEmpty)
      (Some(cg1.head), None, Disjoint) :: alignGenomes(cg1.tail, Seq.empty)
    else if (cg1.head.innovation == cg2.head.innovation)
      (Some(cg1.head), Some(cg2.head), Aligned) :: alignGenomes(cg1.tail, cg2.tail)
    else if (cg1.head.innovation < cg2.head.innovation)
      (Some(cg1.head), None, Excess) :: alignGenomes(cg1.tail, cg2)
    else
      (None, Some(cg2.head), Excess) :: alignGenomes(cg1, cg2.tail)
  }

  def mutate(
    genome: Genome,
    state: BreedingState)(implicit rng: Random): (Genome, BreedingState) = {
    val r = rng.nextDouble()
    if (r < mutationAddNodeProb) {
      mutateAddNode(genome, state)
    } else if (r < mutationAddNodeProb + mutationAddLinkProb) {
      mutateAddLink(genome, state)
    } else {
      // successively mutate weights and mutate enable bits on the genome
      val result = {
        mutateWeights(_: Genome)
      }.andThen {
        mutateEnabled(_: Genome)
      }(genome)
      (result, state)
    }
  }

  def pickNodesAddLink(genome: Genome)(implicit rng: Random): Option[(Int, Int)]

  def mutateAddLink(
    genome: Genome,
    state: BreedingState)(implicit rng: Random): (Genome, BreedingState) = {
    val pair = pickNodesAddLink(genome)

    pair match {
      case None => (genome, state)
      case Some((u, v)) =>
        val (gin, gnn, roi, ios) = state

        //Look for a LinkInnovation between nodes u and v in the record
        val sameInRoi: Option[LinkInnovation] = roi.collectFirst {
          case LinkInnovation(number, `u`, `v`) => LinkInnovation(number, u, v)
        }

        val thisInnov =
          sameInRoi match {
            case None => LinkInnovation(gin + 1, u, v)
            case Some(x) => x
          }

        val newroi =
          sameInRoi match {
            case None => roi :+ thisInnov
            case Some(x) => roi
          }

        val newgene =
          NEATGenome.ConnectionGene(
            inNode = u,
            outNode = v,
            weight = initialWeight,
            enabled = true,
            innovation = thisInnov.innovnum)

        val newgenome =
          NEATGenome.Genome(
            connectionGenes =
              genome.connectionGenes :+ newgene,
            nodes = genome.nodes,
            species = genome.species,
            lastNodeId = genome.lastNodeId)

        (newgenome, (gin + 1, gnn, newroi, ios))

    }
  }

  /** pick a connection gene, disable it and add 2 new connection genes */
  def mutateAddNode(
    genome: Genome,
    state: BreedingState)(implicit rng: Random): (Genome, BreedingState) = {
    val (gin, gnn, roi, ios) = state

    val picked: Int = rng.nextInt(genome.connectionGenes.length)
    val pickedcg = genome.connectionGenes(picked)

    //Look for a NodeInnovation between nodes u and v in the record
    val sameInRoi: Option[NodeInnovation] = roi.collectFirst {
      case NodeInnovation(a, b, c, pickedcg.inNode, pickedcg.outNode) => NodeInnovation(a, b, c, pickedcg.inNode, pickedcg.outNode)
    }

    val thisInnov =
      sameInRoi match {
        case None => NodeInnovation(gin + 1, gin + 2, gnn + 1, pickedcg.inNode, pickedcg.outNode)
        case Some(x) => x
      }

    val newcg1: ConnectionGene =
      ConnectionGene(
        inNode = pickedcg.inNode,
        outNode = thisInnov.newnode,
        weight = 1,
        enabled = true,
        innovation = thisInnov.innovnum1)

    val newcg2: ConnectionGene =
      ConnectionGene(
        inNode = thisInnov.newnode,
        outNode = pickedcg.outNode,
        weight = pickedcg.weight,
        enabled = true,
        innovation = thisInnov.innovnum2)

    val newconnectiongenes = genome.connectionGenes.updated(
      picked,
      pickedcg.copy(enabled = false)
    ) ++ Vector(newcg1, newcg2)

    val newnodes = genome.nodes + (thisInnov.newnode -> HiddenNode(level = (genome.nodes(pickedcg.inNode).level + genome.nodes(pickedcg.outNode).level) / 2.0))

    val newgenome = Genome(
      connectionGenes = newconnectiongenes,
      nodes = newnodes,
      species = genome.species,
      lastNodeId = thisInnov.newnode)

    val newroi =
      roi :+ thisInnov

    (newgenome, (gin + 2, gnn + 1, newroi, ios))
  }

  def mutateWeights(
    genome: Genome)(implicit rng: Random): Genome =
    genome.copy(
      connectionGenes =
        genome.connectionGenes.map { (cg: ConnectionGene) =>
          cg.copy(weight = mutateWeight(cg.weight))
        })

  def mutateWeight(x: Double)(implicit rng: Random): Double =
    if (rng.nextDouble < mutationWeightProb0) 0
    else x + rng.nextGaussian() * mutationWeightSigma

  def mutateEnabled(
    g: Genome)(implicit rng: Random): Genome =
    g.copy(
      connectionGenes =
        g.connectionGenes.map { mutateEnabled })

  def mutateEnabled(cg: ConnectionGene)(implicit rng: Random): ConnectionGene = {
    val newenabled =
      if (cg.enabled) if (rng.nextDouble() < mutationDisableProb) !cg.enabled else cg.enabled
      else if (rng.nextDouble() < mutationEnableProb) !cg.enabled else cg.enabled
    cg.copy(enabled = newenabled)
  }

  /**
   * Sets the innovation numbers of each new mutation in the offsprings such that
   * * unique mutations are attributed a unique number greater than the archive's global innovation number,
   * * mutations that are identical to one found it the archive's record of innovations are attributed its number
   */
  // def postBreeding(
  //   offsprings: Seq[Genome],
  //   population: Population[Genome, P, F],
  //   archive: NEATArchive.Archive)(implicit rng: Random): Seq[Genome] = {
  //   // Set innovation numbers in all offsprings, and then Attribute a species to each offspring
  //   { setInnovationNumbers(_: Seq[Genome], archive) }.andThen {
  //     setSpecies(_: Seq[Genome], archive)
  //   }(offsprings)
  // }

  //  def setInnovationNumbers(
  //    offsprings: Seq[Genome],
  //    archive: NEATArchive.Archive): Seq[Genome] = {
  //    val (newgenomes, newgin, newroi) = offsprings.foldLeft((Seq[Genome](), archive.globalInnovationNumber, archive.recordOfInnovations)) { (acc, genome) =>
  //      val (curgenomes, curgin, curroi) = acc
  //      val (newgenome, newgin, newroi) = genome.setInnovationNumber(curgin, curroi)
  //      (curgenomes :+ newgenome, newgin, newroi)
  //    }
  //    newgenomes
  //  }

  //  def setSpecies(
  //    offsprings: Seq[Genome],
  //    archive: NEATArchive.Archive): Seq[Genome] = {
  //    val (newgenomes, _) = offsprings.foldLeft((Seq[Genome](), archive.indexOfSpecies)) { (acc, genome) =>
  //      val (curgenomes, curios) = acc
  //      // Find a genome in the index of species which is sufficiently similar to the current genome.
  //      // Start by checking if the offspring is compatible with the parent species (currently set in the species member of the offspring)
  //      if (genomesDistance(genome, curios(genome.species)) < speciesCompatibilityThreshold)
  //        (curgenomes :+ genome, curios)
  //      else
  //        curios.find {
  //          case (sindex, prototype) => genomesDistance(prototype, genome) < speciesCompatibilityThreshold
  //        } match {
  //          //If found, attribute the species to the current genome
  //          case Some((species, _)) =>
  //            (curgenomes :+ genome.copy(species = species),
  //              curios)
  //          //Otherwise, create a new species with the current genome as the prototype
  //          case None => {
  //            val newspecies = curios.lastKey + 1
  //            val newgenome = genome.copy(species = newspecies)
  //            (curgenomes :+ newgenome,
  //              curios + (newspecies, newgenome))
  //          }
  //        }
  //    }
  //    newgenomes
  //  }

  def setSpecies(
    genome: Genome,
    indexOfSpecies: IntMap[G]): (Genome, IntMap[G]) = {
    // Find a genome in the index of species which is sufficiently similar to the current genome.
    // Start by checking if the offspring is compatible with the parent species (currently set in the species member of the offspring)
    if (genomesDistance(genome, indexOfSpecies(genome.species)) < speciesCompatibilityThreshold)
      (genome, indexOfSpecies)
    else
      indexOfSpecies.find {
        case (sindex, prototype) => genomesDistance(prototype, genome) < speciesCompatibilityThreshold
      } match {
        //If found, attribute the species to the current genome
        case Some((species, _)) =>
          (genome.copy(species = species), indexOfSpecies)
        //Otherwise, create a new species with the current genome as the prototype
        case None =>
          val newspecies = indexOfSpecies.lastKey + 1
          val newgenome = genome.copy(species = newspecies)
          val newios = indexOfSpecies + ((newspecies, newgenome))
          (newgenome, newios)

      }
  }

  def genomesDistance(
    g1: Genome,
    g2: Genome): Double = {
    val alignment = alignGenomes(g1.connectionGenes, g2.connectionGenes)
    val (excess, disjoint, weightdiff, matchingGenes) = distanceFactors(alignment)
    val longestGenomeSize = max(g1.connectionGenes.length, g2.connectionGenes.length)
    val res = (genDistDisjointCoeff * excess / longestGenomeSize) +
      (genDistExcessCoeff * disjoint / longestGenomeSize) +
      (genDistWeightDiffCoeff * weightdiff / matchingGenes)
    res
  }

  def distanceFactors(alignment: List[(Option[ConnectionGene], Option[ConnectionGene], AlignmentInfo)]): (Int, Int, Double, Int) =
    alignment match {
      case List() => (0, 0, 0, 0)
      case head :: tail =>
        val (excess, disjoint, weightdiff, matchingGenes) = distanceFactors(tail)
        head match {
          case (Some(cg1), Some(cg2), Aligned) => (excess, disjoint, weightdiff + abs(cg1.weight - cg2.weight), matchingGenes + 1)
          case (_, _, Disjoint) => (excess, disjoint + 1, weightdiff, matchingGenes)
          case (_, _, Excess) => (excess + 1, disjoint, weightdiff, matchingGenes)
          case _ => throw new RuntimeException(s"Improper alignment: $head") //this case should never happen
        }

    }

}