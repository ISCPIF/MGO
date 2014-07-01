/*
 * Copyright (C) 27/11/12 Romain Reuillon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.iscpif.mgo.elitism

import fr.iscpif.mgo._
import util.Random

trait MapElitism extends Elitism with MapPlotter with Aggregation with NicheElitism {
  override def niche(individual: Individual[G, P, F]): Any = plot(individual)
  override def keep(individuals: Seq[Individual[G, P, F]])(implicit rng: Random) = Seq(individuals.minBy(i => aggregate(i.fitness)))
}
