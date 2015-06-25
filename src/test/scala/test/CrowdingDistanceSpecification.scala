/*
 * Copyright (C) Guillaume Chérel 30/04/14
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

package test

import org.scalacheck.Properties
import org.scalacheck.Prop
import org.scalacheck.Prop._
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary

import fr.iscpif.mgo.tools.metric.CrowdingDistance
import util.Random

object CrowdingDistanceSpecification extends Properties("CrowdingDistance") {
  implicit val rng = new Random

  property("1D 3 points not ordered") =
    forAll(Gen.choose(-1000.0, 1000.0), Gen.choose(0.0, 1000.0), Gen.choose(0.0, 1000.0)) {
      (x0: Double, distXMin: Double, distXMax: Double) =>
        val points = Vector(Vector(x0 - distXMin), Vector(x0 + distXMax), Vector(x0))
        val cd = CrowdingDistance(points)
        val cdXMin = cd(0)()
        val cdXMax = cd(1)()
        val cdX0 = cd(2)()
        all("xmin; test vector: " + points.toString |: Compare.doubles(cdXMin, Double.PositiveInfinity),
          "xmax; test vector: " + points.toString |: Compare.doubles(cdXMax, Double.PositiveInfinity),
          "x0; test vector: " + points.toString |: Compare.doubles(cdX0, 1.0))
    }

  property("1D 4 points ordered") =
    forAll(Gen.choose(-1000.0, 1000.0), Gen.choose(0.0, 1000.0), Gen.choose(0.0, 1000.0), Gen.choose(0.0, 1000.0)) {
      (x0: Double, dist01: Double, dist12: Double, dist23: Double) =>
        val cd = CrowdingDistance(Vector(Vector(x0),
          Vector(x0 + dist01),
          Vector(x0 + dist01 + dist12),
          Vector(x0 + dist01 + dist12 + dist23)))
        val maxMinusMin = dist01 + dist12 + dist23
        all(Compare.doubles(cd(1)(), (dist01 + dist12) / maxMinusMin),
          Compare.doubles(cd(2)(), (dist12 + dist23) / maxMinusMin))
    }

}
