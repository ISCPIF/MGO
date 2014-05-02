/*
 * Copyright (C) Guillaume Chérel 18/04/14
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.iscpif.mgo.elitism

import fr.iscpif.mgo._
import util.Random

trait PickNNicheElitism <: NicheElitism {

  def keepN: Int

  def keepIndividuals(individuals: Seq[Individual[G, P, F]])(implicit aprng: Random): Seq[Individual[G, P, F]] =
    aprng.shuffle(individuals).take(keepN)

}